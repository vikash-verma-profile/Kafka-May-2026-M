# Day 7 Kafka Streams Lab

Python track: [python-stream-processing-lab](../python-stream-processing-lab/).

## Build

```bat
cd Day-7\labs\java-kafka-streams-lab
mvn -q compile
```

## Create topics first

```bat
cd ..\scripts
create-streams-topics.bat
```

## Run apps

| Lab | Main class |
|-----|------------|
| 01 | `com.training.kafka.streams.lab01.FirstStreamsApp` |
| 02 | `com.training.kafka.streams.lab02.WordCountApp` |
| 03 | `com.training.kafka.streams.lab03.OrderFilterAggregateApp` |
| 04 | `com.training.kafka.streams.lab04.OrderPipelineApp` |

Sample producer: `com.training.kafka.streams.OrderProducer`

Example:

```bat
mvn -q exec:java -Dexec.mainClass=com.training.kafka.streams.lab02.WordCountApp
```
