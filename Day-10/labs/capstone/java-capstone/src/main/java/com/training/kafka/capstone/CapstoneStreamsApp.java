package com.training.kafka.capstone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

/**
 * Capstone starter — filter invalid orders, emit capstone-processed.
 *
 * <p>Extend with windowed aggregations and joins per capstone/README.md.
 */
public final class CapstoneStreamsApp {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        String bootstrap = args.length > 0 ? args[0] : "localhost:9092";

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "capstone-streams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> orders = builder.stream("capstone-orders");

        orders.filter((k, v) -> isValid(v)).to("capstone-processed");

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        System.out.println("Capstone streams running: capstone-orders -> capstone-processed");
    }

    private static boolean isValid(String json) {
        try {
            JsonNode n = MAPPER.readTree(json);
            return n.path("valid").asBoolean(true) && n.path("amount").asDouble(0) > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
