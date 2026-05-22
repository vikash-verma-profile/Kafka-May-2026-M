# Lab 02 - Explore Index and Offset Lookup

**Objective:** Load a partition with many records, dump sparse indexes, and see how offset → byte position mapping works.

From **Kafka_Storage_Internals_Replication.pptx** — Slide 10.

---

## Prerequisites

- [Lab 00](../lab-00-initial-setup/README.md) — single broker
- [Lab 01](../lab-01-inspect-log-files/README.md) recommended (same tools)

---

## Concepts

| Setting | Default | Effect |
|---------|---------|--------|
| `log.index.interval.bytes` | 4096 | New index entry about every 4 KB of log data |
| Sparse index | — | Not every offset is indexed; binary search + sequential scan |
| `kafka-producer-perf-test` | — | Fast way to load test data |

---

## Step 1 - Create topic `index-lab`

```bat
cd C:\Users\om\Desktop\KafKa\Day-5\labs
scripts\create-index-lab-topic.bat
```

Manual:

```bat
cd %KAFKA_HOME%
bin\windows\kafka-topics.bat --create --topic index-lab --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

---

## Step 2 - Produce 10,000 messages

```bat
cd C:\Users\om\Desktop\KafKa\Day-5\labs
scripts\produce-index-load.bat
```

Manual (adjust path if needed):

```bat
cd %KAFKA_HOME%
bin\windows\kafka-producer-perf-test.bat --topic index-lab --num-records 10000 --record-size 100 --throughput -1 --producer-props bootstrap.servers=localhost:9092
```

Wait until perf test reports completion (~few seconds).

---

## Step 3 - Locate partition directory

```powershell
dir C:\kafka-data\kraft-combined-logs\index-lab-0
```

You may see one or more segment triplets (`00000000000000000000.*`, etc.) depending on size.

---

## Step 4 - Dump offset index

```bat
cd %KAFKA_HOME%
bin\windows\kafka-dump-log.bat --files C:\kafka-data\kraft-combined-logs\index-lab-0\00000000000000000000.index
```

Count how many index entries appear vs 10,000 records.

---

## Step 5 - Dump time index

```bat
bin\windows\kafka-dump-log.bat --files C:\kafka-data\kraft-combined-logs\index-lab-0\00000000000000000000.timeindex
```

---

## Step 6 - Dump log (sample)

```bat
bin\windows\kafka-dump-log.bat --files C:\kafka-data\kraft-combined-logs\index-lab-0\00000000000000000000.log --print-data-log | more
```

Compare:

- Offset in log at index entry N
- Physical position from index
- Next index entry’s offset gap

---

## Step 7 - Explain sparse indexing

Answer for yourself (or lab discussion):

1. **Average gap** between indexed offsets ≈ `(last offset) / (index entries - 1)`.
2. **Why sparse?** Full index per record would bloat disk and RAM; 4 KB intervals keep indexes small while lookup stays O(log n) + short scan.
3. **Consumer fetch:** Broker binary-searches `.index`, then reads sequentially from `.log`.

---

## Optional - Force segment roll

To see multiple segments on disk:

```bat
bin\windows\kafka-configs.bat --bootstrap-server localhost:9092 --entity-type topics --entity-name index-lab --alter --add-config segment.ms=10000
```

Produce more messages, wait 10+ seconds, list `index-lab-0` again — new `00000000000xxxxxxx.log` files may appear.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Perf test fails | Broker down; topic missing |
| Still one tiny index | 10k × 100 bytes may fit one segment; index still sparse inside segment |
| `UnknownTopicOrPartition` | Create `index-lab` first |

---

## What you learned

- High-volume produce for storage testing
- Sparse `.index` / `.timeindex` behavior
- Trade-off between index size and lookup speed

---

## Next lab

→ [Lab 03 - Broker Failure Simulation](../lab-03-broker-failure-simulation/README.md) (requires 3 brokers)
