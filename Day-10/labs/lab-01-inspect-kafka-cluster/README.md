# Lab 01 — Inspect a Running Kafka Cluster

**Objective:** Use CLI tools to list brokers, topics, partition leadership, ISR, and consumer groups.

From **Kafka_cap.pptx** — Slide 8.

---

## Implementation

| Track | Command |
|-------|---------|
| **Shell** | [scripts/inspect-cluster.bat](../scripts/inspect-cluster.bat) |
| **Python** | `python lab01_inspect_cluster.py` in [python-production-lab](../../python-production-lab/) |

---

## Prerequisites

- Kafka 3.x/4.x on PATH (`KAFKA_HOME`)
- Cluster reachable on `localhost:9092` (or multi-broker from `start-kafka-cluster.bat`)

---

## Step 1 — Broker API versions

```bat
cd %KAFKA_HOME%
bin\windows\kafka-broker-api-versions.bat --bootstrap-server localhost:9092
```

**Expected:** API version table per broker (confirms connectivity).

Multi-broker:

```bat
bin\windows\kafka-broker-api-versions.bat --bootstrap-server localhost:9092,localhost:9094,localhost:9095
```

---

## Step 2 — List topics

```bat
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list
```

---

## Step 3 — Describe topic (partitions, leaders, ISR)

If `orders` does not exist, create it for the lab:

```bat
bin\windows\kafka-topics.bat --create --topic orders --bootstrap-server localhost:9092 --partitions 3 --replication-factor 3
```

Describe:

```bat
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --describe --topic orders
```

**Expected output (example):**

```text
Topic: orders   PartitionCount: 3   ReplicationFactor: 3
  Partition: 0  Leader: 1  Replicas: 1,2,3  Isr: 1,2,3
  Partition: 1  Leader: 2  Replicas: 2,3,1  Isr: 1,2,3
  Partition: 2  Leader: 3  Replicas: 3,1,2  Isr: 1,2,3
```

---

## Step 4 — List consumer groups

```bat
bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --list
```

Describe a specific group:

```bat
bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --describe --group order-processor
```

---

## Step 5 — Cluster metadata (optional)

```bat
bin\windows\kafka-metadata-shell.bat --snapshot %LOG_DIR%\__cluster_metadata-0\...
```

Or use KRaft `kafka-metadata-quorum.sh describe --status` if configured.

---

## Checkpoint

- [ ] API versions returned for all brokers
- [ ] Topic describe shows Leader, Replicas, ISR
- [ ] Consumer groups listed

---

## Concepts (slides 4–6)

- **Leader** — broker serving reads/writes for partition
- **ISR** — in-sync replicas eligible for leader election
- **Replication factor** — copy count across brokers
