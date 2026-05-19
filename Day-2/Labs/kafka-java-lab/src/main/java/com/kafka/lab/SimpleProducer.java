package com.kafka.lab;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Publishes sample messages to Kafka.
 * <p>
 * Run: mvn -q exec:java -Dexec.mainClass=com.kafka.lab.SimpleProducer
 */
public final class SimpleProducer {

    public static void main(String[] args) throws Exception {
        String bootstrap = arg(args, 0, KafkaConfig.DEFAULT_BOOTSTRAP);
        String topic = arg(args, 1, KafkaConfig.DEFAULT_TOPIC);
        int messageCount = parseInt(arg(args, 2, "5"), 5);

        System.out.printf("Connecting to %s, topic=%s, messages=%d%n", bootstrap, topic, messageCount);

        try (KafkaProducer<String, String> producer =
                new KafkaProducer<>(KafkaConfig.producerProps(bootstrap))) {

            CountDownLatch done = new CountDownLatch(messageCount);

            for (int i = 1; i <= messageCount; i++) {
                String value = "Hello from Java producer - message " + i;
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, value);

                producer.send(record, (RecordMetadata metadata, Exception error) -> {
                    if (error != null) {
                        System.err.printf("Failed to send: %s%n", error.getMessage());
                    } else {
                        System.out.printf(
                                "Sent: \"%s\" -> partition=%d offset=%d%n",
                                value,
                                metadata.partition(),
                                metadata.offset());
                    }
                    done.countDown();
                });
            }

            producer.flush();
            if (!done.await(30, TimeUnit.SECONDS)) {
                System.err.println("Timed out waiting for send callbacks.");
                System.exit(1);
            }
        }

        System.out.println("Producer finished.");
    }

    private static String arg(String[] args, int index, String defaultValue) {
        return args.length > index ? args[index] : defaultValue;
    }

    private static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
