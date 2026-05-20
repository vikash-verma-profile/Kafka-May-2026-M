package com.kafka.producer.lab;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Lab 13- batch.size, linger.ms, compression.type, buffer.memory.
 *
 * <p>Run: mvn -q exec:java -Dexec.mainClass=com.kafka.producer.lab.PerformanceTunedProducer
 */
public final class PerformanceTunedProducer {

    public static void main(String[] args) throws Exception {
        String bootstrap = ProducerConfigFactory.arg(args, 0, ProducerConfigFactory.DEFAULT_BOOTSTRAP);
        String topic = ProducerConfigFactory.arg(args, 1, ProducerConfigFactory.DEFAULT_TOPIC);
        int count = ProducerConfigFactory.parseInt(ProducerConfigFactory.arg(args, 2, "50"), 50);

        Properties props = ProducerConfigFactory.baseProps(bootstrap);
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        System.out.printf(
                "Performance config: batch.size=16384 linger.ms=10 compression=snappy messages=%d%n",
                count);

        long start = System.nanoTime();

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            CountDownLatch done = new CountDownLatch(count);

            for (int i = 1; i <= count; i++) {
                final int seq = i;
                String payload = "Perf-message-" + seq + "-".repeat(50);
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, payload);

                producer.send(record, (RecordMetadata metadata, Exception error) -> {
                    if (error == null && seq % 10 == 0) {
                        System.out.printf(
                                "Sample: partition=%d offset=%d%n",
                                metadata.partition(), metadata.offset());
                    }
                    done.countDown();
                });
            }

            producer.flush();
            done.await(60, TimeUnit.SECONDS);
        }

        double seconds = (System.nanoTime() - start) / 1_000_000_000.0;
        System.out.printf("Sent %d messages in %.2f s (%.1f msg/s)%n", count, seconds, count / seconds);
    }
}
