# Lab 05 — Tune Consumer Fetch & Parallelism

**Objective:** Scale a consumer group to match partitions and tune fetch settings until lag stays near zero.

From **Kafka_cap.pptx** — Slide 22.

---

## Prerequisites

- Topic `bench` with **6+ partitions**
- Continuous load (run producer perf test in background)
- **Using Strimzi (Lab 02)?** Use `localhost:19092` instead of `localhost:9092`. See [Lab 02](../lab-02-kubernetes-strimzi/README.md).

---

## Step 1 — Start single consumer

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 ^
  --topic bench --group bench-cg
```

---

## Step 2 — Check lag

```bat
bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 ^
  --describe --group bench-cg
```

Note **LAG** column per partition.

---

## Step 3 — Scale to 3 consumers

Open **3 terminals**, same `--group bench-cg`. Kafka assigns one partition per consumer (up to partition count).

Re-run `--describe` — lag should drop.

---

## Step 4 — Tune fetch (programmatic or consumer props)

For Java consumer:

```properties
fetch.min.bytes=65536
fetch.max.wait.ms=50
max.poll.records=500
enable.auto.commit=false
```

Console consumer has limited tuning — use a small `ConsumerBench` Java app or `kafka-consumer-perf-test.sh` with:

```bat
--consumer-props fetch.min.bytes=65536 fetch.max.wait.ms=50
```

---

## Step 5 — Target steady-state

With continuous producer load, goal:

```text
GROUP  TOPIC  PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG = 0
```

---

## Checkpoint

- [ ] Lag measured with 1 vs 3 consumers
- [ ] Fetch settings documented
- [ ] Steady-state LAG ≈ 0 under load

---

## Best practices (slide 21)

- `fetch.min.bytes` — batch fetches for throughput
- `max.poll.records` — balance processing time vs poll interval
- Cooperative rebalancing for large groups
