package com.kafka.consumer.lab;

import java.time.Duration;
import java.util.Collections;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/** Lab 03 - commitSync() after each non-empty poll batch. */
public final class ManualSyncCommitConsumer {

    public static void main(String[] args) {
        String groupId = ConsumerConfigFactory.arg(args, 0, "commit-lab-group");
        var props = ConsumerConfigFactory.baseProps(ConsumerConfigFactory.DEFAULT_BOOTSTRAP, groupId);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(ConsumerConfigFactory.DEMO_TOPIC));
            System.out.println("ManualSyncCommitConsumer: commitSync() after each batch");

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("SYNC partition=%d offset=%d value=%s%n",
                            record.partition(), record.offset(), record.value());
                }
                if (!records.isEmpty()) {
                    consumer.commitSync();
                    System.out.printf("Committed %d records%n", records.count());
                }
            }
        }
    }
}
