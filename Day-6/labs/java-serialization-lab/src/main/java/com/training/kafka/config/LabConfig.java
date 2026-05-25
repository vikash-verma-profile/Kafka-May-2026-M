package com.training.kafka.config;

import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

public final class LabConfig {

    public static final String BOOTSTRAP = "localhost:9092";
    public static final String SCHEMA_REGISTRY = "http://localhost:8081";

    private LabConfig() {}

    public static String arg(String[] args, int index, String defaultValue) {
        return args != null && args.length > index ? args[index] : defaultValue;
    }

    public static Properties avroProducerProps(String bootstrap, String registryUrl) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer");
        props.put("schema.registry.url", registryUrl);
        props.put("auto.register.schemas", "true");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        return props;
    }

    public static Properties avroConsumerProps(String bootstrap, String registryUrl, String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        props.put("schema.registry.url", registryUrl);
        props.put("specific.avro.reader", "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return props;
    }
}
