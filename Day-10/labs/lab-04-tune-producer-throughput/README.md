# Lab 04 — Tune Producer for Throughput

**Objective:** Achieve ~3–5× producer throughput over Lab 03 baseline using batching, compression, and idempotence.

From **Kafka_cap.pptx** — Slide 19.

---

## Implementation

| Track | Command |
|-------|---------|
| **Shell** | [scripts/run-producer-tuned.bat](../scripts/run-producer-tuned.bat) |
| **Python** | `python lab04_tuned_throughput.py` in [python-production-lab](../../python-production-lab/) |

---

## Prerequisites

- [Lab 03](../lab-03-baseline-throughput/README.md) baseline recorded
- Topic `bench` with data (or recreate)

---

## Step 1 — Batching tuning

```bat
bin\windows\kafka-producer-perf-test.bat --topic bench ^
  --num-records 1000000 --record-size 1024 --throughput -1 ^
  --producer-props bootstrap.servers=localhost:9092 batch.size=65536 linger.ms=20
```

Record MB/sec vs baseline.

---

## Step 2 — Add LZ4 compression

```bat
--producer-props bootstrap.servers=localhost:9092 batch.size=65536 linger.ms=20 compression.type=lz4
```

---

## Step 3 — Idempotence + in-flight

```bat
--producer-props bootstrap.servers=localhost:9092 batch.size=65536 linger.ms=20 compression.type=lz4 max.in.flight.requests.per.connection=5 enable.idempotence=true acks=all
```

---

## Step 4 — Compare results

| Config stage | MB/sec | p99 latency | vs baseline |
|--------------|--------|-------------|-------------|
| Baseline | | | 1.0× |
| + batch/linger | | | |
| + lz4 | | | |
| + idempotent | | | |

**Expected:** ~3–5× throughput; latency may rise slightly with higher `linger.ms`.

---

## Best practices (slide 18)

```properties
acks=all
compression.type=snappy
linger.ms=5
batch.size=65536
enable.idempotence=true
```

Use `snappy` or `lz4` based on CPU vs network bottleneck.

---

## Checkpoint

- [ ] Throughput improved measurably
- [ ] Documented trade-off (latency vs throughput)
- [ ] `acks=all` + idempotence still enabled in final config
