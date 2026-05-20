package com.kafka.producer.lab;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Lab 08- Retries and retry backoff (try stopping broker mid-run).
 *
 * <p>Run: mvn -q exec:java -Dexec.mainClass=com.kafka.producer.lab.RetriesProducer
 */
public final class RetriesProducer {

    public static void main(String[] args) throws Exception {
        String bootstrap = ProducerConfigFactory.arg(args, 0, ProducerConfigFactory.DEFAULT_BOOTSTRAP);
        String topic = ProducerConfigFactory.arg(args, 1, ProducerConfigFactory.DEFAULT_TOPIC);
        int count = ProducerConfigFactory.parseInt(ProducerConfigFactory.arg(args, 2, "5"), 5);

        Properties props = ProducerConfigFactory.baseProps(bootstrap);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);

        System.out.println("Retries=3, retry.backoff.ms=1000. Tip: restart broker during slow sends.");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            CountDownLatch done = new CountDownLatch(count);

            for (int i = 1; i <= count; i++) {
                String value = "Retry-demo-" + i;
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, value);

                producer.send(record, (RecordMetadata metadata, Exception error) -> {
                    if (error != null) {
                        System.err.printf("Failed after retries: %s%n", error.getMessage());
                    } else {
                        System.out.printf(
                                "Sent %s -> partition=%d offset=%d%n",
                                value, metadata.partition(), metadata.offset());
                    }
                    done.countDown();
                });

                Thread.sleep(2000);
            }

            producer.flush();
            done.await(60, TimeUnit.SECONDS);
        }
    }
}
