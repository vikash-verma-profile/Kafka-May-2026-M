# Day 6 Java Serialization Lab

Maven project for Labs 01–02 (four formats), 04–06 (Avro + Schema Registry).

Python track: [python-serialization-lab](../python-serialization-lab/).

## Build

```bat
cd Day-6\labs\java-serialization-lab
mvn -q compile
```

Requires network for Confluent Maven repo on first build.

## Run

| Lab | Command |
|-----|---------|
| 01 | `mvn -q exec:java -Dexec.mainClass=com.training.kafka.lab01.FourFormatsLab` |
| 02 | `mvn -q exec:java -Dexec.mainClass=com.training.kafka.lab02.FormatBenchmark` |
| 04 | `..\scripts\create-employees-avro-topic.bat` then `mvn -q exec:java -Dexec.mainClass=com.training.kafka.lab04.AvroProducer` |
| 05 | `mvn -q exec:java -Dexec.mainClass=com.training.kafka.lab05.AvroConsumer` |
| 06 | `mvn -q exec:java -Dexec.mainClass=com.training.kafka.lab06.SchemaEvolutionDemo` |

Schema Registry must be running on `http://localhost:8081` for Labs 04–06.
