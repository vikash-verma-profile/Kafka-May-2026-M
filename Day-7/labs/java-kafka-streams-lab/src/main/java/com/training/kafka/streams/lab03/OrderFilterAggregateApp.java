package com.training.kafka.streams.lab03;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.kafka.streams.StreamsConfigFactory;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

/**
 * Lab 03 — filter valid orders and aggregate total amount per customer.
 *
 * <p>Run: mvn -q exec:java -Dexec.mainClass=com.training.kafka.streams.lab03.OrderFilterAggregateApp
 */
public final class OrderFilterAggregateApp {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        String bootstrap = StreamsConfigFactory.arg(args, 0, StreamsConfigFactory.DEFAULT_BOOTSTRAP);
        Properties props = StreamsConfigFactory.baseProps("order-filter-aggregate-app", bootstrap);

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> orders = builder.stream("orders-raw");

        KStream<String, String> valid =
                orders.filter((k, v) -> {
                    try {
                        JsonNode n = MAPPER.readTree(v);
                        return n.path("valid").asBoolean(true) && n.path("amount").asDouble(0) > 0;
                    } catch (Exception e) {
                        return false;
                    }
                });

        valid.filter((k, v) -> readAmount(v) >= 5000).to("orders-critical");

        KTable<String, Double> totals =
                valid.groupBy((k, v) -> readCustomerId(v))
                        .aggregate(
                                () -> 0.0,
                                (customerId, json, total) -> total + readAmount(json),
                                Materialized.with(Serdes.String(), Serdes.Double()));

        totals.toStream()
                .mapValues(
                        (customerId, total) ->
                                String.format("{\"customerId\":\"%s\",\"total\":%.2f}", customerId, total))
                .to("orders-metrics");

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        System.out.println("Filter + aggregate app running.");
    }

    private static double readAmount(String json) {
        try {
            return MAPPER.readTree(json).path("amount").asDouble(0);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String readCustomerId(String json) {
        try {
            return MAPPER.readTree(json).path("customerId").asText("unknown");
        } catch (Exception e) {
            return "unknown";
        }
    }
}
