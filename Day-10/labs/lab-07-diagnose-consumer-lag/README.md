# Lab 07 — Diagnose Consumer Lag

**Objective:** Find the slowest partition, reset offsets, and recover consumer group lag.

From **Kafka_cap.pptx** — Slide 29.

---

## Implementation

| Track | Command |
|-------|---------|
| **Shell** | [scripts/reset-consumer-offsets-latest.bat](../scripts/reset-consumer-offsets-latest.bat) |
| **Python** | `python lab07_consumer_lag.py localhost:9092 order-processor` |

---

## Prerequisites

- Consumer group `order-processor` consuming topic `orders`
- **Using Strimzi (Lab 02)?** Use `localhost:19092` instead of `localhost:9092`. See [Lab 02](../lab-02-kubernetes-strimzi/README.md).
- Producer load generator running (perf test or script)

---

## Step 1 — Show group lag

```bat
bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 ^
  --describe --group order-processor
```

Identify partition with **highest LAG**.

---

## Step 2 — Stop slow consumer

Stop one consumer instance to let lag grow (for demo), or throttle processing in code (`Thread.sleep`).

---

## Step 3 — Reset offsets to latest (lab only)

> **Warning:** Skips unread messages — use only in lab or after explicit approval.

```bat
bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 ^
  --group order-processor --topic orders ^
  --reset-offsets --to-latest --execute
```

Dry-run first:

```bat
--reset-offsets --to-latest --dry-run
```

---

## Step 4 — Confirm recovery

```bat
bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 ^
  --describe --group order-processor
```

**Expected:** LAG drops to near **0** within seconds.

---

## Step 5 — Production alternatives

Instead of reset:

- Scale consumers (Lab 05)
- Fix slow processing logic
- Increase partitions + consumers

---

## Checkpoint

- [ ] Identified highest-lag partition
- [ ] Reset executed (dry-run shown)
- [ ] LAG < 100 on all partitions

---

## Root causes (slide 28)

Slow consumer logic, too few consumers, GC pauses, fetch too small, hot partition.
