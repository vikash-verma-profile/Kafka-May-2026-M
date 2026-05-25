package com.training.kafka.streams.lab04;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.kafka.streams.StreamsConfigFactory;
import java.time.Duration;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

/**
 * Lab 04-mini project: validate orders, branch invalid/high-value, windowed regional totals.
 *
 * <p>Run: mvn -q exec:java -Dexec.mainClass=com.training.kafka.streams.lab04.OrderPipelineApp
 */
public final class OrderPipelineApp {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        String bootstrap = StreamsConfigFactory.arg(args, 0, StreamsConfigFactory.DEFAULT_BOOTSTRAP);
        Properties props = StreamsConfigFactory.baseProps("order-pipeline-app", bootstrap);

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> orders = builder.stream("orders");

        var branches =
                orders.split()
                        .branch((k, v) -> isValid(v), Branched.as("valid"))
                        .branch((k, v) -> !isValid(v), Branched.as("invalid"))
                        .noDefaultBranch();

        branches.get("invalid").to("invalid-orders");
        KStream<String, String> valid = branches.get("valid");

        valid.filter((k, v) -> amount(v) >= 5000)
                .peek((k, v) -> System.out.println("ALERT high-value order key=" + k))
                .to("high-value-orders");

        valid.groupBy((k, v) -> region(v))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
                .aggregate(
                        () -> 0.0,
                        (region, json, sum) -> sum + amount(json),
                        org.apache.kafka.streams.kstream.Materialized.with(
                                Serdes.String(), Serdes.Double()))
                .toStream()
                .mapValues(
                        (Windowed<String> region, Double total) ->
                                String.format(
                                        "{\"region\":\"%s\",\"windowStart\":%d,\"totalSales\":%.2f}",
                                        region.key(), region.window().start(), total))
                .to("order-analytics");

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        System.out.println("Order pipeline running.");
    }

    private static boolean isValid(String json) {
        try {
            JsonNode n = MAPPER.readTree(json);
            return n.path("valid").asBoolean(true)
                    && !n.path("region").asText("").isBlank()
                    && amount(json) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static double amount(String json) {
        try {
            return MAPPER.readTree(json).path("amount").asDouble(0);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String region(String json) {
        try {
            return MAPPER.readTree(json).path("region").asText("unknown");
        } catch (Exception e) {
            return "unknown";
        }
    }
}
