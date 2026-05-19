package com.kafka.lab;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * Subscribes to a topic and prints messages as they arrive.
 * <p>
 * Run: mvn -q exec:java -Dexec.mainClass=com.kafka.lab.SimpleConsumer
 * <p>
 * Press Ctrl+C to stop.
 */
public final class SimpleConsumer {

    public static void main(String[] args) {
        String bootstrap = arg(args, 0, KafkaConfig.DEFAULT_BOOTSTRAP);
        String topic = arg(args, 1, KafkaConfig.DEFAULT_TOPIC);
        String groupId = arg(args, 2, "java-lab-group");

        Properties props = KafkaConfig.consumerProps(bootstrap, groupId);

        System.out.printf(
                "Connecting to %s, topic=%s, group=%s (Ctrl+C to stop)%n",
                bootstrap,
                topic,
                groupId);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf(
                            "Received: \"%s\" | partition=%d offset=%d key=%s%n",
                            record.value(),
                            record.partition(),
                            record.offset(),
                            record.key());
                }
            }
        }
    }

    private static String arg(String[] args, int index, String defaultValue) {
        return args.length > index ? args[index] : defaultValue;
    }
}
