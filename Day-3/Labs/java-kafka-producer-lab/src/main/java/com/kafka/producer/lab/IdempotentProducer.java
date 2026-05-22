package com.kafka.producer.lab;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * Lab 09- Idempotent producer prevents duplicate writes on retry.
 *
 * <p>Run: mvn -q exec:java "-Dexec.mainClass=com.kafka.producer.lab.IdempotentProducer"
 */
public final class IdempotentProducer {

    public static void main(String[] args) throws Exception {
        String bootstrap = ProducerConfigFactory.arg(args, 0, ProducerConfigFactory.DEFAULT_BOOTSTRAP);
        String topic = ProducerConfigFactory.arg(args, 1, ProducerConfigFactory.DEFAULT_TOPIC);
        int count = ProducerConfigFactory.parseInt(ProducerConfigFactory.arg(args, 2, "10"), 10);

        Properties props = ProducerConfigFactory.baseProps(bootstrap);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        System.out.println("Idempotence enabled. Sending " + count + " messages...");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 1; i <= count; i++) {
                String value = "Idempotent-order-" + i;
                ProducerRecord<String, String> record =
                        new ProducerRecord<>(topic, "order-" + i, value);

                producer.send(record, (metadata, error) -> {
                    if (error != null) {
                        System.err.println("Error: " + error.getMessage());
                    } else {
                        System.out.printf(
                                "%s -> partition=%d offset=%d%n",
                                value, metadata.partition(), metadata.offset());
                    }
                }).get();
            }
        }

        System.out.println("Done. With idempotence, retries do not create duplicate records.");
    }
}
