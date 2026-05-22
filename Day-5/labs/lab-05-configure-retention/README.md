# Lab 05 - Configure Retention Policies

**Objective:** Create a topic with short time-based retention, produce data, roll segments, and observe old segments deleted.

From **Kafka_Storage_Internals_Replication.pptx** — Slide 25.

---

## Prerequisites

- Single broker on `localhost:9092`
- `log.dirs` e.g. `C:\kafka-data\kraft-combined-logs`

---

## Concepts

| Policy | Config | Behavior |
|--------|--------|----------|
| Time | `retention.ms` | Delete sealed segments older than threshold |
| Size | `retention.bytes` | Delete oldest segments when partition exceeds size |
| Active segment | — | Never deleted by retention |

Default cluster retention is often 7 days (`log.retention.hours=168`). This lab uses **60 seconds**.

---

## Step 1 - Create topic with 60s retention

```bat
cd C:\Users\om\Desktop\KafKa\Day-5\labs
scripts\create-retention-lab-topic.bat
```

Manual:

```bat
cd %KAFKA_HOME%
bin\windows\kafka-topics.bat --create --topic retention-lab --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --config retention.ms=60000
```

---

## Step 2 - Produce messages

```bat
scripts\seed-retention-lab.bat
```

Or console producer (~50+ lines):

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic retention-lab
```

---

## Step 3 - List segment files on disk

```powershell
dir C:\kafka-data\kraft-combined-logs\retention-lab-0\*.log
```

Note filenames and file sizes.

---

## Step 4 - Force segment roll (optional but faster)

```bat
cd %KAFKA_HOME%
bin\windows\kafka-configs.bat --bootstrap-server localhost:9092 --entity-type topics --entity-name retention-lab --alter --add-config segment.ms=10000
```

Produce a few more messages, wait **15+ seconds**, list `.log` files again — you may see a second segment (`00000000000000001234.log` style name).

---

## Step 5 - Wait for retention

1. Stop producing.
2. Wait **at least 70 seconds** (60s retention + cleaner interval).
3. Retention checker runs on `log.retention.check.interval.ms` (default 5 min) — for quicker results, temporarily lower it on the topic or broker, or restart broker after wait.

**Faster check** — alter broker log cleaner (session only, dev lab):

```bat
bin\windows\kafka-configs.bat --bootstrap-server localhost:9092 --entity-type brokers --entity-name 1 --alter --add-config log.retention.check.interval.ms=10000
```

> Broker id `1` is typical for single-node KRaft; use `kafka-broker-api-versions` / logs if yours differs.

---

## Step 6 - Observe deletion

```powershell
dir C:\kafka-data\kraft-combined-logs\retention-lab-0
```

Old sealed segments should disappear; the **active** segment remains.

---

## Step 7 - Verify topic config

```bat
cd C:\Users\om\Desktop\KafKa\Day-5\labs
scripts\describe-retention-lab.bat
```

```bat
bin\windows\kafka-configs.bat --bootstrap-server localhost:9092 --entity-type topics --entity-name retention-lab --describe
```

Confirm `retention.ms=60000`.

---

## Bonus - Size-based retention

Create a second topic:

```bat
bin\windows\kafka-topics.bat --create --topic retention-bytes-lab --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --config retention.bytes=1024
```

Produce large messages until partition size exceeds 1 KB; oldest segments trim first.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Nothing deleted | Wait longer; force segment roll; check only sealed segments delete |
| All data gone | Active segment still has recent data; consume with `--from-beginning` |
| Config not applied | `--describe` on topic configs |

---

## What you learned

- Topic-level `retention.ms` override
- Segment roll vs retention delete
- Active segment protection

---

## Next lab

→ [Lab 06 - Enable Log Compaction](../lab-06-enable-log-compaction/README.md)
