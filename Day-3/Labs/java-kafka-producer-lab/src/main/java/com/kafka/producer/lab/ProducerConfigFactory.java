package com.kafka.producer.lab;

import java.util.Properties;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

final class ProducerConfigFactory {

    static final String DEFAULT_BOOTSTRAP = "localhost:9092";
    static final String DEFAULT_TOPIC = "orders-topic";

    private ProducerConfigFactory() {
    }

    static Properties baseProps(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "day3-producer-lab");
        return props;
    }

    static String arg(String[] args, int index, String defaultValue) {
        return args.length > index ? args[index] : defaultValue;
    }

    static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
