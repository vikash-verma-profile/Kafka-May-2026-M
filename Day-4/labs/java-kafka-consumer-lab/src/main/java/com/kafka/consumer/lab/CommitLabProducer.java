package com.kafka.consumer.lab;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

/** Lab 03 - sends 100 numbered messages to demo-topic. */
public final class CommitLabProducer {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ConsumerConfigFactory.DEFAULT_BOOTSTRAP);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 1; i <= 100; i++) {
                String key = "commit-key-" + (i % 3);
                String value = "commit-msg-" + i;
                producer.send(new ProducerRecord<>(ConsumerConfigFactory.DEMO_TOPIC, key, value));
            }
            producer.flush();
            System.out.println("Sent 100 messages to " + ConsumerConfigFactory.DEMO_TOPIC);
        }
    }
}
