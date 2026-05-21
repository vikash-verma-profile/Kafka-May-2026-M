# Lab 04 - Simulate and Measure Consumer Lag

**Objective:** Create backlog with a slow consumer, measure LAG with CLI, scale out consumers, and document recovery.

Based on **Kafka_Consumers.pptx** - Lab 4.

---

## Prerequisites

- [Lab 00](../lab-00-initial-setup/README.md) - topic `lag-demo` (3 partitions)
- Broker on `localhost:9092`

---

## Concepts

```text
LAG = LOG-END-OFFSET - CURRENT-OFFSET
```

High LAG means the consumer is falling behind the producer.

---

## Step 1 - Setup slow consumer

**Terminal A** - one consumer, **500 ms sleep** per message:

```powershell
cd C:\Users\om\Desktop\KafKa\Day-4\labs
run-java-consumer.bat com.kafka.consumer.lab.SlowConsumer lag-group 500
```

Arguments: `group-id` `sleep-ms` (default 500).

Python:

```powershell
cd labs\python-kafka-consumer-lab
python slow_consumer.py lag-group 500
```

Leave this running.

---

## Step 2 - Produce load (10,000 messages)

**Terminal B:**

```powershell
cd C:\Users\om\Desktop\KafKa\Day-4\labs
scripts\produce-lag-load.bat
```

This uses `kafka-producer-perf-test` when available, or falls back to a Java producer sending 10,000 records.

**Alternative (Java):**

```powershell
cd java-kafka-consumer-lab
mvn -q exec:java -Dexec.mainClass=com.kafka.consumer.lab.LagLoadProducer
```

Target rate ~200 msg/sec is simulated by batching; the slow consumer will still fall behind.

---

## Step 3 - Measure lag every 10 seconds

**Terminal C:**

```powershell
scripts\watch-lag.bat lag-group
```

Or manually:

```powershell
%KAFKA_HOME%\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --describe --group lag-group
```

**Record** per partition:

| Time | Partition | LAG |
|------|-----------|-----|
| T+0s | 0 | ... |
| T+10s | 0 | ... |
| ... | | |

**Expected:** LAG **increases** while production outpaces 500ms/message processing on a single consumer.

---

## Step 4 - Scale out (add consumers)

With **Terminal A** still running, start:

**Terminal D:**

```powershell
run-java-consumer.bat com.kafka.consumer.lab.SlowConsumer lag-group 500 Consumer-2
```

**Terminal E:**

```powershell
run-java-consumer.bat com.kafka.consumer.lab.SlowConsumer lag-group 500 Consumer-3
```

Use `NamedConsumer` variant if you want partition assignment logs:

```powershell
run-java-consumer.bat com.kafka.consumer.lab.SlowConsumer lag-group 500
```

After rebalance, **three consumers** should each own **one partition** of `lag-demo`.

---

## Step 5 - Verify recovery

Continue `watch-lag.bat` (or periodic `--describe`).

**Expected:**

1. LAG may still grow briefly during rebalance.
2. With 3 consumers and 3 partitions, effective processing capacity ≈ **3×** single consumer.
3. LAG should **decrease** over time until near **0** when the backlog is drained.

---

## Step 6 - Document findings

Capture in your notes:

| Metric | Your value |
|--------|------------|
| Peak LAG (per partition) | |
| Time to recover after 3rd consumer joined | |
| Consumer count vs partition count | Must have **partitions ≥ consumers** for full parallelism |

**Relationship:** max useful consumers in a group ≈ **number of partitions** on the subscribed topic.

---

## Troubleshooting lag (reference)

| Cause | Mitigation |
|-------|------------|
| Slow processing in `poll` loop | Reduce per-message work; async processing |
| Too few consumers | Add consumers up to partition count |
| Too few partitions | `kafka-topics --alter --partitions` |
| Frequent rebalance | Stable membership; tune `session.timeout.ms` |

---

## Cleanup

```powershell
%KAFKA_HOME%\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --delete --group lag-group
```

Stop all consumer terminals with `Ctrl+C`.

---

## What you learned

- How to read **LAG** from `kafka-consumer-groups --describe`
- Scaling consumers reduces per-partition backlog
- Partition count caps parallelism

---

## Course complete

Review [labs/README.md](../README.md) and the Q&A slides in **Kafka_Consumers.pptx**.
