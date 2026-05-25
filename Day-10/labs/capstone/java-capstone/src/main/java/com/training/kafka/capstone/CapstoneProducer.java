package com.training.kafka.capstone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Properties;
import java.util.Random;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * Capstone starter — tuned producer emitting order events to capstone-orders.
 *
 * <p>Run: mvn -q exec:java -Dexec.mainClass=com.training.kafka.capstone.CapstoneProducer
 */
public final class CapstoneProducer {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Random RND = new Random();

    public static void main(String[] args) throws Exception {
        String bootstrap = args.length > 0 ? args[0] : "localhost:9092";
        String topic = args.length > 1 ? args[1] : "capstone-orders";
        int count = args.length > 2 ? Integer.parseInt(args[2]) : 100;

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        props.put(ProducerConfig.LINGER_MS_CONFIG, "5");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 0; i < count; i++) {
                ObjectNode order = MAPPER.createObjectNode();
                order.put("orderId", "ord-" + i);
                order.put("customerId", "cust-" + RND.nextInt(20));
                order.put("region", "region-" + RND.nextInt(4));
                order.put("amount", 500 + RND.nextInt(9500));
                order.put("valid", true);
                String json = MAPPER.writeValueAsString(order);
                producer.send(new ProducerRecord<>(topic, order.get("orderId").asText(), json));
            }
            producer.flush();
        }
        System.out.println("Produced " + count + " events to " + topic);
    }
}
