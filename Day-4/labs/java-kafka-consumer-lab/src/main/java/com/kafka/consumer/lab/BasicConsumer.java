package com.kafka.consumer.lab;

import java.time.Duration;
import java.util.Collections;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Lab 01 - basic poll loop with manual commit on shutdown.
 */
public final class BasicConsumer {

    public static void main(String[] args) {
        String groupId = ConsumerConfigFactory.arg(args, 0, "lab1-group");
        var props = ConsumerConfigFactory.baseProps(ConsumerConfigFactory.DEFAULT_BOOTSTRAP, groupId);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(
                    Collections.singletonList(ConsumerConfigFactory.DEMO_TOPIC),
                    new AssignmentLogger("BasicConsumer"));

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutdown: committing offsets...");
                consumer.commitSync();
            }));

            System.out.printf("Consuming topic=%s group=%s (Ctrl+C to stop)%n",
                    ConsumerConfigFactory.DEMO_TOPIC, groupId);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf(
                            "Partition=%d, Offset=%d, Key=%s, Value=%s%n",
                            record.partition(),
                            record.offset(),
                            record.key(),
                            record.value());
                }
            }
        }
    }
}
