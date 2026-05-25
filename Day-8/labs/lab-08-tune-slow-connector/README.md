# Lab 08-Tune a Slow Connector

**Objective:** Diagnose a lagging JDBC source and apply `tasks.max`, batch, and producer tuning.

From **Kafka_Connect_API.pptx**-Slide 45.

---

## Prerequisites

- JDBC source connector (Lab 02 or similar)
- Baseline lag metric (records behind or growing consumer lag on output topic)

---

## Scenario

Connector reads **200 GB/day** with:

- `tasks.max=1`
- Default batch sizes
- Single Connect worker

Lag: **hours** behind real time.

---

## Step 1-Capture baseline

```bash
curl -s http://localhost:8083/connectors/postgres-orders-source/status | jq
```

Note JMX or Connect REST metrics if available:

- `source-record-poll-rate`
- `source-record-write-rate`

---

## Step 2-Apply tuning knobs (in order)

### Knob 1-Parallelism (try first)

```properties
tasks.max=8
```

Match to **splittable source** (multiple tables/partitions). Re-deploy connector.

**Measure:** poll rate should increase if source allows parallel polls.

### Knob 2-JDBC batch size

```properties
batch.max.rows=2000
poll.interval.ms=2000
```

### Knob 3-Producer overrides

```properties
producer.override.batch.size=65536
producer.override.linger.ms=20
producer.override.compression.type=lz4
```

---

## Step 3-Full tuned config excerpt

```json
{
  "tasks.max": "8",
  "batch.max.rows": "2000",
  "poll.interval.ms": "2000",
  "producer.override.batch.size": "65536",
  "producer.override.linger.ms": "20",
  "producer.override.compression.type": "lz4"
}
```

Update via PUT `/connectors/{name}/config`.

---

## Step 4-Measure impact

| Metric | Before | After |
|--------|--------|-------|
| poll-rate | | |
| Lag (min) | | |
| CPU on worker | | |

---

## Discussion answers

### Which knob first?

**`tasks.max`**-cheapest win if source can parallelize. Measure `source-record-poll-rate`.

### Second worker vs more tasks?

Add **second worker** when:

- CPU saturated on first worker
- `tasks.max` already optimal but CPU-bound transforms/converters

More tasks on one JVM won't help CPU-bound single-threaded work.

### Compression trade-off

`lz4` reduces network/disk; consumers spend CPU decompressing-usually net positive for Kafka.

---

## Checkpoint

- [ ] Documented before/after metrics
- [ ] At least two knobs applied
- [ ] Can justify order of changes

---

## Production reference (slide 44)

- Scale `tasks.max` to partitionable units
- Right-size worker heap for converter buffers
- Monitor offset commit lag
