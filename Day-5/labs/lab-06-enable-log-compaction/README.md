# Lab 06 - Enable Log Compaction

**Objective:** Use `cleanup.policy=compact`, produce duplicate keys, trigger compaction, and verify only the latest value per key remains (plus tombstone behavior).

From **Kafka_Storage_Internals_Replication.pptx** — Slide 26.

---

## Prerequisites

- Single broker on `localhost:9092`

---

## Concepts

| Item | Detail |
|------|--------|
| `cleanup.policy=compact` | Keeps latest record per key |
| Tombstone | `key` with **null** value → delete key after `delete.retention.ms` |
| `min.cleanable.dirty.ratio` | How much duplicate data before cleaner runs (lab: `0.01`) |
| Use case | CDC, config snapshots, KTable changelog |

---

## Step 1 - Create compacted topic

```bat
cd C:\Users\om\Desktop\KafKa\Day-5\labs
scripts\create-compact-lab-topic.bat
```

Manual:

```bat
cd %KAFKA_HOME%
bin\windows\kafka-topics.bat --create --topic compact-lab --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --config cleanup.policy=compact
```

---

## Step 2 - Produce duplicate keys

**Terminal A:**

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic compact-lab --property parse.key=true --property key.separator=:
```

Paste these lines one at a time:

```text
A:version-1
B:version-1
A:version-2
C:version-1
B:version-2
A:version-3
```

`Ctrl+C` when done.

---

## Step 3 - Consume from beginning (before compaction)

**Terminal B:**

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic compact-lab --from-beginning --property print.key=true --property key.separator=: --group compact-lab-read-1
```

You should see **6** records including all `A` versions.

Stop consumer (`Ctrl+C`).

---

## Step 4 - Trigger compaction

```bat
bin\windows\kafka-configs.bat --bootstrap-server localhost:9092 --entity-type topics --entity-name compact-lab --alter --add-config min.cleanable.dirty.ratio=0.01
```

Produce a few more duplicate keys to mark segments dirty, then wait **30–60 seconds** for the log cleaner thread.

Optional nudge — produce again:

```text
A:version-3
B:version-2
C:version-1
```

---

## Step 5 - Consume again

New consumer group so you read the full log state:

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic compact-lab --from-beginning --property print.key=true --property key.separator=: --group compact-lab-read-2
```

**Expected after compaction:** one record per key:

```text
A:version-3
B:version-2
C:version-1
```

Compaction is asynchronous — if you still see duplicates, wait and retry with a new group id.

---

## Step 6 - Tombstone for key A

**Producer:**

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic compact-lab --property parse.key=true --property key.separator=:
```

Send null value for key A. In console, use an empty value after the colon:

```text
A:
```

(Just `A:` then Enter — empty value is the tombstone.)

---

## Step 7 - Verify tombstone retention

1. Wait for `delete.retention.ms` (default 24h) **or** lower it for the lab:

```bat
bin\windows\kafka-configs.bat --bootstrap-server localhost:9092 --entity-type topics --entity-name compact-lab --alter --add-config delete.retention.ms=60000
```

2. After compaction cycles (~1–2 min with low dirty ratio), consume again — key `A` should disappear entirely.

---

## Inspect on disk (optional)

```bat
bin\windows\kafka-dump-log.bat --files C:\kafka-data\kraft-combined-logs\compact-lab-0\00000000000000000000.log --print-data-log
```

Compare file before and after compaction (fewer records for duplicate keys).

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Still see all duplicates | Compaction not run yet; lower `min.cleanable.dirty.ratio`; produce more updates |
| Tombstone not removing A | Wait `delete.retention.ms`; ensure null value produced |
| `cleanup.policy` is delete | Recreate topic with `compact` |

---

## What you learned

- Compaction vs time-based delete
- Latest value per key semantics
- Tombstones and `delete.retention.ms`

---

## Next lab

→ [Lab 07 - Replication Monitoring](../lab-07-replication-monitoring/README.md)
