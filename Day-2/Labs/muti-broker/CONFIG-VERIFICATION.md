# Multi-Broker Config Verification (`muti-broker/`)

---

## Architecture (correct setup)

```text
                    +------------------+
                    |  controller      |
                    |  node.id = 1     |
                    |  port 9093       |  (KRaft only -not for producers)
                    +--------+---------+
                             |
         +-------------------+-------------------+
         |                   |                   |
   +-----v-----+       +-----v-----+       +-----v-----+
   | broker-1  |       | broker-2  |       | broker-3  |
   | node.id=2 |       | node.id=3 |       | node.id=4 |
   | :9092     |       | :9094     |       | :9095     |
   +-----------+       +-----------+       +-----------+
```

**CLI bootstrap (producers/consumers):**

```text
localhost:9092,localhost:9094,localhost:9095
```

Do **not** use port `9093` in `--bootstrap-server` -that is the controller.

---

## Your original files -what was OK / what was fixed

| Item | broker-1 | broker-2 | Status |
|------|----------|----------|--------|
| `process.roles=broker` | Yes | Yes | Correct |
| `controller.quorum.bootstrap.servers=localhost:9093` | Yes | Yes | Correct (needs controller running) |
| Client port | 9092 | 9094 | Correct |
| `advertised.listeners` matches port | Yes | Yes | Correct |
| Separate `log.dirs` | Yes | Yes | Correct |
| **node.id unique** | Was **1** | Was **2** | **Fixed** → 2 and 3 (1 reserved for controller) |
| **controller.properties** | Missing | Missing | **Added** (official template; `log.dirs` → `C:/kafka-data/...`) |
| **log.dirs on Windows** | `/tmp/...` | `/tmp/...` | **Fixed** → `C:/kafka-data/...` |
| **Replication for 3 brokers** | RF=1 | RF=1 | **Fixed** → RF=3 on brokers |
| **broker-3** | -| -| **Added** (node.id=4, port 9095) |

---

## Node ID map (final)

| File | Role | node.id | Client port |
|------|------|---------|-------------|
| `controller.properties` | controller | 1 | -(9093 internal) |
| `broker-1.properties` | broker | 2 | 9092 |
| `broker-2.properties` | broker | 3 | 9094 |
| `broker-3.properties` | broker | 4 | 9095 |

---

## Config location (canonical)

Active configs for this lab:

```text
C:\kafka-bin\kafka_2.13-4.2.0\config\
  controller.properties
  broker-1.properties
  broker-2.properties
  broker-3.properties
```

Copies may also exist under `muti-broker\`; scripts and the lab doc use **kafka-bin `config\`** paths.

## File list

```text
C:\kafka-bin\kafka_2.13-4.2.0\config\
  controller.properties
  broker-1.properties
  broker-2.properties
  broker-3.properties
muti-broker/
  (lab docs + scripts)
  cluster-id.txt             (created by setup-cluster.bat)
  scripts/
    setup-cluster.bat
    start-controller.bat     ← start FIRST
    start-broker-1.bat
    start-broker-2.bat
    start-broker-3.bat
    verify-cluster.bat
  CONFIG-VERIFICATION.md     ← this file
  kafka-multi-broker-cli-lab.md
```

---

## Setup & verify (commands)

### 1. Stop other Kafka (single broker / Docker)

Free ports: **9092, 9093, 9094, 9095**.

### 2. Format cluster (once, or after deleting `C:\kafka-data\kraft-*`)

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\muti-broker\scripts
setup-cluster.bat
```

### 3. Start in order (4 terminals)

| Order | Script |
|-------|--------|
| 1 | `start-controller.bat` |
| 2 | `start-broker-1.bat` |
| 3 | `start-broker-2.bat` |
| 4 | `start-broker-3.bat` |

Wait for `Kafka Server started` in each.

### 4. Verify

```bat
verify-cluster.bat
```

### 5. Create topic (3 partitions, RF=3)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set BS=localhost:9092,localhost:9094,localhost:9095
bin\windows\kafka-topics.bat --bootstrap-server %BS% --create --topic lab-messages --partitions 3 --replication-factor 3
bin\windows\kafka-topics.bat --bootstrap-server %BS% --describe --topic lab-messages
```

**Expected:** 3 partitions, leaders on node ids 2, 3, and/or 4, `Replicas:` lists all three.

---

## If you already formatted with old node.ids

Delete data folders and re-run setup:

```bat
rmdir /s /q C:\kafka-data\kraft-controller-logs
rmdir /s /q C:\kafka-data\kraft-broker-logs-1
rmdir /s /q C:\kafka-data\kraft-broker-logs-2
rmdir /s /q C:\kafka-data\kraft-broker-logs-3
setup-cluster.bat
```
