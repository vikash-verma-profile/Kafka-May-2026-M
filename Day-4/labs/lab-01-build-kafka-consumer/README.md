# Lab 01 - Build Kafka Consumer

**Objective:** Create and run a Kafka consumer that subscribes to `demo-topic`, polls messages, and verifies offsets with `kafka-consumer-groups.sh`.

Based on **Kafka_Consumers.pptx** - Lab 1.

---

## Prerequisites

- [Lab 00 - Initial setup](../lab-00-initial-setup/README.md) completed
- Broker running on `localhost:9092`
- Topic `demo-topic` with 3 partitions
- Test messages in the topic (run `scripts\seed-demo-topic.bat` if empty)

---

## Concepts (from slides)

| Concept | Summary |
|---------|---------|
| Pull model | Consumer calls `poll()` to fetch batches |
| Offset | Position in each partition; resumes after restart |
| `group.id` | Identifies the consumer group for coordination |
| `auto.offset.reset` | `earliest` when no committed offset exists |

---

## Step 1 - Review consumer configuration

Open:

```text
labs/java-kafka-consumer-lab/src/main/java/com/kafka/consumer/lab/
  ConsumerConfigFactory.java
  BasicConsumer.java
```

| Property | Value in lab |
|----------|----------------|
| `bootstrap.servers` | `localhost:9092` |
| `group.id` | `lab1-group` (default) |
| `auto.offset.reset` | `earliest` |
| `enable.auto.commit` | `false` (manual commit in code) |
| Deserializers | `StringDeserializer` |
| Topic | `demo-topic` |

---

## Step 2 - Compile the project

```powershell
cd C:\Users\om\Desktop\KafKa\Day-4\labs\java-kafka-consumer-lab
mvn -q compile
```

---

## Step 3 - Run the Java consumer

**Terminal 3:**

```powershell
cd C:\Users\om\Desktop\KafKa\Day-4\labs
run-java-consumer.bat com.kafka.consumer.lab.BasicConsumer
```

Custom group id (optional):

```powershell
run-java-consumer.bat com.kafka.consumer.lab.BasicConsumer my-group-1
```

**Expected output (example):**

```text
Assigned partitions: [demo-topic-0, demo-topic-1, demo-topic-2]
Partition=0, Offset=0, Key=key-1, Value=Message-1
...
```

Press `Ctrl+C` to stop. The consumer commits offsets on shutdown via `commitSync()` in a shutdown hook.

---

## Step 4 - Run the Python consumer (optional)

```powershell
cd C:\Users\om\Desktop\KafKa\Day-4\labs\python-kafka-consumer-lab
python basic_consumer.py
```

With custom group:

```powershell
python basic_consumer.py lab1-group
```

---

## Step 5 - Observe partition assignment & ordering

While the consumer runs, note:

1. **Assigned partitions** - printed at startup (all 3 when you are the only member of the group).
2. **Ordering** - offsets increase sequentially **within** each partition.
3. **Keys** - messages with the same key go to the same partition (if produced with keys).

Produce a few more messages in **Terminal 2**:

```powershell
%KAFKA_HOME%\bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic demo-topic --property parse.key=true --property key.separator=:
```

Example: `user-A:order-99`

---

## Step 6 - Verify offsets with CLI

Stop the consumer (`Ctrl+C`), then:

```powershell
cd C:\Users\om\Desktop\KafKa\Day-4\labs
scripts\describe-consumer-group.bat lab1-group
```

**Columns to read:**

| Column | Meaning |
|--------|---------|
| `CURRENT-OFFSET` | Last committed offset + 1 (next read position) |
| `LOG-END-OFFSET` | Latest offset in the partition |
| `LAG` | `LOG-END-OFFSET - CURRENT-OFFSET` (unprocessed messages) |

---

## Step 7 - Restart and observe resume behavior

1. Run `BasicConsumer` again with the **same** `group.id`.
2. It should continue from committed offsets (not re-read everything).
3. Delete the group to replay from the beginning:

```powershell
REM Stop all consumers in the group first, then:
%KAFKA_HOME%\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --delete --group lab1-group
```

Run consumer again with `auto.offset.reset=earliest` to read from the start.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `Connection refused` | Start broker (Lab 00) |
| No messages | Run `scripts\seed-demo-topic.bat` |
| `UnknownTopicOrPartition` | Create `demo-topic` (Lab 00) |
| Consumer exits immediately | Check broker logs; verify topic has data |

---

## What you learned

- `KafkaConsumer` + `subscribe()` + `poll()` loop
- Partition assignment for a consumer group
- Offset progression and `kafka-consumer-groups --describe`

---

## Next lab

→ [Lab 02 - Consumer Groups](../lab-02-consumer-groups/README.md)
