package com.kafka.consumer.lab;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

/** Lab 04 - sends 10,000 messages to lag-demo. */
public final class LagLoadProducer {

    public static void main(String[] args) {
        int total = ConsumerConfigFactory.parseInt(ConsumerConfigFactory.arg(args, 0, "10000"), 10000);
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ConsumerConfigFactory.DEFAULT_BOOTSTRAP);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.LINGER_MS_CONFIG, "5");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 1; i <= total; i++) {
                String key = "lag-key-" + (i % 3);
                String value = "lag-msg-" + i;
                producer.send(new ProducerRecord<>(ConsumerConfigFactory.LAG_TOPIC, key, value));
                if (i % 1000 == 0) {
                    System.out.println("Sent " + i + " messages...");
                }
            }
            producer.flush();
            System.out.println("Sent " + total + " messages to " + ConsumerConfigFactory.LAG_TOPIC);
        }
    }
}
