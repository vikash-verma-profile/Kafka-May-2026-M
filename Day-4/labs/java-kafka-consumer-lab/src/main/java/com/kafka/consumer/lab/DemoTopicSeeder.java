package com.kafka.consumer.lab;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

/** Seeds demo-topic with 60 keyed messages for Lab 01/02. */
public final class DemoTopicSeeder {

    public static void main(String[] args) {
        int count = ConsumerConfigFactory.parseInt(ConsumerConfigFactory.arg(args, 0, "60"), 60);
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ConsumerConfigFactory.DEFAULT_BOOTSTRAP);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 1; i <= count; i++) {
                String key = "key-" + (i % 3 + 1);
                String value = "Message-" + i;
                producer.send(new ProducerRecord<>(ConsumerConfigFactory.DEMO_TOPIC, key, value));
            }
            producer.flush();
            System.out.printf("Seeded %d messages to %s%n", count, ConsumerConfigFactory.DEMO_TOPIC);
        }
    }
}
