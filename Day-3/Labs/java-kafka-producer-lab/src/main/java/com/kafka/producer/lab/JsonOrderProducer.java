package com.kafka.producer.lab;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * Lab 11- Structured JSON order events.
 *
 * <p>Run: mvn -q exec:java -Dexec.mainClass=com.kafka.producer.lab.JsonOrderProducer
 */
public final class JsonOrderProducer {

    public static void main(String[] args) throws Exception {
        String bootstrap = ProducerConfigFactory.arg(args, 0, ProducerConfigFactory.DEFAULT_BOOTSTRAP);
        String topic = ProducerConfigFactory.arg(args, 1, ProducerConfigFactory.DEFAULT_TOPIC);
        int count = ProducerConfigFactory.parseInt(ProducerConfigFactory.arg(args, 2, "5"), 5);

        Properties props = ProducerConfigFactory.baseProps(bootstrap);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        String[] customers = {"Vikash", "John", "Priya", "Amit"};

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 1; i <= count; i++) {
                String customer = customers[i % customers.length];
                int amount = 1000 + i * 500;
                String json = String.format(
                        "{\"orderId\":%d,\"customer\":\"%s\",\"amount\":%d,\"payment\":\"UPI\"}",
                        1000 + i, customer, amount);

                String key = String.valueOf(1000 + i);
                producer.send(new ProducerRecord<>(topic, key, json), (metadata, error) -> {
                    if (error != null) {
                        System.err.println("Failed: " + error.getMessage());
                    } else {
                        System.out.printf(
                                "Sent JSON -> partition=%d offset=%d body=%s%n",
                                metadata.partition(), metadata.offset(), json);
                    }
                }).get();
            }
        }

        System.out.println("JSON messages sent.");
    }
}
