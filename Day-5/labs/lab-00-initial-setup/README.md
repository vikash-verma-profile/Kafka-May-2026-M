# Lab 00 - Initial Setup (Day 5 Storage & Replication)

**Objective:** Prepare Kafka for Day 5 labs — single broker for storage/retention labs, and optionally a 3-broker cluster for replication labs.

Complete this once before Lab 01–07.

---

## Prerequisites checklist

| Software | Version | Verify |
|----------|---------|--------|
| Java JDK | 17+ | `java -version` |
| Apache Kafka | 3.x / 4.x | e.g. `C:\kafka-bin\kafka_2.13-4.2.0` |

---

## Step 1 - Locate Kafka

If you completed Day 2/3/4, reuse the same install:

```powershell
cd C:\kafka-bin\kafka_2.13-4.2.0
```

Fresh install: download from [kafka.apache.org](https://kafka.apache.org/downloads), extract, then format KRaft once per [Day-2 KRaft setup](../../Day-2/Labs/kafka-kraft-setup-windows.md) (Part 1).

---

## Step 2 - Set KAFKA_HOME (each new terminal)

**CMD:**

```bat
set KAFKA_HOME=C:\kafka-bin\kafka_2.13-4.2.0
set PATH=%KAFKA_HOME%\bin\windows;%PATH%
```

**PowerShell:**

```powershell
$env:KAFKA_HOME = "C:\kafka-bin\kafka_2.13-4.2.0"
```

Adjust the path if your folder name differs.

---

## Step 3A - Single broker (Labs 01, 02, 05, 06)

**Terminal 1** — keep open:

```bat
cd %KAFKA_HOME%
bin\windows\kafka-server-start.bat config\server.properties
```

Wait for `Kafka Server started`.

**Default log directory** (from `config\server.properties`):

```text
log.dirs=C:\kafka-data\kraft-combined-logs
```

If your install uses a different path, open `config\server.properties` and note `log.dirs` — you need it in Lab 01.

**Terminal 2 — verify:**

```bat
bin\windows\kafka-broker-api-versions.bat --bootstrap-server localhost:9092
```

---

## Step 3B - Three-broker cluster (Labs 03, 04, 07)

Stop the single broker first (close its terminal). Free ports: 9092, 9093, 9094, 9095, 9193, 9293.

### One-time format

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-local-3brokers\scripts
setup-cluster.bat
```

### Start brokers (3 terminals)

```bat
start-broker1.bat
start-broker2.bat
start-broker3.bat
```

Each should log `Kafka Server started`.

### Bootstrap servers for Day 5

```bat
set BS=localhost:9092,localhost:9094,localhost:9095
```

**Log directories:**

| Broker | `log.dirs` |
|--------|------------|
| 1 | `C:\kafka-data\multi-broker-1` |
| 2 | `C:\kafka-data\multi-broker-2` |
| 3 | `C:\kafka-data\multi-broker-3` |

Full guide: [3-broker local cluster](../../Day-2/Labs/kafka-local-multi-broker-cli-lab.md).

**Verify cluster:**

```bat
cd %KAFKA_HOME%
bin\windows\kafka-broker-api-versions.bat --bootstrap-server localhost:9092,localhost:9094,localhost:9095
```

---

## Step 4 - Confirm kafka-dump-log works

Used in Lab 01 and 02:

```bat
cd %KAFKA_HOME%
bin\windows\kafka-dump-log.bat --help
```

---

## Step 5 - Create lab topics (optional)

From `Day-5\labs` with `KAFKA_HOME` set:

| Script | When |
|--------|------|
| `scripts\create-storage-demo-topic.bat` | Before Lab 01 |
| `scripts\create-index-lab-topic.bat` | Before Lab 02 |
| `scripts\create-failover-lab-topic.bat` | Before Lab 03 (3 brokers) |
| `scripts\create-isr-lab-topic.bat` | Before Lab 04 (3 brokers) |
| `scripts\create-retention-lab-topic.bat` | Before Lab 05 |
| `scripts\create-compact-lab-topic.bat` | Before Lab 06 |

Topics are also created inside each lab guide if you prefer step-by-step.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `meta.properties` missing | Run `kafka-storage format` (Day-2 guide) |
| Port 9092 in use | Stop other Kafka or Docker |
| 3-broker format fails | Delete `C:\kafka-data\multi-broker-*` and rerun `setup-cluster.bat` |
| `kafka-dump-log` not found | Use `bin\windows\kafka-dump-log.bat`, not `.sh` |

---

## What you learned

- Where Kafka stores partition data on disk (`log.dirs`)
- Single vs 3-broker bootstrap strings
- Tools needed for the rest of Day 5

---

## Next lab

→ [Lab 01 - Inspect Kafka Log Files](../lab-01-inspect-log-files/README.md)
