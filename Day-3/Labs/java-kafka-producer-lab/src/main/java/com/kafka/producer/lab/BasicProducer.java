package com.kafka.producer.lab;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * Lab 03- Send a single message with key "101" to orders-topic.
 *
 * <p>Run: mvn -q exec:java "-Dexec.mainClass=com.kafka.producer.lab.BasicProducer"
 */
public final class BasicProducer {

    public static void main(String[] args) throws Exception {
        String bootstrap = ProducerConfigFactory.arg(args, 0, ProducerConfigFactory.DEFAULT_BOOTSTRAP);
        String topic = ProducerConfigFactory.arg(args, 1, ProducerConfigFactory.DEFAULT_TOPIC);

        try (KafkaProducer<String, String> producer =
                new KafkaProducer<>(ProducerConfigFactory.baseProps(bootstrap))) {

            ProducerRecord<String, String> record =
                    new ProducerRecord<>(topic, "101", "Order Created");

            producer.send(record).get();
            System.out.println("Message Sent Successfully");
        }
    }
}
