package com.kafka.consumer.lab;

import java.time.Duration;
import java.util.Collections;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * Lab 02 - consumer groups; logs name and partition assignment on rebalance.
 */
public final class NamedConsumer {

    public static void main(String[] args) {
        String groupId = ConsumerConfigFactory.arg(args, 0, "lab-group");
        String name = ConsumerConfigFactory.arg(args, 1, "Consumer-1");
        var props = ConsumerConfigFactory.baseProps(ConsumerConfigFactory.DEFAULT_BOOTSTRAP, groupId);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.CLIENT_ID_CONFIG, name);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(
                    Collections.singletonList(ConsumerConfigFactory.DEMO_TOPIC),
                    new AssignmentLogger(name));

            System.out.printf("[%s] group=%s topic=%s (Ctrl+C to stop)%n", name, groupId,
                    ConsumerConfigFactory.DEMO_TOPIC);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf(
                            "[%s] P=%d offset=%d key=%s value=%s%n",
                            name,
                            record.partition(),
                            record.offset(),
                            record.key(),
                            record.value());
                }
                if (!records.isEmpty()) {
                    consumer.commitSync();
                }
            }
        }
    }
}
