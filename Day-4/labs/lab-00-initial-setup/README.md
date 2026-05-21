# Lab 00 - Initial Setup (Day 4 Consumers)

**Objective:** Install prerequisites, start a single-node Kafka broker (KRaft), and create Day 4 lab topics.

Complete this lab **once** before Lab 01–04.

---

## Prerequisites checklist

| Software | Version | Verify |
|----------|---------|--------|
| Java JDK | 17+ | `java -version` |
| Apache Kafka | 3.x / 4.x | Folder exists, e.g. `C:\kafka-bin\kafka_2.13-4.2.0` |
| Maven | 3.8+ | `mvn -version` |
| Python (optional) | 3.10+ | `python --version` |

---

## Step 1 - Install or locate Kafka

### Option A - Already installed (recommended)

If you completed Day 2/3, use the same Kafka folder:

```powershell
cd C:\kafka-bin\kafka_2.13-4.2.0
```

### Option B - Fresh install

1. Download [Apache Kafka](https://kafka.apache.org/downloads) (binary, Scala 2.13).
2. Extract to e.g. `C:\kafka-bin\kafka_2.13-4.2.0`.
3. Follow the one-time KRaft format steps in [Day-2 KRaft setup](../../Day-2/Labs/kafka-kraft-setup-windows.md) (Part 1).

---

## Step 2 - Set environment variables (each new terminal)

```powershell
set KAFKA_HOME=C:\kafka-bin\kafka_2.13-4.2.0
set PATH=%KAFKA_HOME%\bin\windows;%PATH%
```

For PowerShell (session only):

```powershell
$env:KAFKA_HOME = "C:\kafka-bin\kafka_2.13-4.2.0"
```

> Adjust the path if your Kafka version or folder name differs.

---

## Step 3 - Start the Kafka broker

**Terminal 1** (keep open):

```powershell
cd %KAFKA_HOME%
bin\windows\kafka-server-start.bat config\server.properties
```

**Expected:** log line similar to `Kafka Server started`.

| Problem | Fix |
|---------|-----|
| `meta.properties` missing | Run `kafka-storage format` once (see Day-2 guide) |
| Port 9092 in use | Stop other Kafka/Docker instances |

---

## Step 4 - Verify broker connectivity

**Terminal 2:**

```powershell
cd %KAFKA_HOME%
bin\windows\kafka-broker-api-versions.bat --bootstrap-server localhost:9092
```

No connection errors means the broker is reachable.

---

## Step 5 - Create Day 4 topics

From `Day-4\labs` (with `KAFKA_HOME` set):

```powershell
cd C:\Users\om\Desktop\KafKa\Day-4\labs
scripts\create-demo-topic.bat
scripts\create-lag-demo-topic.bat
```

Or run commands manually:

```powershell
bin\windows\kafka-topics.bat --create --topic demo-topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
bin\windows\kafka-topics.bat --create --topic lag-demo --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

| Topic | Partitions | Used in |
|-------|------------|---------|
| `demo-topic` | 3 | Labs 01, 02, 03 |
| `lag-demo` | 3 | Lab 04 |

If topics already exist, `--create` may fail - that is OK. Verify with Step 6.

---

## Step 6 - Describe topics

```powershell
scripts\describe-demo-topic.bat
scripts\describe-lag-demo-topic.bat
```

**Expected (example):**

```text
Topic: demo-topic
PartitionCount: 3
```

---

## Step 7 - Build Java consumer project (for Labs 01–04)

```powershell
cd C:\Users\om\Desktop\KafKa\Day-4\labs\java-kafka-consumer-lab
mvn -q compile
```

**Expected:** `BUILD SUCCESS` (or no error output with `-q`).

---

## Step 8 - Python setup (optional)

```powershell
cd C:\Users\om\Desktop\KafKa\Day-4\labs\python-kafka-consumer-lab
pip install -r requirements.txt
```

---

## Step 9 - Seed test data (optional, for Lab 01)

Produce 50+ keyed messages to `demo-topic`:

```powershell
cd C:\Users\om\Desktop\KafKa\Day-4\labs
scripts\seed-demo-topic.bat
```

Or use the console producer interactively:

```powershell
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic demo-topic --property parse.key=true --property key.separator=:
```

Type lines like `key-1:Message-1` (press Enter per line). Stop with `Ctrl+C`.

---

## Terminal layout (recommended)

| Terminal | Role |
|----------|------|
| 1 | Kafka broker (`kafka-server-start`) |
| 2 | CLI / scripts (topics, consumer-groups) |
| 3+ | Your consumer apps (Java/Python) |

---

## What you learned

- Kafka broker runs on `localhost:9092`
- Day 4 uses **`demo-topic`** and **`lag-demo`**, each with **3 partitions**
- `KAFKA_HOME` points CLI tools at your install

---

## Next lab

→ [Lab 01 - Build Kafka Consumer](../lab-01-build-kafka-consumer/README.md)
