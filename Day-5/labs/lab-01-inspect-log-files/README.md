# Lab 01 - Inspect Kafka Log Files

**Objective:** Find partition directories on disk, list segment files, and dump `.log`, `.index`, and `.timeindex` with `kafka-dump-log`.

From **Kafka_Storage_Internals_Replication.pptx**-Slide 8.

---

## Prerequisites

- [Lab 00](../lab-00-initial-setup/README.md)-single broker on `localhost:9092`
- `KAFKA_HOME` set
- Know your `log.dirs` path (default: `C:\kafka-data\kraft-combined-logs`)

---

## Concepts (from slides)

| File | Role |
|------|------|
| `*.log` | Append-only message data |
| `*.index` | Sparse map: offset → byte position in `.log` |
| `*.timeindex` | Timestamp → offset |
| Naming | Base offset in filename, e.g. `00000000000000000000.log` |
| Active segment | Receives writes; sealed segments are immutable |

---

## Step 1 - Create a topic and produce messages

**Terminal 2:**

```bat
cd C:\Users\om\Desktop\KafKa\Day-5\labs
scripts\create-storage-demo-topic.bat
```

Or manually:

```bat
cd %KAFKA_HOME%
bin\windows\kafka-topics.bat --create --topic storage-demo --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic storage-demo
```

Type a few lines (`order-1`, `order-2`, …), then `Ctrl+C`.

---

## Step 2 - Locate the partition directory

Open File Explorer or PowerShell:

```powershell
cd C:\kafka-data\kraft-combined-logs
dir storage-demo-0
```

You should see files similar to:

```text
00000000000000000000.log
00000000000000000000.index
00000000000000000000.timeindex
leader-epoch-checkpoint
partition.metadata
```

> Folder name is `<topic>-<partition>`, e.g. `storage-demo-0`.

---

## Step 3 - List segment files

```powershell
cd C:\kafka-data\kraft-combined-logs\storage-demo-0
dir *.log, *.index, *.timeindex
```

Note the **base offset** in each filename (usually `0` for a new topic).

---

## Step 4 - Dump the log segment

```bat
cd %KAFKA_HOME%
bin\windows\kafka-dump-log.bat --files C:\kafka-data\kraft-combined-logs\storage-demo-0\00000000000000000000.log --print-data-log
```

Scroll the first ~20 records. Each line shows offset, timestamp, key, value, and size.

To limit output in PowerShell:

```powershell
bin\windows\kafka-dump-log.bat --files C:\kafka-data\kraft-combined-logs\storage-demo-0\00000000000000000000.log --print-data-log | Select-Object -First 25
```

---

## Step 5 - Inspect the offset index

```bat
bin\windows\kafka-dump-log.bat --files C:\kafka-data\kraft-combined-logs\storage-demo-0\00000000000000000000.index
```

Entries look like: relative offset + physical position in the `.log` file.

---

## Step 6 - Inspect the time index

```bat
bin\windows\kafka-dump-log.bat --files C:\kafka-data\kraft-combined-logs\storage-demo-0\00000000000000000000.timeindex
```

Maps timestamps to offsets (used for time-based consumption).

---

## Step 7 - Cross-check index vs log

1. Pick an offset from the log dump (e.g. offset `3`).
2. Find the nearest entry in the index dump.
3. Confirm the index points to the correct region in the `.log` (same key/value at that offset).

This is how consumers avoid scanning the entire file from byte 0.

---

## Challenge

With only a few messages, the index may have one entry. Produce more data (Lab 02 uses 10k records) and compare:

- Index entry count vs total records
- Why sparse indexing (`log.index.interval.bytes`, default 4096) saves space

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| No `storage-demo-0` folder | Produce at least one message after creating the topic |
| Wrong `log.dirs` | Check `%KAFKA_HOME%\config\server.properties` |
| Empty `.log` | Broker not running or topic name typo |

---

## What you learned

- On-disk layout for one partition
- Three file types per segment
- `kafka-dump-log` for offline inspection

---

## Next lab

→ [Lab 02 - Index and Offset Lookup](../lab-02-index-offset-lookup/README.md)
