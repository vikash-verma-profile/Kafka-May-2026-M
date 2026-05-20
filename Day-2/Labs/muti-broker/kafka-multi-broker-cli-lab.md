# Multi-Broker Kafka Lab - Local CLI (Step by Step)

**Kafka install:** `C:\kafka-bin\kafka_2.13-4.2.0`  
**Config folder:** `C:\kafka-bin\kafka_2.13-4.2.0\config`  
**CLI tools:** `C:\kafka-bin\kafka_2.13-4.2.0\bin\windows\`

**Architecture:** 1 controller + 3 brokers (KRaft). CLI only - no application code.

**Config review:** [CONFIG-VERIFICATION.md](./CONFIG-VERIFICATION.md)

**Full architecture (infra + diagrams):** [../kafka-architecture-guide.md](../kafka-architecture-guide.md)

---

## 1. Config files used (from `config\`)

| File | Role | `process.roles` | `node.id` | Client port | `log.dirs` |
|------|------|-----------------|-----------|-------------|------------|
| `controller.properties` | KRaft controller | `controller` | 1 | - (9093 controller) | `C:/kafka-data/kraft-controller-logs` |
| `broker-1.properties` | Broker | `broker` | 2 | **9092** | `C:/kafka-data/kraft-broker-logs-1` |
| `broker-2.properties` | Broker | `broker` | 3 | **9094** | `C:/kafka-data/kraft-broker-logs-2` |
| `broker-3.properties` | Broker | `broker` | 4 | **9095** | `C:/kafka-data/kraft-broker-logs-3` |

All brokers use:

```properties
controller.quorum.bootstrap.servers=localhost:9093
```

**Bootstrap for producers / consumers / topics CLI:**

```text
localhost:9092,localhost:9094,localhost:9095
```

Do **not** put port `9093` in `--bootstrap-server` (that is the controller).

Other files in `config\` (not used in this lab):

| File | Purpose |
|------|---------|
| `server.properties` | Single combined broker+controller (your Day-1 setup) |
| `producer.properties` | Default producer client settings |
| `consumer.properties` | Default consumer client settings |

---

## 2. Prerequisites

**Command 1 - Check Kafka**

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
dir config\controller.properties
dir config\broker-1.properties
dir bin\windows\kafka-storage.bat
```

**Command 2 - Stop other Kafka**

- Close any terminal running `kafka-server-start.bat config\server.properties`
- Stop Docker Kafka if running

Ports **9092, 9093, 9094, 9095** must be free.

---

# Part A - One-time cluster setup

### Step A1 - Open Command Prompt

### Step A2 - Go to Kafka home

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
```

### Step A3 - Create log directories

```bat
mkdir C:\kafka-data\kraft-controller-logs
mkdir C:\kafka-data\kraft-broker-logs-1
mkdir C:\kafka-data\kraft-broker-logs-2
mkdir C:\kafka-data\kraft-broker-logs-3
```

### Step A4 - Generate cluster ID (copy the output)

```bat
bin\windows\kafka-storage.bat random-uuid
```

Example output: `IrBZOkc6Siq9sMLnDA53Hg` — use your value as `YOUR_CLUSTER_ID` in the steps below.

### Step A5 - Format controller

Replace `YOUR_CLUSTER_ID` with the UUID from Step A4:

```bat
bin\windows\kafka-storage.bat format -t YOUR_CLUSTER_ID -c config\controller.properties --standalone
```

**Expected:** message about formatting `C:/kafka-data/kraft-controller-logs`.

### Step A6 - Format broker 1

Same cluster ID:

```bat
bin\windows\kafka-storage.bat format -t YOUR_CLUSTER_ID -c config\broker-1.properties --no-initial-controllers
```

### Step A7 - Format broker 2

```bat
bin\windows\kafka-storage.bat format -t YOUR_CLUSTER_ID -c config\broker-2.properties --no-initial-controllers
```

### Step A8 - Format broker 3

```bat
bin\windows\kafka-storage.bat format -t YOUR_CLUSTER_ID -c config\broker-3.properties --no-initial-controllers
```

---

# Part B - Start the cluster (every lab session)

Use **4 terminals**. Wait for `Kafka Server started` in each before the next.

### Step B1 - Terminal 1: Controller (start first)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-server-start.bat config\controller.properties
```

### Step B2 - Terminal 2: Broker 1

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-server-start.bat config\broker-1.properties
```

### Step B3 - Terminal 3: Broker 2

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-server-start.bat config\broker-2.properties
```

### Step B4 - Terminal 4: Broker 3

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-server-start.bat config\broker-3.properties
```

---

# Part C - Verify cluster (Terminal 5)

### Step C1 - Go to Kafka and set bootstrap

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set BS=localhost:9092,localhost:9094,localhost:9095
```

### Step C2 - Test broker API

```bat
bin\windows\kafka-broker-api-versions.bat --bootstrap-server %BS%
```

No connection error = cluster OK.

### Step C3 - List topics (may be empty)

```bat
bin\windows\kafka-topics.bat --bootstrap-server %BS% --list
```

---

# Part D - Scenario 2: One producer, three brokers, one consumer

### How it works

1. Producer connects to `%BS%`.
2. Kafka returns metadata; each partition has a **leader** broker (node 2, 3, or 4).
3. Producer writes to leaders; data **replicates** to other brokers.
4. One consumer reads all messages from the topic.

---

### Step D1 - Set bootstrap (new terminal)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set BS=localhost:9092,localhost:9094,localhost:9095
```

### Step D2 - Create topic (3 partitions, replication 3)

```bat
bin\windows\kafka-topics.bat --bootstrap-server %BS% --create --topic scenario2-topic --partitions 3 --replication-factor 3
```

**Expected:** `Created topic scenario2-topic.`

### Step D3 - Describe topic (see leaders on each broker)

```bat
bin\windows\kafka-topics.bat --bootstrap-server %BS% --describe --topic scenario2-topic
```

**Example:**

```text
Partition: 0  Leader: 2  Replicas: 2,3,4  Isr: 2,3,4
Partition: 1  Leader: 3  Replicas: 3,4,2  Isr: 3,4,2
Partition: 2  Leader: 4  Replicas: 4,2,3  Isr: 4,2,3
```

### Step D4 - Start consumer (Terminal A - before producer)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set BS=localhost:9092,localhost:9094,localhost:9095
bin\windows\kafka-console-consumer.bat --bootstrap-server %BS% --topic scenario2-topic --from-beginning --group scenario2-local
```

Leave this terminal open.

### Step D5 - Start producer (Terminal B)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set BS=localhost:9092,localhost:9094,localhost:9095
bin\windows\kafka-console-producer.bat --bootstrap-server %BS% --topic scenario2-topic
```

### Step D6 - Type messages (press Enter after each line)

```text
hello-from-single-producer-1
hello-from-single-producer-2
hello-from-single-producer-3
```

### Step D7 - Check consumer terminal

You should see the same three lines.

---

# Part E - Scenario 1A: Two producers, two consumers (same group)

Messages are **shared** between the two consumers.

### Step E1 - Bootstrap

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set BS=localhost:9092,localhost:9094,localhost:9095
```

### Step E2 - Create topic

```bat
bin\windows\kafka-topics.bat --bootstrap-server %BS% --create --topic multi-pc-topic --partitions 3 --replication-factor 3
```

### Step E3 - Consumer 1 (Terminal A)

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server %BS% --topic multi-pc-topic --from-beginning --group shared-local-group
```

### Step E4 - Consumer 2 (Terminal B)

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server %BS% --topic multi-pc-topic --from-beginning --group shared-local-group
```

### Step E5 - Producer 1 (Terminal C)

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server %BS% --topic multi-pc-topic --producer-property client.id=producer-A
```

Type: `from-A-1` then `from-A-2` (Enter after each).

### Step E6 - Producer 2 (Terminal D)

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server %BS% --topic multi-pc-topic --producer-property client.id=producer-B
```

Type: `from-B-1` then `from-B-2`.

### Step E7 - Observe

Both consumers together receive all 4 messages; each consumer gets a **subset**.

---

# Part F - Scenario 1B: Two consumers, different groups (fan-out)

Each group gets **every** message.

### Step F1 - Consumer group `team-red` (Terminal A)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set BS=localhost:9092,localhost:9094,localhost:9095
bin\windows\kafka-console-consumer.bat --bootstrap-server %BS% --topic multi-pc-topic --from-beginning --group team-red
```

### Step F2 - Consumer group `team-blue` (Terminal B)

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server %BS% --topic multi-pc-topic --from-beginning --group team-blue
```

### Step F3 - Producer (Terminal C)

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server %BS% --topic multi-pc-topic
```

Type: `broadcast-1` and `broadcast-2`.

### Step F4 - Observe

**Both** consumers print **both** messages.

---

# Part G - Extra CLI commands

Always:

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

**Describe consumer group**

```bat
bin\windows\kafka-consumer-groups.bat --bootstrap-server %BS% --describe --group scenario2-local
```

**Delete topic**

```bat
bin\windows\kafka-topics.bat --bootstrap-server %BS% --delete --topic scenario2-topic
```

---

# Part H - Stop cluster and reset

### Step H1 - Stop servers

Press **Ctrl+C** in each of the 4 broker/controller terminals.

### Step H2 - Optional: wipe data and re-format

```bat
rmdir /s /q C:\kafka-data\kraft-controller-logs
rmdir /s /q C:\kafka-data\kraft-broker-logs-1
rmdir /s /q C:\kafka-data\kraft-broker-logs-2
rmdir /s /q C:\kafka-data\kraft-broker-logs-3
```

Then repeat **Part A** (format).

---

# Troubleshooting

| Problem | Fix |
|---------|-----|
| Brokers won't start | Start `config\controller.properties` first |
| `Address already in use` | Stop `server.properties` / Docker Kafka |
| `Replication factor: 3` error | All 3 brokers must be running |
| `No readable meta.properties` | Run Part A format steps |
| Consumer empty | Same topic name; press Enter in producer |
| Wrong bootstrap | Use `9092,9094,9095` not `9093` |

---

# Quick reference

All commands from `C:\kafka-bin\kafka_2.13-4.2.0`:

| Step | Command |
|------|---------|
| Go to Kafka | `cd C:\kafka-bin\kafka_2.13-4.2.0` |
| Format controller | `bin\windows\kafka-storage.bat format -t YOUR_CLUSTER_ID -c config\controller.properties --standalone` |
| Format brokers | `bin\windows\kafka-storage.bat format -t YOUR_CLUSTER_ID -c config\broker-N.properties --no-initial-controllers` |
| Start controller | `bin\windows\kafka-server-start.bat config\controller.properties` |
| Start broker 1 | `bin\windows\kafka-server-start.bat config\broker-1.properties` |
| Start broker 2 | `bin\windows\kafka-server-start.bat config\broker-2.properties` |
| Start broker 3 | `bin\windows\kafka-server-start.bat config\broker-3.properties` |
| Bootstrap | `set BS=localhost:9092,localhost:9094,localhost:9095` |
