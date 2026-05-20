# Lab 01-Environment Setup & Kafka Cluster

**Objective:** Start Kafka (KRaft) and create the lab topic `orders-topic` with 4 partitions.

## Prerequisites

| Software | Version |
|----------|---------|
| Java JDK | 17+ |
| Apache Kafka | 3.x / 4.x |
| Maven | 3.8+ (for Java labs later) |

## Step 1-Open your Kafka installation folder

Adjust the path to match your machine:

```powershell
cd C:\kafka-bin\kafka_2.13-4.2.0
```

Set `KAFKA_HOME` for the helper scripts in `Labs/scripts/`:

```powershell
set KAFKA_HOME=C:\kafka-bin\kafka_2.13-4.2.0
```

## Step 2-Start the Kafka broker (KRaft)

From the Kafka folder:

```powershell
bin\windows\kafka-server-start.bat config\server.properties
```

**Expected output:** `Kafka Server started` (or similar). Leave this terminal open.

> If this is a fresh install, run `kafka-storage format` once first-see [Day-2 KRaft setup](../../Day-2/Labs/kafka-kraft-setup-windows.md).

## Step 3-Create `orders-topic`

Open a **new** terminal:

```powershell
bin\windows\kafka-topics.bat --create ^
  --topic orders-topic ^
  --bootstrap-server localhost:9092 ^
  --partitions 4 ^
  --replication-factor 1
```

Or from `Day-3/Labs`:

```powershell
Labs\scripts\create-orders-topic.bat
```

| Parameter | Meaning |
|-----------|---------|
| `--topic` | Topic name |
| `--partitions` | Parallelism (4 for this course) |
| `--replication-factor` | Copies per partition (1 on single broker) |

## Step 4-Verify the topic

```powershell
bin\windows\kafka-topics.bat --describe ^
  --topic orders-topic ^
  --bootstrap-server localhost:9092
```

**Expected output (example):**

```text
Topic: orders-topic
PartitionCount: 4
```

Or run: `Labs\scripts\describe-orders-topic.bat`

## Step 5-Quick connectivity check (optional)

```powershell
bin\windows\kafka-broker-api-versions.bat --bootstrap-server localhost:9092
```

## What you learned

- Kafka runs as a broker on `localhost:9092`
- Topics are divided into **partitions**
- Day-3 labs use **`orders-topic`** with **4 partitions**

## Next lab

→ [Lab 02-Producer Workflow](../lab-02-producer-workflow/README.md)
