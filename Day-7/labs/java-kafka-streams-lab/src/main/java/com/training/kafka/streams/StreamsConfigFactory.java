package com.training.kafka.streams;

import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

public final class StreamsConfigFactory {

    public static final String DEFAULT_BOOTSTRAP = "localhost:9092";

    private StreamsConfigFactory() {}

    public static String arg(String[] args, int index, String defaultValue) {
        return args != null && args.length > index ? args[index] : defaultValue;
    }

    public static Properties baseProps(String applicationId, String bootstrap) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        return props;
    }
}
