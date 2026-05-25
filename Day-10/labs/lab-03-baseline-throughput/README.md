# Lab 03 — Measure Baseline Throughput

**Objective:** Record baseline producer and consumer throughput before tuning.

From **Kafka_cap.pptx** — Slide 16.

---

## Implementation

| Track | Command |
|-------|---------|
| **Shell** | [scripts/run-producer-baseline.bat](../scripts/run-producer-baseline.bat) (`kafka-producer-perf-test`) |
| **Python** | `python lab03_baseline_throughput.py` in [python-production-lab](../../python-production-lab/) |

---

## Prerequisites

- 3-broker cluster (Lab 01 or 02)
- Client host with low latency to brokers

---

## Step 1 — Create benchmark topic

```bat
bin\windows\kafka-topics.bat --create --topic bench ^
  --bootstrap-server localhost:9092 ^
  --partitions 6 --replication-factor 3
```

---

## Step 2 — Producer perf test

```bat
bin\windows\kafka-producer-perf-test.bat --topic bench ^
  --num-records 1000000 --record-size 1024 --throughput -1 ^
  --producer-props bootstrap.servers=localhost:9092
```

Record from output:

- **records/sec**
- **MB/sec**
- **avg latency** and **p99** if shown

**Example:** `85,234 records/sec (83.24 MB/sec), 9.1ms avg latency`

---

## Step 3 — Consumer perf test

```bat
bin\windows\kafka-consumer-perf-test.bat --topic bench ^
  --messages 1000000 --bootstrap-server localhost:9092
```

Record consumer MB/sec.

---

## Step 4 — Save baseline

Create `baseline-results.md` in this folder:

| Metric | Producer | Consumer |
|--------|----------|----------|
| MB/sec | | |
| records/sec | | |
| p99 latency (ms) | | |
| Date / cluster | | |

---

## Checkpoint

- [ ] 1M records produced successfully
- [ ] 1M records consumed
- [ ] Baseline numbers saved for Labs 04–05 comparison

---

## Note

Always tune against a baseline — changes without measurement are guesswork (slide 16).
