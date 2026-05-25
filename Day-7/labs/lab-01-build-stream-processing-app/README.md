# Lab 01 — Build a Stream Processing Application

**Objective:** Create a Kafka Streams project, configure `StreamsConfig`, define input/output topics, implement a simple topology, and verify output.

From **Kafka_Streams.pptx** — Slide 27.

---

## Implementation

| Track | Command |
|-------|---------|
| **Java** | `mvn -q exec:java -Dexec.mainClass=com.training.kafka.streams.lab01.FirstStreamsApp` in [java-kafka-streams-lab](../../java-kafka-streams-lab/) |
| **Python** | `python lab01_uppercase_stream.py` in [python-stream-processing-lab](../../python-stream-processing-lab/) |

Create topics: [scripts/create-streams-topics.bat](../scripts/create-streams-topics.bat).

---

## Prerequisites

- Kafka on `localhost:9092`
- Java 17+, Maven 3.8+

---

## Step 1 — Maven project

```xml
<dependency>
  <groupId>org.apache.kafka</groupId>
  <artifactId>kafka-streams</artifactId>
  <version>3.7.0</version>
</dependency>
```

Match version to your broker when possible.

---

## Step 2 — Create topics

```bat
cd %KAFKA_HOME%
bin\windows\kafka-topics.bat --create --topic streams-input --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
bin\windows\kafka-topics.bat --create --topic streams-output --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

---

## Step 3 — StreamsConfig

```java
Properties props = new Properties();
props.put(StreamsConfig.APPLICATION_ID_CONFIG, "first-streams-app");
props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
// Optional for production:
// props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
```

---

## Step 4 — Topology (uppercase transform)

```java
StreamsBuilder builder = new StreamsBuilder();
KStream<String, String> input = builder.stream("streams-input");

input.mapValues(String::toUpperCase)
     .to("streams-output");

KafkaStreams streams = new KafkaStreams(builder.build(), props);
streams.start();
Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
```

---

## Step 5 — Produce test data

**Terminal 2:**

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic streams-input
```

Type: `hello kafka streams`

---

## Step 6 — Verify output

**Terminal 3:**

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic streams-output --from-beginning
```

**Expected:** `HELLO KAFKA STREAMS`

---

## Step 7 — Inspect application metadata

```bat
bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --list
```

Find group `first-streams-app` (internal Streams consumer).

---

## Discussion tie-in (slide 10)

For VIP orders > ₹5,000: input `orders`, output `vip-orders`, use `.filter()` (stateless), same partition key as order id for ordering.

---

## Checkpoint

- [ ] Streams app starts without exception
- [ ] Output topic receives transformed records
- [ ] App shuts down cleanly on `Ctrl+C`

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| `Topic streams-input not found` | Create topics first |
| No output | Check `APPLICATION_ID` unique; verify producer topic name |
| `StreamsException` serde | Key/value types must match serdes |
