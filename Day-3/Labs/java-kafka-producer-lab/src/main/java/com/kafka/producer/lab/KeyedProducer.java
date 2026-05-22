package com.kafka.producer.lab;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Lab 05- Same key routes all messages to the same partition.
 *
 * <p>Run: mvn -q exec:java "-Dexec.mainClass=com.kafka.producer.lab.KeyedProducer"
 */
public final class KeyedProducer {

    public static void main(String[] args) throws Exception {
        String bootstrap = ProducerConfigFactory.arg(args, 0, ProducerConfigFactory.DEFAULT_BOOTSTRAP);
        String topic = ProducerConfigFactory.arg(args, 1, ProducerConfigFactory.DEFAULT_TOPIC);
        String key = ProducerConfigFactory.arg(args, 2, "customer-1");
        int count = ProducerConfigFactory.parseInt(ProducerConfigFactory.arg(args, 3, "10"), 10);

        System.out.printf("Sending %d messages with key=%s to %s%n", count, key, topic);

        try (KafkaProducer<String, String> producer =
                new KafkaProducer<>(ProducerConfigFactory.baseProps(bootstrap))) {

            CountDownLatch done = new CountDownLatch(count);

            for (int i = 1; i <= count; i++) {
                String value = "Order-" + i;
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

                producer.send(record, (RecordMetadata metadata, Exception error) -> {
                    if (error != null) {
                        System.err.println("Send failed: " + error.getMessage());
                    } else {
                        System.out.printf(
                                "%s -> partition=%d offset=%d%n",
                                value, metadata.partition(), metadata.offset());
                    }
                    done.countDown();
                });
            }

            producer.flush();
            done.await(30, TimeUnit.SECONDS);
        }

        System.out.println("Done. All messages with the same key should share one partition.");
    }
}
