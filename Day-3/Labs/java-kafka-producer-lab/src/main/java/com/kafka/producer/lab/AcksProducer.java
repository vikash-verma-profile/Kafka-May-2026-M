package com.kafka.producer.lab;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * Lab 07- Configure acks (0, 1, or all).
 *
 * <p>Run: mvn -q exec:java -Dexec.mainClass=com.kafka.producer.lab.AcksProducer -Dexec.args="all"
 */
public final class AcksProducer {

    public static void main(String[] args) throws Exception {
        String bootstrap = ProducerConfigFactory.arg(args, 0, ProducerConfigFactory.DEFAULT_BOOTSTRAP);
        String topic = ProducerConfigFactory.arg(args, 1, ProducerConfigFactory.DEFAULT_TOPIC);
        String acks = ProducerConfigFactory.arg(args, 2, "all");

        Properties props = ProducerConfigFactory.baseProps(bootstrap);
        props.put(ProducerConfig.ACKS_CONFIG, acks);

        System.out.printf("Producer acks=%s, topic=%s%n", acks, topic);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            ProducerRecord<String, String> record =
                    new ProducerRecord<>(topic, "acks-demo", "Message with acks=" + acks);

            producer.send(record, (metadata, error) -> {
                if (error != null) {
                    System.err.println("Failed: " + error.getMessage());
                } else {
                    System.out.printf(
                            "Acknowledged on partition=%d offset=%d%n",
                            metadata.partition(),
                            metadata.offset());
                }
            }).get();

            System.out.println("Message sent.");
        }
    }
}
