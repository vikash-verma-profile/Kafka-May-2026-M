package com.training.kafka.streams.lab02;

import com.training.kafka.streams.StreamsConfigFactory;
import java.util.Arrays;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

/**
 * Lab 02-real-time word count: sentences → word-counts.
 *
 * <p>Run: mvn -q exec:java -Dexec.mainClass=com.training.kafka.streams.lab02.WordCountApp
 */
public final class WordCountApp {

    public static void main(String[] args) {
        String bootstrap = StreamsConfigFactory.arg(args, 0, StreamsConfigFactory.DEFAULT_BOOTSTRAP);
        Properties props = StreamsConfigFactory.baseProps("wordcount-app", bootstrap);

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> sentences = builder.stream("sentences");

        KTable<String, Long> counts =
                sentences
                        .flatMapValues(v -> Arrays.asList(v.toLowerCase().split("\\W+")))
                        .filter((k, word) -> word != null && !word.isBlank())
                        .groupBy((k, word) -> word)
                        .count(Materialized.as("word-count-store"));

        counts.toStream().to("word-counts", Produced.with(Serdes.String(), Serdes.Long()));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        System.out.println("Word count app running.");
    }
}
