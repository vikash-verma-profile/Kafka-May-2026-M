# Multi-Broker Kafka Lab -Local Windows (CLI Only)

Run **3 Kafka brokers** on your **local machine** (`C:\kafka-bin\kafka_2.13-4.2.0`) and practice producer/consumer scenarios using **only CLI** -no Docker, no Java/Python/.NET code.

**Single-broker local guide:** [kafka-kraft-setup-windows.md](./kafka-kraft-setup-windows.md)  
**Docker version of this lab:** [kafka-multi-broker-cli-lab.md](./kafka-multi-broker-cli-lab.md)

---

## 1. What you will learn

| Concept | How you see it |
|---------|----------------|
| **3 brokers on one PC** | 3 terminals running `kafka-server-start.bat` |
| **Bootstrap servers** | Clients use `localhost:9092,localhost:9094,localhost:9095` |
| **Partitions & replication** | Topic with 3 partitions, replication factor 3 |
| **Leader broker** | `kafka-topics.bat --describe` |
| **Scenario 2** | 1 producer → 3 brokers → 1 consumer |
| **Scenario 1** | Multiple producers / multiple consumers |

---

## 2. Cluster layout (local)

```text
  YOUR WINDOWS PC -C:\kafka-bin\kafka_2.13-4.2.0
  +------------------------------------------------------------------+
  |  bootstrap-server (clients):                                     |
  |    localhost:9092,localhost:9094,localhost:9095                  |
  |                                                                  |
  |  Terminal 1          Terminal 2          Terminal 3            |
  |  broker1             broker2             broker3                 |
  |  PLAINTEXT :9092     PLAINTEXT :9094     PLAINTEXT :9095         |
  |  CONTROLLER :9093    CONTROLLER :9193    CONTROLLER :9293        |
  |  log: multi-broker-1  multi-broker-2     multi-broker-3          |
  +------------------------------------------------------------------+
```

| Broker | `node.id` | Client port (PLAINTEXT) | Controller port | Log directory |
|--------|-----------|-------------------------|-----------------|---------------|
| 1 | 1 | **9092** | 9093 | `C:\kafka-data\multi-broker-1` |
| 2 | 2 | **9094** | 9193 | `C:\kafka-data\multi-broker-2` |
| 3 | 3 | **9095** | 9293 | `C:\kafka-data\multi-broker-3` |

> **Note:** Port **9093** is the **controller** for broker 1, not a client port. Do not use `9093` in `--bootstrap-server` for producers/consumers.

**Config files (in this repo):**

```text
Labs/kafka-local-3brokers/config/broker1.properties
Labs/kafka-local-3brokers/config/broker2.properties
Labs/kafka-local-3brokers/config/broker3.properties
```

---

## 3. Prerequisites

| Requirement | Detail |
|-------------|--------|
| Kafka install | `C:\kafka-bin\kafka_2.13-4.2.0` |
| `kafka-storage.bat` | Must not be empty (see [kafka-kraft-setup-windows.md](./kafka-kraft-setup-windows.md)) |
| Free ports | **9092, 9093, 9094, 9095, 9193, 9293** |
| RAM | ~2–3 GB for 3 JVM processes |

### Stop other Kafka before this lab

1. Close terminal running single-broker `kafka-server-start.bat config\server.properties`
2. Stop Docker Kafka if running:

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-docker
docker compose down
docker compose -f docker-compose-3brokers.yml down
```

---

## 4. One-time cluster setup

### Step 4.1 -Run setup script (format all 3 brokers)

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-local-3brokers\scripts
setup-cluster.bat
```

**Expected:** cluster ID printed, three “Formatting…” successes.

This creates:

- `C:\kafka-data\multi-broker-1`, `multi-broker-2`, `multi-broker-3`
- `cluster-id.txt` and `initial-controllers.txt` under `kafka-local-3brokers\`

**If format fails:** delete the three `multi-broker-*` folders and run `setup-cluster.bat` again.

---

### Step 4.2 -Start all 3 brokers (3 separate terminals)

Keep each terminal open.

**Terminal 1 -Broker 1**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-local-3brokers\scripts
start-broker1.bat
```

Wait for: `Kafka Server started`

**Terminal 2 -Broker 2**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-local-3brokers\scripts
start-broker2.bat
```

**Terminal 3 -Broker 3**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-local-3brokers\scripts
start-broker3.bat
```

---

### Step 4.3 -Verify cluster

**Terminal 4 (CLI)**

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set BS=localhost:9092,localhost:9094,localhost:9095
bin\windows\kafka-broker-api-versions.bat --bootstrap-server %BS%
```

No connection error = cluster is ready.

---

## 5. CLI command pattern (every scenario)

**Always:**

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set BS=localhost:9092,localhost:9094,localhost:9095
```

| Task | Command |
|------|---------|
| Create topic | `bin\windows\kafka-topics.bat --bootstrap-server %BS% --create ...` |
| Describe topic | `bin\windows\kafka-topics.bat --bootstrap-server %BS% --describe ...` |
| Producer | `bin\windows\kafka-console-producer.bat --bootstrap-server %BS% --topic <name>` |
| Consumer | `bin\windows\kafka-console-consumer.bat --bootstrap-server %BS% --topic <name> --from-beginning --group <group>` |

---

# Scenario 2 -One publisher, 3 brokers, one consumer

**Goal:** One console producer sends messages. Data is stored on all 3 brokers (replicas). One console consumer reads everything.

### How they communicate

1. Producer connects to `%BS%` (any broker).
2. Kafka returns metadata: **leader** broker per partition.
3. Producer writes to leaders; leaders **replicate** to other brokers.
4. Consumer connects to `%BS%`, joins group `scenario2-local`, reads from assigned partitions.

```text
  [1 Producer] ---> leader on broker 2 (example) ---> replicas on broker 1, 3
                           |
                           v
                    [1 Consumer]  (reads via cluster metadata)
```

---

### Step 2.1 -Create topic

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set BS=localhost:9092,localhost:9094,localhost:9095

bin\windows\kafka-topics.bat --bootstrap-server %BS% --create --topic scenario2-topic --partitions 3 --replication-factor 3
```

**Expected:** `Created topic scenario2-topic.`

---

### Step 2.2 -See partition leaders (proof of multi-broker)

```bat
bin\windows\kafka-topics.bat --bootstrap-server %BS% --describe --topic scenario2-topic
```

**Example:**

```text
Topic: scenario2-topic  PartitionCount: 3  ReplicationFactor: 3
  Partition: 0  Leader: 2  Replicas: 2,3,1  Isr: 2,3,1
  Partition: 1  Leader: 3  Replicas: 3,1,2  Isr: 3,1,2
  Partition: 2  Leader: 1  Replicas: 1,2,3  Isr: 1,2,3
```

| Column | Meaning |
|--------|---------|
| **Leader** | Broker id handling reads/writes |
| **Replicas** | All brokers holding a copy |
| **Isr** | In-sync replicas |

---

### Step 2.3 -Start ONE consumer (Terminal A)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set BS=localhost:9092,localhost:9094,localhost:9095

bin\windows\kafka-console-consumer.bat --bootstrap-server %BS% --topic scenario2-topic --from-beginning --group scenario2-local
```

Leave running.

---

### Step 2.4 -Start ONE producer (Terminal B)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set BS=localhost:9092,localhost:9094,localhost:9095

bin\windows\kafka-console-producer.bat --bootstrap-server %BS% --topic scenario2-topic
```

**Type (Enter after each line):**

```text
local-msg-1
local-msg-2
local-msg-3
local-msg-4
local-msg-5
```

---

### Step 2.5 -Expected result

| Terminal | What you see |
|----------|----------------|
| Producer | Accepts lines |
| Consumer | All 5 messages (order may vary) |

Run `--describe` again -leaders stay on different broker ids.

---

# Scenario 1 -Multiple producers and consumers

Same `%BS%` and `cd C:\kafka-bin\kafka_2.13-4.2.0`.

---

## Scenario 1A -Two producers, two consumers (same group)

Messages are **split** between consumers.

### Create topic

```bat
set BS=localhost:9092,localhost:9094,localhost:9095
bin\windows\kafka-topics.bat --bootstrap-server %BS% --create --topic multi-pc-topic --partitions 3 --replication-factor 3
```

### Consumer 1 (Terminal A)

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server %BS% --topic multi-pc-topic --from-beginning --group shared-local-group
```

### Consumer 2 (Terminal B)

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server %BS% --topic multi-pc-topic --from-beginning --group shared-local-group
```

### Producer 1 (Terminal C)

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server %BS% --topic multi-pc-topic --producer-property client.id=producer-A
```

Type: `from-A-1`, `from-A-2`

### Producer 2 (Terminal D)

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server %BS% --topic multi-pc-topic --producer-property client.id=producer-B
```

Type: `from-B-1`, `from-B-2`

### Observe

- 4 messages sent total  
- Both consumers together show all 4  
- Each consumer gets a **subset** (load balancing)

---

## Scenario 1B -Two consumers, different groups (fan-out)

Each group receives **every** message.

### Consumer group `team-red` (Terminal A)

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server %BS% --topic multi-pc-topic --from-beginning --group team-red
```

### Consumer group `team-blue` (Terminal B)

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server %BS% --topic multi-pc-topic --from-beginning --group team-blue
```

### One producer (Terminal C)

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server %BS% --topic multi-pc-topic
```

Type: `broadcast-1`, `broadcast-2`

### Observe

- **Both** consumers print **both** messages

---

# Scenario comparison

| Scenario | Producers | Brokers | Consumers | Groups | Result |
|----------|-----------|---------|-----------|--------|--------|
| **2** | 1 | 3 local | 1 | 1 | All messages to one consumer; replicas on 3 brokers |
| **1A** | 2 | 3 local | 2 | Same | Messages split across consumers |
| **1B** | 1+ | 3 local | 2 | Different | Each group gets all messages |

---

# Useful CLI reference

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set BS=localhost:9092,localhost:9094,localhost:9095
```

**List topics**

```bat
bin\windows\kafka-topics.bat --bootstrap-server %BS% --list
```

**List consumer groups**

```bat
bin\windows\kafka-consumer-groups.bat --bootstrap-server %BS% --list
```

**Describe group**

```bat
bin\windows\kafka-consumer-groups.bat --bootstrap-server %BS% --describe --group scenario2-local
```

**Delete topic**

```bat
bin\windows\kafka-topics.bat --bootstrap-server %BS% --delete --topic scenario2-topic
```

---

# Stop the cluster

1. Press **Ctrl+C** in each of the 3 broker terminals  
2. Optional -wipe data for a fresh lab:

```bat
rmdir /s /q C:\kafka-data\multi-broker-1
rmdir /s /q C:\kafka-data\multi-broker-2
rmdir /s /q C:\kafka-data\multi-broker-3
```

Then run `setup-cluster.bat` again.

---

# Troubleshooting

| Problem | Fix |
|---------|-----|
| `Address already in use` | Stop single-broker Kafka / Docker; check ports 9092–9095 |
| `No readable meta.properties` | Run `setup-cluster.bat` |
| `Replication factor: 3 larger than number of brokers` | Start all 3 brokers before creating topic |
| `LEADER_NOT_AVAILABLE` | Wait 30 s after starting brokers 1–3 |
| Producer/consumer only use `localhost:9092` | Use full `%BS%` with 9092,9094,9095 |
| `kafka-storage.bat` does nothing | File was empty -copy from [kafka-kraft-setup-windows.md](./kafka-kraft-setup-windows.md) |
| Format already exists | Delete `C:\kafka-data\multi-broker-*` folders, re-run setup |

---

# Local vs Docker (same lab, different runtimes)

| Step | Local (this doc) | Docker |
|------|------------------|--------|
| Start cluster | 3× `start-brokerN.bat` | `docker compose -f docker-compose-3brokers.yml up -d` |
| Bootstrap | `localhost:9092,localhost:9094,localhost:9095` | `localhost:9092,localhost:9093,localhost:9094` |
| CLI prefix | `bin\windows\kafka-....bat` | `docker exec broker1 /opt/kafka/bin/....sh` |
| Config | `Labs\kafka-local-3brokers\config\` | `docker-compose-3brokers.yml` |

---

# Quick start -Scenario 2 (copy/paste)

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-local-3brokers\scripts
setup-cluster.bat
```

Start `start-broker1.bat`, `start-broker2.bat`, `start-broker3.bat` in **3 terminals**.

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set BS=localhost:9092,localhost:9094,localhost:9095
bin\windows\kafka-topics.bat --bootstrap-server %BS% --create --topic scenario2-topic --partitions 3 --replication-factor 3
bin\windows\kafka-topics.bat --bootstrap-server %BS% --describe --topic scenario2-topic
```

**Consumer terminal:**

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server %BS% --topic scenario2-topic --from-beginning --group scenario2-local
```

**Producer terminal:**

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server %BS% --topic scenario2-topic
```

---

# Files in this lab

```text
kafka-local-3brokers/
  config/
    broker1.properties
    broker2.properties
    broker3.properties
  scripts/
    setup-cluster.bat
    start-broker1.bat
    start-broker2.bat
    start-broker3.bat
kafka-local-multi-broker-cli-lab.md   ← this document
```
