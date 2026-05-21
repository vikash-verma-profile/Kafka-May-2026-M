# Lab 03 - Manual vs Auto Commit

**Objective:** Compare `enable.auto.commit`, `commitSync()`, and `commitAsync()` by processing messages, crashing mid-run, and counting duplicates after restart.

Based on **Kafka_Consumers.pptx** - Lab 3.

---

## Prerequisites

- Labs 00–02 completed
- Fresh or reset consumer group offsets for fair comparison (see Step 0)

---

## Step 0 - Reset offsets (recommended)

Stop all consumers in `commit-lab-group`, then reset to earliest:

```powershell
%KAFKA_HOME%\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --group commit-lab-group --reset-offsets --to-earliest --topic demo-topic --execute
```

If the group is active, stop consumers first or use `--delete` and recreate.

Re-seed messages if needed:

```powershell
scripts\seed-demo-topic.bat
```

---

## Step 1 - Auto commit test

**Class:** `AutoCommitConsumer`  
**Settings:** `enable.auto.commit=true`, `auto.commit.interval.ms=5000`

```powershell
cd C:\Users\om\Desktop\KafKa\Day-4\labs
run-java-consumer.bat com.kafka.consumer.lab.AutoCommitConsumer commit-lab-group
```

1. Let it process messages for ~10 seconds.
2. **Kill abruptly** (`Ctrl+C` or close terminal) **without** graceful shutdown.
3. Restart the same consumer.

**Observe:** some messages may be **processed but not yet committed** (5s interval), so they can be **delivered again** after restart → **at-least-once** with possible duplicates.

---

## Step 2 - Disable auto commit (no commit calls)

**Class:** `NoCommitConsumer` (processes only, never commits)

```powershell
run-java-consumer.bat com.kafka.consumer.lab.NoCommitConsumer commit-lab-group
```

1. Process messages for a while.
2. Stop and restart.

**Observe:** on restart, Kafka uses the **last committed offset** (often 0 or an old value) → **full reprocessing** of uncommitted data.

---

## Step 3 - Manual `commitSync()` test

Reset offsets (Step 0), then:

```powershell
run-java-consumer.bat com.kafka.consumer.lab.ManualSyncCommitConsumer commit-lab-group
```

This consumer calls `commitSync()` **after each poll batch** is processed.

1. Run until ~50 messages are logged.
2. Kill abruptly mid-run.
3. Restart and count how many messages are **reprocessed**.

**Observe:** typically **fewer duplicates** than auto-commit, at the cost of higher commit latency.

---

## Step 4 - Manual `commitAsync()` test

Reset offsets, then:

```powershell
run-java-consumer.bat com.kafka.consumer.lab.ManualAsyncCommitConsumer commit-lab-group
```

Uses `commitAsync()` with a callback that logs commit errors.

1. Run and kill abruptly as in Step 3.
2. Compare duplicate count vs sync commit.

**Observe:** faster throughput, but a crash **before** async commit completes can lose the last batch’s offset → more duplicates than sync.

---

## Step 5 - Controlled duplicate scenario (100 messages)

### 5a - Produce exactly 100 messages

```powershell
cd C:\Users\om\Desktop\KafKa\Day-4\labs\java-kafka-consumer-lab
mvn -q exec:java -Dexec.mainClass=com.kafka.consumer.lab.CommitLabProducer
```

This sends 100 numbered messages to `demo-topic` with keys.

### 5b - Run sync consumer with a counter

```powershell
cd C:\Users\om\Desktop\KafKa\Day-4\labs
run-java-consumer.bat com.kafka.consumer.lab.ManualSyncCommitConsumer commit-lab-group
```

Stop after the log shows ~50 messages processed, then restart.

**Record:** how many of the 100 messages were seen **more than once** (check offset/value in logs).

Repeat with `AutoCommitConsumer` and `ManualAsyncCommitConsumer` after resetting offsets.

---

## Step 6 - Document trade-offs

Fill in your observations:

| Strategy | Pros | Cons |
|----------|------|------|
| Auto commit | Simple, no code | Duplicates on crash; commit timing not tied to processing |
| No commit | Shows offset behavior | Never advances offset |
| `commitSync()` | Stronger guarantee before continuing | Slower (blocks on broker) |
| `commitAsync()` | Higher throughput | May lose last commit on crash |

**Best practice (from slides):** process → then commit; design **idempotent** handlers for duplicates.

---

## Python equivalents

```powershell
cd labs\python-kafka-consumer-lab
python auto_commit_consumer.py commit-lab-group
python manual_sync_consumer.py commit-lab-group
python manual_async_consumer.py commit-lab-group
```

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| No duplicates seen | Kill faster during processing; reduce `auto.commit.interval.ms` for auto test |
| Reset fails | Stop all consumers in the group first |
| Same messages every time | Delete group: `--delete --group commit-lab-group` |

---

## What you learned

- Offsets live in `__consumer_offsets`
- Commit strategy controls **duplicates vs performance**
- Production systems often use **manual commit after successful processing**

---

## Next lab

→ [Lab 04 - Consumer Lag](../lab-04-consumer-lag/README.md)
