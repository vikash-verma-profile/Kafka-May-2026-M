# Lab 02 - Demonstrate Consumer Groups

**Objective:** Run multiple consumers in the same group, observe rebalancing, idle standby consumers, and independent consumption with a second group.

Based on **Kafka_Consumers.pptx** - Lab 2.

---

## Prerequisites

- [Lab 00](../lab-00-initial-setup/README.md) and [Lab 01](../lab-01-build-kafka-consumer/README.md)
- `demo-topic` with 3 partitions and messages seeded
- Three free terminals for consumers (plus broker terminal)

---

## Configuration for this lab

| Setting | Value |
|---------|-------|
| Topic | `demo-topic` |
| Partitions | 3 |
| Primary group | `lab-group` |
| Second group | `second-group` |

Use `NamedConsumer` so logs show **consumer name** and **partition assignment** on every rebalance.

---

## Step 1 - Start Consumer-1 (all partitions)

**Terminal A:**

```powershell
cd C:\Users\om\Desktop\KafKa\Day-4\labs
run-java-consumer.bat com.kafka.consumer.lab.NamedConsumer lab-group Consumer-1
```

**Expected:**

```text
[Consumer-1] Assigned partitions: [demo-topic-0, demo-topic-1, demo-topic-2]
```

Consumer-1 alone owns all three partitions.

---

## Step 2 - Add Consumer-2 (rebalance)

**Terminal B** (same `group.id`):

```powershell
run-java-consumer.bat com.kafka.consumer.lab.NamedConsumer lab-group Consumer-2
```

**Watch both terminals.** After rebalancing, partitions split, for example:

| Consumer | Partitions (example) |
|----------|----------------------|
| Consumer-1 | P0, P1 |
| Consumer-2 | P2 |

Exact split depends on the assignor; the important point is **no partition is read by two consumers in the same group**.

---

## Step 3 - Add Consumer-3 (one partition each)

**Terminal C:**

```powershell
run-java-consumer.bat com.kafka.consumer.lab.NamedConsumer lab-group Consumer-3
```

**Expected:** each of the three consumers is assigned **exactly one** partition - maximum parallelism for 3 partitions.

---

## Step 4 - Add Consumer-4 (standby / idle)

**Terminal D:**

```powershell
run-java-consumer.bat com.kafka.consumer.lab.NamedConsumer lab-group Consumer-4
```

**Expected:**

```text
[Consumer-4] Assigned partitions: []
```

Consumer-4 joins the group but receives **no partitions** because partition count (3) is less than consumer count (4). It stays idle as a **standby** until another consumer leaves.

---

## Step 5 - Remove a consumer (failover)

1. Stop **Consumer-2** (`Ctrl+C` in Terminal B).
2. Watch Consumer-1, Consumer-3, and Consumer-4 - a **rebalance** occurs.
3. Consumer-4 may receive the partition(s) that Consumer-2 had.
4. Note the delay - related to `session.timeout.ms` (default ~45s) if the process is killed abruptly; graceful `close()` is faster.

**CLI check (Terminal 2):**

```powershell
scripts\describe-consumer-group.bat lab-group
```

You should see active members and LAG per partition.

---

## Step 6 - Independent consumer group

Start a consumer with a **different** `group.id` - it reads the **entire topic independently** (its own offsets).

**Terminal E:**

```powershell
run-java-consumer.bat com.kafka.consumer.lab.NamedConsumer second-group Independent-1
```

**Expected:** `second-group` receives **all** partitions (alone) and processes **all messages** from the beginning (if no prior commits), regardless of `lab-group` progress.

Verify two groups:

```powershell
%KAFKA_HOME%\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --list
scripts\describe-consumer-group.bat lab-group
scripts\describe-consumer-group.bat second-group
```

Each group maintains separate entries in `__consumer_offsets`.

---

## Python equivalent (optional)

```powershell
cd labs\python-kafka-consumer-lab
python named_consumer.py lab-group Consumer-1
```

Open additional terminals with `Consumer-2`, `Consumer-3`, etc.

---

## Step 7 - Produce load during rebalance (optional)

While multiple consumers run, produce messages:

```powershell
scripts\seed-demo-topic.bat
```

Observe that only the consumer **assigned** to a partition processes messages for that partition.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Rebalance never happens | Ensure same `group.id` and topic subscription |
| All consumers idle | Check broker; verify topic exists |
| Long delay after kill | Normal with hard kill - wait for session timeout or use graceful shutdown |

---

## What you learned

- One partition → at most one consumer per group
- Adding/removing members triggers **rebalancing**
- Extra consumers beyond partition count stay **idle**
- Different `group.id` → independent offset tracking

---

## Next lab

→ [Lab 03 - Manual vs Auto Commit](../lab-03-manual-vs-auto-commit/README.md)
