package com.kafka.producer.lab;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Lab 06- No key: messages spread across partitions (sticky batching per batch).
 *
 * <p>Run: mvn -q exec:java -Dexec.mainClass=com.kafka.producer.lab.RoundRobinProducer
 */
public final class RoundRobinProducer {

    public static void main(String[] args) throws Exception {
        String bootstrap = ProducerConfigFactory.arg(args, 0, ProducerConfigFactory.DEFAULT_BOOTSTRAP);
        String topic = ProducerConfigFactory.arg(args, 1, ProducerConfigFactory.DEFAULT_TOPIC);
        int count = ProducerConfigFactory.parseInt(ProducerConfigFactory.arg(args, 2, "20"), 20);

        System.out.printf("Sending %d messages WITHOUT key to %s%n", count, topic);

        try (KafkaProducer<String, String> producer =
                new KafkaProducer<>(ProducerConfigFactory.baseProps(bootstrap))) {

            CountDownLatch done = new CountDownLatch(count);

            for (int i = 1; i <= count; i++) {
                String value = "Order-" + i;
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, value);

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

        System.out.println("Done. Observe multiple partition numbers in the output.");
    }
}
