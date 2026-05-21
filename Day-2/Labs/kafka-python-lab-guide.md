# Kafka Python Lab -Create, Build & Run Guide

Python version of the Java lab: a **producer** sends messages to Kafka and a **consumer** prints them. Uses the same broker (`localhost:9092`) and topic (`lab-messages`).

**Related docs**

- Kafka broker setup: [kafka-kraft-setup-windows.md](./kafka-kraft-setup-windows.md)
- Java lab (same concepts): [kafka-java-lab-guide.md](./kafka-java-lab-guide.md)

---

## 1. What this lab does

| File | Role |
|------|------|
| **simple_producer.py** | Sends 5 text messages to `lab-messages` |
| **simple_consumer.py** | Subscribes and prints each message |
| **config.py** | Shared defaults: broker, topic, group id |

Library: **[kafka-python](https://github.com/dpkp/kafka-python)** (pure Python, easy install on Windows).

---

## 2. Prerequisites

| Requirement | How to check |
|-------------|----------------|
| **Python 3.9+** | `python --version` |
| **Kafka running** | `localhost:9092` -[kafka-kraft-setup-windows.md](./kafka-kraft-setup-windows.md) |
| **Topic `lab-messages`** | Create once (see below) |

---

## 3. Lab folder layout

```text
Day-2/Labs/
  kafka-python-lab-guide.md    ← this file
  run-python-producer.bat
  run-python-consumer.bat
  kafka-python-lab/
    requirements.txt
    config.py
    simple_producer.py
    simple_consumer.py
    .venv/                     ← created by you (not in git)
```

---

## 4. How this lab was created

### Step 4.1 -Project folder

```text
kafka-python-lab/
  requirements.txt
  config.py
  simple_producer.py
  simple_consumer.py
```

### Step 4.2 -`requirements.txt`

```text
kafka-python==2.0.2
```

Install with: `pip install -r requirements.txt`

### Step 4.3 -`config.py`

Default values (same as Java lab):

- Bootstrap: `localhost:9092`
- Topic: `lab-messages`
- Consumer group: `python-lab-group`

### Step 4.4 -`simple_producer.py`

1. Creates `KafkaProducer` with UTF-8 value serializer  
2. Sends `"Hello from Python producer - message N"`  
3. Waits for each send with `future.get()` (like Java callback)  
4. Prints partition and offset  

### Step 4.5 -`simple_consumer.py`

1. Creates `KafkaConsumer` subscribed to `lab-messages`  
2. `auto_offset_reset="earliest"` -read from start if no offset saved  
3. Iterates `for message in consumer` and prints value, partition, offset  
4. Stops on **Ctrl+C**  

---

## 5. One-time setup

### Step 5.1 -Start Kafka (Terminal 1)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-server-start.bat config\server.properties
```

Wait for: `Kafka Server started`

### Step 5.2 -Create topic (once)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --create --topic lab-messages --partitions 1 --replication-factor 1
```

### Step 5.3 -Virtual environment and dependencies

**Command 1**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-python-lab
```

**Command 2**

```bat
python -m venv .venv
```

**Command 3**

```bat
.venv\Scripts\pip install -r requirements.txt
```

Expected: installs `kafka-python` without errors.

---

## 6. How to run the lab (every session)

Use **3 terminals**: Kafka broker, consumer, producer.

### Method A -From `kafka-python-lab` folder

#### Terminal 2 -Consumer (start first)

**Command 1**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-python-lab
```

**Command 2**

```bat
.venv\Scripts\python simple_consumer.py
```

**Expected:**

```text
Connecting to localhost:9092, topic=lab-messages, group=python-lab-group (Ctrl+C to stop)
```

#### Terminal 3 -Producer

**Command 1**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-python-lab
```

**Command 2**

```bat
.venv\Scripts\python simple_producer.py
```

**Expected in producer terminal:**

```text
Connecting to localhost:9092, topic=lab-messages, messages=5
Sent: "Hello from Python producer - message 1" -> partition=0 offset=20
...
Producer finished.
```

**Expected in consumer terminal:**

```text
Received: "Hello from Python producer - message 1" | partition=0 offset=20 key=None
...
```

---

### Method B -From `Labs` folder (helper scripts)

After setup (Step 5.3), from `Labs`:

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs
run-python-consumer.bat
```

New terminal:

```bat
run-python-producer.bat
```

---

## 7. Optional arguments

```text
python script.py [bootstrapServers] [topic] [extra]
```

**Producer** -send 3 messages:

```bat
.venv\Scripts\python simple_producer.py localhost:9092 lab-messages 3
```

**Consumer** -custom group id:

```bat
.venv\Scripts\python simple_consumer.py localhost:9092 lab-messages my-group
```

---

## 8. End-to-end checklist

| # | Step | Done? |
|---|------|-------|
| 1 | Kafka on `localhost:9092` | ☐ |
| 2 | Topic `lab-messages` exists | ☐ |
| 3 | `python -m venv .venv` + `pip install -r requirements.txt` | ☐ |
| 4 | Run **simple_consumer.py** | ☐ |
| 5 | Run **simple_producer.py** | ☐ |
| 6 | See **Received:** in consumer | ☐ |

---

## 9. Troubleshooting

| Issue | Fix |
|-------|-----|
| `No module named 'kafka'` | Activate venv; run `pip install -r requirements.txt` |
| `NoBrokersAvailable` | Start Kafka broker |
| `UnknownTopicOrPartitionError` | Create `lab-messages` topic |
| Helper bat says "Run setup first" | Complete Step 5.3 in `kafka-python-lab` |
| Consumer shows old messages | Normal with `earliest`; use new group id |
| `python` not found | Install Python 3.9+ and add to PATH |

---

## 10. Java vs Python lab (same flow)

| | Java | Python |
|---|------|--------|
| Folder | `kafka-java-lab` | `kafka-python-lab` |
| Dependencies | Maven `kafka-clients` | `pip install kafka-python` |
| Producer class | `SimpleProducer` | `simple_producer.py` |
| Consumer class | `SimpleConsumer` | `simple_consumer.py` |
| Topic | `lab-messages` | `lab-messages` |
| Group id | `java-lab-group` | `python-lab-group` |

You can run Java producer and Python consumer (or vice versa) on the same topic.

---

## 11. Quick reference -copy/paste

**Setup (once)**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-python-lab
python -m venv .venv
.venv\Scripts\pip install -r requirements.txt
```

**Consumer**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-python-lab
.venv\Scripts\python simple_consumer.py
```

**Producer**

```bat
.venv\Scripts\python simple_producer.py
```

**From Labs**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs
run-python-consumer.bat
run-python-producer.bat
```
