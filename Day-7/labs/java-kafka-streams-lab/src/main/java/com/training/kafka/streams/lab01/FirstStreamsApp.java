package com.training.kafka.streams.lab01;

import com.training.kafka.streams.StreamsConfigFactory;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

/**
 * Lab 01 — uppercase transform: streams-input → streams-output.
 *
 * <p>Run: mvn -q exec:java -Dexec.mainClass=com.training.kafka.streams.lab01.FirstStreamsApp
 */
public final class FirstStreamsApp {

    public static void main(String[] args) {
        String bootstrap = StreamsConfigFactory.arg(args, 0, StreamsConfigFactory.DEFAULT_BOOTSTRAP);
        Properties props = StreamsConfigFactory.baseProps("first-streams-app", bootstrap);

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> input = builder.stream("streams-input");
        input.mapValues(value -> value.toUpperCase())
                .to("streams-output", Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        System.out.println("Running. Produce to streams-input, consume streams-output.");
    }
}
