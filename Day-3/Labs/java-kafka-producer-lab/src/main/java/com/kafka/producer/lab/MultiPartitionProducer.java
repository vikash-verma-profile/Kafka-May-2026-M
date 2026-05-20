package com.kafka.producer.lab;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Lab 12- 100 messages with customerId keys (4 customers -> 4 partitions).
 *
 * <p>Run: mvn -q exec:java -Dexec.mainClass=com.kafka.producer.lab.MultiPartitionProducer
 */
public final class MultiPartitionProducer {

    public static void main(String[] args) throws Exception {
        String bootstrap = ProducerConfigFactory.arg(args, 0, ProducerConfigFactory.DEFAULT_BOOTSTRAP);
        String topic = ProducerConfigFactory.arg(args, 1, ProducerConfigFactory.DEFAULT_TOPIC);
        int count = ProducerConfigFactory.parseInt(ProducerConfigFactory.arg(args, 2, "100"), 100);

        Properties props = ProducerConfigFactory.baseProps(bootstrap);
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        Map<Integer, Integer> partitionCounts = new HashMap<>();

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            CountDownLatch done = new CountDownLatch(count);

            for (int i = 1; i <= count; i++) {
                String customerId = "customer-" + (i % 4);
                String value = "Order-" + i;
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, customerId, value);

                producer.send(record, (RecordMetadata metadata, Exception error) -> {
                    if (error == null) {
                        partitionCounts.merge(metadata.partition(), 1, Integer::sum);
                        System.out.printf(
                                "key=%s %s -> partition=%d offset=%d%n",
                                customerId, value, metadata.partition(), metadata.offset());
                    } else {
                        System.err.println("Error: " + error.getMessage());
                    }
                    done.countDown();
                });
            }

            producer.flush();
            done.await(60, TimeUnit.SECONDS);
        }

        System.out.println("\n--- Partition distribution ---");
        partitionCounts.forEach((p, c) -> System.out.printf("Partition %d: %d messages%n", p, c));
    }
}
