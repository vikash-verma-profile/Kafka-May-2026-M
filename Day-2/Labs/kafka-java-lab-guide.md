# Kafka Java Lab -Create, Build & Run Guide

This document explains **what the lab is**, **how it was created**, **what each file does**, and **how to run it** on your Windows machine with local Kafka.

**Related docs**

- Kafka broker setup: [kafka-kraft-setup-windows.md](./kafka-kraft-setup-windows.md)
- Short README inside project: [kafka-java-lab/README.md](./kafka-java-lab/README.md)

---

## 1. What this lab does

| Component | Role |
|-----------|------|
| **SimpleProducer** | Connects to Kafka, sends 5 text messages to topic `lab-messages` |
| **SimpleConsumer** | Subscribes to `lab-messages`, prints every message received |
| **KafkaConfig** | Shared settings: `localhost:9092`, serializers, consumer group |

You run the **consumer first**, then the **producer**. Messages typed/sent by Java appear in the consumer terminal -same idea as the console producer/consumer lab, but using the **Kafka Java client API**.

---

## 2. Prerequisites

| Requirement | How to check |
|-------------|----------------|
| **Java 17+** | `java -version` |
| **Apache Maven** | `mvn -version` |
| **Kafka broker running** | `localhost:9092` -see [kafka-kraft-setup-windows.md](./kafka-kraft-setup-windows.md) Part 2 |
| **Topic `lab-messages`** | Created once via CLI (step below) |

---

## 3. Lab folder layout (already created for you)

```text
Day-2/Labs/
  kafka-java-lab-guide.md     ← this file
  run-producer.bat            ← run producer from Labs folder
  run-consumer.bat            ← run consumer from Labs folder
  kafka-java-lab/
    pom.xml
    README.md
    src/main/java/com/kafka/lab/
      KafkaConfig.java
      SimpleProducer.java
      SimpleConsumer.java
```

---

## 4. How this lab was created (step by step)

Use this section if you want to **recreate** the project from scratch or understand the structure.

### Step 4.1 -Create Maven project folders

```text
kafka-java-lab/
  pom.xml
  src/main/java/com/kafka/lab/
```

### Step 4.2 -`pom.xml` -dependencies and Java version

The project uses:

- **`kafka-clients`** -official Apache Kafka Java API (producer/consumer)
- **`slf4j-simple`** -logging (Kafka client uses SLF4J)
- **Java 17** -`maven.compiler.release`
- **`exec-maven-plugin`** -run `main` without packaging a JAR manually

Key dependency:

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>3.9.0</version>
</dependency>
```

> **Note:** Your broker is Kafka **4.2**. Client `3.9.x` is compatible for this basic lab.

### Step 4.3 -`KafkaConfig.java`

Central place for connection settings:

| Setting | Producer | Consumer |
|---------|----------|----------|
| Bootstrap | `localhost:9092` | `localhost:9092` |
| Serializers | `StringSerializer` | `StringDeserializer` |
| Group ID | -| `java-lab-group` |
| Offset reset | -| `earliest` (read from start of topic) |

### Step 4.4 -`SimpleProducer.java`

1. Builds `KafkaProducer<String, String>`
2. Sends messages: `"Hello from Java producer - message 1"` … `5`
3. Uses **send callback** to print partition and offset
4. Calls `flush()` so all messages are sent before exit

### Step 4.5 -`SimpleConsumer.java`

1. Builds `KafkaConsumer<String, String>`
2. Subscribes to `lab-messages`
3. **Poll loop** -reads batches every 1 second
4. Prints each record: value, partition, offset
5. Runs until you press **Ctrl+C**

### Step 4.6 -Helper scripts (optional)

From `Labs/` folder, `run-producer.bat` and `run-consumer.bat` run Maven inside `kafka-java-lab/` so you do not forget to `cd`.

---

## 5. One-time setup before first run

### Step 5.1 -Start Kafka (Terminal 1)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-server-start.bat config\server.properties
```

Wait for: `Kafka Server started`

### Step 5.2 -Create topic (Terminal 2, once)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --create --topic lab-messages --partitions 1 --replication-factor 1
```

If topic exists, you will see an error -that is fine, skip to build/run.

### Step 5.3 -Build the Java project (once per code change)

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-java-lab
mvn -q compile
```

Expected: no errors (downloads dependencies on first run).

---

## 6. How to run the lab (every session)

You need **3 terminals**:

| Terminal | Purpose |
|----------|---------|
| **1** | Kafka broker (always running) |
| **2** | Java **consumer** (start first) |
| **3** | Java **producer** (start second) |

---

### Method A -Run from `kafka-java-lab` folder (recommended)

#### Terminal 2 -Consumer

**Command 1**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-java-lab
```

**Command 2**

```bat
mvn -q exec:java -Dexec.mainClass=com.kafka.lab.SimpleConsumer
```

**Expected startup:**

```text
Connecting to localhost:9092, topic=lab-messages, group=java-lab-group (Ctrl+C to stop)
```

Leave this terminal open.

#### Terminal 3 -Producer

**Command 1**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-java-lab
```

**Command 2**

```bat
mvn -q exec:java -Dexec.mainClass=com.kafka.lab.SimpleProducer
```

**Expected in producer terminal:**

```text
Connecting to localhost:9092, topic=lab-messages, messages=5
Sent: "Hello from Java producer - message 1" -> partition=0 offset=12
Sent: "Hello from Java producer - message 2" -> partition=0 offset=13
...
Producer finished.
```

**Expected in consumer terminal (same time):**

```text
Received: "Hello from Java producer - message 1" | partition=0 offset=12 key=null
Received: "Hello from Java producer - message 2" | partition=0 offset=13 key=null
...
```

Offset numbers vary depending on how many messages were sent before.

---

### Method B -Run from `Labs` folder (helper scripts)

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs
run-consumer.bat
```

New terminal:

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs
run-producer.bat
```

---

## 7. Optional: custom arguments

Run from **`kafka-java-lab`** folder.

**Producer** -send 3 messages:

```bat
mvn -q exec:java -Dexec.mainClass=com.kafka.lab.SimpleProducer -Dexec.args="localhost:9092 lab-messages 3"
```

**Consumer** -custom group id:

```bat
mvn -q exec:java -Dexec.mainClass=com.kafka.lab.SimpleConsumer -Dexec.args="localhost:9092 lab-messages my-custom-group"
```

Argument order: `[bootstrapServers] [topic] [thirdArg]`

---

## 8. End-to-end checklist

| # | Step | Done? |
|---|------|-------|
| 1 | Kafka running on `localhost:9092` | ☐ |
| 2 | Topic `lab-messages` exists | ☐ |
| 3 | `cd kafka-java-lab` and `mvn compile` | ☐ |
| 4 | Start **SimpleConsumer** | ☐ |
| 5 | Start **SimpleProducer** | ☐ |
| 6 | See **Received:** lines in consumer | ☐ |

---

## 9. Troubleshooting

| Error / symptom | Cause | Fix |
|-----------------|-------|-----|
| `no POM in this directory` | Ran `mvn` from `Labs` not `kafka-java-lab` | `cd kafka-java-lab` or use `run-producer.bat` / `run-consumer.bat` |
| `Connection refused` | Kafka not running | Start broker (Part 2 of setup guide) |
| `UnknownTopicOrPartitionException` | Topic missing | Create `lab-messages` (Step 5.2) |
| Consumer shows nothing | Producer not run, or wrong topic | Same topic name in both classes |
| Consumer shows old messages | `auto.offset.reset=earliest` | Normal; use new `group id` to re-read from start |
| `mvn` not recognized | Maven not installed | Install Maven, add to PATH |
| Build fails Java version | Java &lt; 17 | Install JDK 17+ |
| First `mvn` run slow | Downloading dependencies | Wait; needs internet once |

---

## 10. How it maps to Kafka concepts

```text
  SimpleProducer                Kafka Broker                 SimpleConsumer
  (KafkaProducer)          (localhost:9092)              (KafkaConsumer)
        |                            |                            |
        |---- publish to ------------>|---- topic: lab-messages --->|
        |     lab-messages            |                            |
        |                             |     consumer group:        |
        |                             |     java-lab-group         |
```

- **Topic:** `lab-messages` -channel for messages  
- **Partition:** `0` (single partition in this lab)  
- **Offset:** position of each message in the partition  
- **Consumer group:** `java-lab-group` -Kafka tracks what the group has read  

---

## 11. Quick reference -copy/paste

**Build**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-java-lab
mvn -q compile
```

**Consumer**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-java-lab
mvn -q exec:java -Dexec.mainClass=com.kafka.lab.SimpleConsumer
```

**Producer**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-java-lab
mvn -q exec:java -Dexec.mainClass=com.kafka.lab.SimpleProducer
```

**From Labs only**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs
run-consumer.bat
run-producer.bat
```

---

## 12. Next steps (optional learning)

- Change message text in `SimpleProducer.java` and recompile
- Add a **message key** in `ProducerRecord` (e.g. `"user-1"`)
- Run **two consumers** with the same group id -messages are load-balanced
- Run a consumer with a **new group id** -both read all messages from `earliest`
