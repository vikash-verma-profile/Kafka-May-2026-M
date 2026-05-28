# Lab 08 — Tune a Slow Connector

**Objective:** Diagnose a lagging JDBC source and apply `tasks.max`, batch, and producer tuning.

From **Kafka_Connect_API.pptx** — Slide 45.

**Tested with:** Java 17, Kafka 4.2, MySQL JDBC source on Windows.

---

## Prerequisites

- Lab 02 + Lab 06 (JDBC source running, load script used)
- Baseline: growing lag on `mysql-orders` or slow poll rate
- Tuned config: [configs/jdbc-source-tuned.json](../configs/jdbc-source-tuned.json)

---

## Scenario

Connector reads **200 GB/day** with:

- `tasks.max=1`
- Default batch sizes
- Single Connect worker

Lag: **hours** behind real time.

---

## Step 0 — Baseline connector

Check current connector (Lab 02 default):

```powershell
Invoke-RestMethod http://localhost:8083/connectors/mysql-orders-source/status
```

Optional: run load while measuring:

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs\scripts
.\load-orders.ps1 -Count 1000
```

---

## Step 1 — Capture baseline metrics

Note before tuning:

| Metric | Your value |
| ------ | ---------- |
| Connector state | |
| Task count | |
| Messages on `mysql-orders` (rough) | |
| Connect log errors | |

```powershell
.\scripts\connect-status.bat mysql-orders-source http://localhost:8083
```

JMX / REST metrics (if exposed):

- `source-record-poll-rate`
- `source-record-write-rate`

---

## Step 2 — Apply tuning knobs (in order)

### Knob 1 — Parallelism (try first)

```properties
tasks.max=8
```

Relevant for **multiple tables** or splittable work. Single `orders` table may still use 1 task — measure anyway.

### Knob 2 — JDBC batch size

```properties
batch.max.rows=2000
poll.interval.ms=2000
```

### Knob 3 — Producer overrides

```properties
producer.override.batch.size=65536
producer.override.linger.ms=20
producer.override.compression.type=lz4
```

---

## Step 3 — Deploy tuned connector

Use [jdbc-source-tuned.json](../configs/jdbc-source-tuned.json) (`mysql-orders-source-tuned`):

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs

curl.exe -X DELETE http://localhost:8083/connectors/mysql-orders-source
curl.exe -X DELETE http://localhost:8083/connectors/mysql-orders-source-tuned

.\scripts\deploy-connector.bat .\configs\jdbc-source-tuned.json http://localhost:8083
.\scripts\connect-status.bat mysql-orders-source-tuned http://localhost:8083
```

Or **PUT** config on existing connector (advanced):

```powershell
# GET current config, merge tuning fields, PUT to /connectors/{name}/config
```

---

## Step 4 — Measure impact

| Metric | Before | After |
| ------ | ------ | ----- |
| poll-rate | | |
| Lag (min) | | |
| CPU on worker | | |

Re-run `load-orders.ps1` and consume `mysql-orders`:

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic mysql-orders --from-beginning --max-messages 10
```

---

## Discussion answers

### Which knob first?

**`tasks.max`** — cheapest win if source can parallelize. Measure `source-record-poll-rate`.

### Second worker vs more tasks?

Add a **second Connect worker** when:

- CPU saturated on first worker
- `tasks.max` already optimal but CPU-bound converters/SMTs

More tasks on one JVM won't help CPU-bound single-threaded work.

### Compression trade-off

`lz4` reduces network/disk; consumers spend CPU decompressing — usually net positive for Kafka.

---

## Checkpoint

- [ ] Documented before/after metrics
- [ ] At least two knobs applied
- [ ] Can justify order of changes

---

## Troubleshooting

| Issue | Fix |
| ----- | --- |
| No improvement | Single table may not use extra tasks; try batch + producer knobs |
| Connector FAILED after tune | Invalid config key — check Connect logs |
| Duplicate data | Expected with at-least-once; idempotent consumers or upsert sink |

---

## Production reference (slide 44)

- Scale `tasks.max` to partitionable units
- Right-size worker heap for converter buffers
- Monitor offset commit lag

---

## Related

- [Lab 02 — JDBC source](../lab-02-postgresql-jdbc-source/README.md)
- [Lab 06 — Load test](../lab-06-stream-db-changes-cdc/README.md)
