# Lab 06 — Tune Log Segment and Retention

**Objective:** Set per-topic segment size and retention; verify on disk.

From **Kafka_cap.pptx** — Slide 25.

---

## Prerequisites

- Running cluster from Lab 01
- Know `log.dirs` path (e.g. `C:\kafka-data\kraft-combined-logs`)
- **Using Strimzi (Lab 02)?** Use `localhost:19092` instead of `localhost:9092`. Disk inspection steps apply to local brokers only.

---

## Step 1 — Create topic `orders`

```bat
bin\windows\kafka-topics.bat --create --topic orders ^
  --bootstrap-server localhost:9092 --partitions 6 --replication-factor 1
```

---

## Step 2 — Set segment size (128 MB)

```bat
bin\windows\kafka-configs.bat --bootstrap-server localhost:9092 ^
  --entity-type topics --entity-name orders ^
  --alter --add-config segment.bytes=134217728
```

---

## Step 3 — Set 7-day retention

```bat
bin\windows\kafka-configs.bat --bootstrap-server localhost:9092 ^
  --entity-type topics --entity-name orders ^
  --alter --add-config retention.ms=604800000
```

---

## Step 4 — Verify config

```bat
bin\windows\kafka-configs.bat --bootstrap-server localhost:9092 ^
  --entity-type topics --entity-name orders --describe
```

**Expected:**

```text
segment.bytes=134217728 sensitive=false
retention.ms=604800000 sensitive=false
```

---

## Step 5 — Inspect segments on disk

Produce enough data to roll segments (or lower segment size for demo):

```powershell
cd C:\kafka-data\kraft-combined-logs\orders-0
dir *.log
```

**Expected:** segment files named with base offsets; older segments deleted after retention.

---

## Checkpoint

- [ ] Config describe shows both settings
- [ ] Segment files visible under `orders-0`
- [ ] Understand retention vs compaction (Day 5)

---

## Storage tips (slide 24)

- SSD for `log.dirs`
- Separate OS disk from Kafka data disk in production
