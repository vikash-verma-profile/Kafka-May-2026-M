package com.training.kafka.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Properties;
import java.util.Random;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

/** Sample order/event producer for Day 7 labs. */
public final class OrderProducer {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Random RND = new Random();

    public static void main(String[] args) throws Exception {
        String bootstrap = StreamsConfigFactory.arg(args, 0, StreamsConfigFactory.DEFAULT_BOOTSTRAP);
        String topic = StreamsConfigFactory.arg(args, 1, "orders");
        int count = Integer.parseInt(StreamsConfigFactory.arg(args, 2, "50"));

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        String[] regions = {"north", "south", "east", "west"};

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 0; i < count; i++) {
                boolean valid = RND.nextInt(10) != 0;
                double amount = valid ? 1000 + RND.nextInt(9000) : -1;
                ObjectNode node = MAPPER.createObjectNode();
                node.put("orderId", "o-" + i);
                node.put("customerId", "c-" + RND.nextInt(5));
                node.put("region", regions[RND.nextInt(regions.length)]);
                node.put("amount", amount);
                node.put("valid", valid);
                String json = MAPPER.writeValueAsString(node);
                producer.send(new ProducerRecord<>(topic, node.get("orderId").asText(), json));
            }
        }
        System.out.println("Produced " + count + " orders to " + topic);
    }
}
