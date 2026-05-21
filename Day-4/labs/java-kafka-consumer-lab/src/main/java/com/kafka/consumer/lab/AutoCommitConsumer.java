package com.kafka.consumer.lab;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/** Lab 03 - enable.auto.commit=true (default interval 5s). */
public final class AutoCommitConsumer {

    public static void main(String[] args) {
        String groupId = ConsumerConfigFactory.arg(args, 0, "commit-lab-group");
        Properties props = ConsumerConfigFactory.baseProps(ConsumerConfigFactory.DEFAULT_BOOTSTRAP, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(ConsumerConfigFactory.DEMO_TOPIC));
            System.out.println("AutoCommitConsumer: enable.auto.commit=true, interval=5000ms");

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("AUTO partition=%d offset=%d value=%s%n",
                            record.partition(), record.offset(), record.value());
                }
            }
        }
    }
}
