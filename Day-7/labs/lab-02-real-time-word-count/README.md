# Lab 02 — Real-Time Word Count

**Objective:** Read sentences from a topic, split into words, count per word, publish counts to an output topic.

From **Kafka_Streams.pptx** — Slides 32–33.

---

## Implementation

| Track | Command |
|-------|---------|
| **Java** | `com.training.kafka.streams.lab02.WordCountApp` |
| **Python** | `python lab02_word_count.py` |

---

## Prerequisites

- [Lab 01](../lab-01-build-stream-processing-app/README.md)
- Topics: `sentences` (input), `word-counts` (output)

---

## Step 1 — Create topics

```bat
bin\windows\kafka-topics.bat --create --topic sentences --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
bin\windows\kafka-topics.bat --create --topic word-counts --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

---

## Step 2 — Topology

```java
KStream<String, String> sentences = builder.stream("sentences");

KTable<String, Long> counts = sentences
    .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
    .filter((k, word) -> !word.isEmpty())
    .groupBy((key, word) -> word)
    .count();

counts.toStream().to("word-counts", Produced.with(Serdes.String(), Serdes.Long()));
```

Flow: **Input → flatMapValues → groupBy → count → Output**

---

## Step 3 — Application config

```java
props.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-app");
props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
```

Requires broker 2.5+ and `read_committed` if downstream consumers use transactions.

---

## Step 4 — Run and feed data

**Producer:**

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic sentences
```

```
kafka streams are powerful
kafka is distributed
streams are fun
```

**Consumer on output:**

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic word-counts --from-beginning --property print.key=true --property print.value=true
```

**Expected:** Keys like `kafka`, `streams` with increasing counts.

---

## Step 5 — Observe state store (optional)

```bat
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list
```

Look for internal changelog: `wordcount-app-*-changelog`.

---

## Checkpoint

- [ ] Word counts update as new sentences arrive
- [ ] Same word in multiple sentences increments count
- [ ] Changelog topic exists (stateful operation)

---

## Concepts (slides 15–18)

| Operation | Stateless / Stateful |
|-----------|---------------------|
| `flatMapValues` | Stateless |
| `groupBy` + `count` | Stateful (uses RocksDB + changelog) |
