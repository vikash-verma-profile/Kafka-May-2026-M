# Lab 04 - Simulate and Measure Consumer Lag

**Objective:** Create backlog with a slow consumer, measure LAG with CLI, scale out consumers, and document recovery.

Based on **Kafka_Consumers.pptx** - Lab 4.

---

## Prerequisites

- [Lab 00](../lab-00-initial-setup/README.md) - topic `lag-demo` (3 partitions)
- Broker on `localhost:9092` (or your multi-broker list if you run a cluster)
- **`KAFKA_HOME` set** for `watch-lag.bat` and CLI cleanup (see [Lab 00](../lab-00-initial-setup/README.md)):

```powershell
set KAFKA_HOME=C:\kafka-bin\kafka_2.13-4.2.0
```

`produce-lag-load.bat` still works without `KAFKA_HOME` (it falls back to the Java producer).

---

## Concepts

```text
LAG = LOG-END-OFFSET - CURRENT-OFFSET
```

High LAG means the consumer is falling behind the producer.

A consumer group id exists in Kafka only **after** at least one consumer has joined with that `group.id`.

---

## Step 1 - Setup slow consumer

**Terminal A** - one consumer, **500 ms sleep** per message:

```powershell
cd C:\Users\om\Desktop\KafKa\Day-4\labs
run-java-consumer.bat com.kafka.consumer.lab.SlowConsumer lag-group 500
```

Arguments: `group-id` `sleep-ms` (optional `client-label`, default `SlowConsumer`).

**Verify startup** — the first line must show `group=lag-group`:

```text
[SlowConsumer] group=lag-group topic=lag-demo sleep=500ms
```

If you see `group=com.kafka.consumer.lab.SlowConsumer` instead, arguments were not passed correctly. Use the Maven command below or update `run-java-consumer.bat` from the repo (Windows `%*` quirk after `shift`).

**Windows alternative (explicit args):**

```powershell
cd java-kafka-consumer-lab
mvn -q exec:java -Dexec.mainClass=com.kafka.consumer.lab.SlowConsumer "-Dexec.args=lag-group 500"
```

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

This uses `kafka-producer-perf-test` when `KAFKA_HOME` is set, or falls back to a Java producer sending 10,000 records.

**Alternative (Java):**

```powershell
cd java-kafka-consumer-lab
mvn -q exec:java -Dexec.mainClass=com.kafka.consumer.lab.LagLoadProducer
```

Run the producer **once** for the lab (10,000 messages). Running it twice doubles the backlog.

Target rate ~200 msg/sec is simulated by batching; the slow consumer will still fall behind.

---

## Step 3 - Measure lag every 10 seconds

See **[Where to see LAG and what to verify](#where-to-see-lag-and-what-to-verify)** for sample CLI output, column meanings, and the checklist.

**Terminal C** — set `KAFKA_HOME` first, then poll:

```powershell
set KAFKA_HOME=C:\kafka-bin\kafka_2.13-4.2.0
cd C:\Users\om\Desktop\KafKa\Day-4\labs
scripts\watch-lag.bat lag-group
```

Or manually:

```powershell
%KAFKA_HOME%\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --describe --group lag-group
```

If you use a multi-broker cluster, add all brokers (example):

```powershell
%KAFKA_HOME%\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092,localhost:9094,localhost:9095 --describe --group lag-group
```

**Record** per partition:

| Time | Partition | LAG |
|------|-----------|-----|
| T+0s | 0 | ... |
| T+10s | 0 | ... |
| ... | | |

**Expected:** LAG **increases** while production outpaces 500ms/message processing on a single consumer.

---

## Where to see LAG and what to verify

### Where LAG is (and is not)

| Location | Shows LAG? |
|----------|------------|
| **Terminal C** — `kafka-consumer-groups --describe` | **Yes** — this is the official metric |
| **Terminal A** — slow consumer logs (`P=1 offset=460 ...`) | **No** — only processing progress; use CLI for LAG |
| **Terminal B** — producer | **No** — only send count |

LAG is **not** printed by the Java consumer. You measure it only with Step 3 commands.

### Sample output (what success looks like)

After the consumer is running and load is produced, `--describe` should look like this (group name must match your consumer — `lag-group` or whatever startup logged):

```text
GROUP           TOPIC     PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG     CONSUMER-ID  ...
lag-group       lag-demo  0          1200            3500            2300    ...
lag-group       lag-demo  1          460             20001           19541   ...
lag-group       lag-demo  2          596             9999            9403    ...
```

Read the **`LAG` column** (one row per partition of `lag-demo`). Ignore the log4j “Reconfiguration failed” line if it appears — it does not affect the table.

### Column meanings

| Column | Meaning |
|--------|---------|
| **LOG-END-OFFSET** | Newest offset on the partition (how far the producer has written) |
| **CURRENT-OFFSET** | Last offset the consumer **committed** (finished processing) |
| **LAG** | Backlog = `LOG-END-OFFSET − CURRENT-OFFSET` |

Example: partition 1 with `LOG-END-OFFSET=20001` and `CURRENT-OFFSET=460` → **LAG = 19541** (that many messages not yet committed).

### Verification checklist (by step)

| Step | What to verify |
|------|----------------|
| **1 – Consumer** | First line shows `group=lag-group` and `topic=lag-demo`. Consumer terminal stays running. |
| **2 – Producer** | “Sent 10000 messages to lag-demo” (run once unless you want extra backlog). |
| **3 – Describe** | Command succeeds (no `GroupIdNotFoundException`). You see **3 rows** for partitions 0, 1, 2. **LAG > 0** on busy partitions. Re-run every 10s: **LAG increases** while one slow consumer cannot keep up. |
| **4 – Scale out** | Three consumer processes, same group id. After rebalance, each partition has a different consumer in the `CONSUMER-ID` column. |
| **5 – Recovery** | **LAG decreases** over time; eventually **LAG ≈ 0** on all partitions. |

If `--describe --group lag-group` fails but the consumer is running, list groups and use the name you see:

```powershell
%KAFKA_HOME%\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --list
```

Often the wrong id is `com.kafka.consumer.lab.SlowConsumer` when batch args were mis-passed — describe that group until you restart with `group=lag-group`.

### What to copy into your lab table

From each `--describe`, record **LAG** per partition:

| Time | Partition | CURRENT-OFFSET | LOG-END-OFFSET | LAG |
|------|-----------|----------------|----------------|-----|
| T+0s | 0 | 0 | 0 | 0 |
| T+0s | 1 | 460 | 20001 | 19541 |
| T+0s | 2 | 596 | 9999 | 9403 |
| T+10s | 1 | … | … | … (higher = falling behind; lower = catching up) |

### Quick mental check

- **LAG = 0** → caught up on that partition
- **LAG in thousands** → large backlog (expected in this lab)
- **LAG goes up** (Step 3, one consumer) → producer faster than 500 ms/msg processing
- **LAG goes down** (Step 5, three consumers) → scaling is working

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

## Troubleshooting

### `Group lag-group not found` / `GroupIdNotFoundException`

| Check | Action |
|-------|--------|
| Consumer not started | Start Step 1 first; the group is created when the consumer joins. |
| Wrong group id | Confirm startup log shows `group=lag-group`. If the consumer used another id (e.g. the main class name), describe that group instead, or restart with correct args. |
| Consumer stopped | Restart the slow consumer, then run `--describe` again. |

List all groups:

```powershell
%KAFKA_HOME%\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --list
```

### `Set KAFKA_HOME first` from `watch-lag.bat`

```powershell
set KAFKA_HOME=C:\kafka-bin\kafka_2.13-4.2.0
```

Or run `kafka-consumer-groups.bat` from `%KAFKA_HOME%\bin\windows` manually (Step 3).

### Startup shows `group=com.kafka.consumer.lab.SlowConsumer`

On Windows, `run-java-consumer.bat` used to pass the main class as the first program argument. Fix: use the updated `run-java-consumer.bat`, or the Maven `-Dexec.args=lag-group 500` command in Step 1.

### Lag causes (runtime)

| Cause | Mitigation |
|-------|------------|
| Slow processing in `poll` loop | Reduce per-message work; async processing |
| Too few consumers | Add consumers up to partition count |
| Too few partitions | `kafka-topics --alter --partitions` |
| Frequent rebalance | Stable membership; tune `session.timeout.ms` |

### Harmless log noise

- `SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder"` from Java lab apps
- `KAFKA_HOME not set - using Java LagLoadProducer` when using the Java fallback producer

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
