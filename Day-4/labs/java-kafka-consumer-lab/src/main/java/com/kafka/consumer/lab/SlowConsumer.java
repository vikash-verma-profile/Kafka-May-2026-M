package com.kafka.consumer.lab;

import java.time.Duration;
import java.util.Collections;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/** Lab 04 - intentional delay per message to build consumer lag. */
public final class SlowConsumer {

    public static void main(String[] args) throws InterruptedException {
        String groupId = ConsumerConfigFactory.arg(args, 0, "lag-group");
        int sleepMs = ConsumerConfigFactory.parseInt(ConsumerConfigFactory.arg(args, 1, "500"), 500);
        String label = ConsumerConfigFactory.arg(args, 2, "SlowConsumer");

        var props = ConsumerConfigFactory.baseProps(ConsumerConfigFactory.DEFAULT_BOOTSTRAP, groupId);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.CLIENT_ID_CONFIG, label);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(
                    Collections.singletonList(ConsumerConfigFactory.LAG_TOPIC),
                    new AssignmentLogger(label));

            System.out.printf("[%s] group=%s topic=%s sleep=%dms%n",
                    label, groupId, ConsumerConfigFactory.LAG_TOPIC, sleepMs);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    Thread.sleep(sleepMs);
                    System.out.printf("[%s] P=%d offset=%d value=%s%n",
                            label, record.partition(), record.offset(), record.value());
                }
                if (!records.isEmpty()) {
                    consumer.commitSync();
                }
            }
        }
    }
}
