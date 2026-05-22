# Lab 03- Java Kafka Producer (Basic)

**Objective:** Build and run a Java producer that sends one message with a key to `orders-topic`.

## Prerequisites

- [Lab 01](../lab-01-environment-setup/README.md) completed (broker + topic)
- Java 17+, Maven 3.8+

## Step 1- Open the Maven project

```text
Day-3/Labs/java-kafka-producer-lab/
  pom.xml
  src/main/java/com/kafka/producer/lab/
    BasicProducer.java
    ProducerConfigFactory.java
```

## Step 2- Review dependencies (`pom.xml`)

The project uses:

- `kafka-clients`- official Java producer API
- `slf4j-simple`- logging for the client

## Step 3- Read the producer code

Main class: `com.kafka.producer.lab.BasicProducer`

Key settings:

| Property | Value |
|----------|-------|
| `bootstrap.servers` | `localhost:9092` |
| Key serializer | `StringSerializer` |
| Value serializer | `StringSerializer` |
| Topic | `orders-topic` |
| Key | `"101"` |
| Value | `"Order Created"` |

## Step 4- Compile the project

```powershell
cd Day-3\Labs\java-kafka-producer-lab
mvn -q compile
```

## Step 5- Run the producer

**Option A- Maven:**

```powershell
mvn -q exec:java "-Dexec.mainClass=com.kafka.producer.lab.BasicProducer"
```

**Option B- Helper script (from `Labs` folder):**

```powershell
.\run-java-lab.ps1 com.kafka.producer.lab.BasicProducer
```

Or in **cmd.exe**: `run-java-lab.bat com.kafka.producer.lab.BasicProducer`

**Expected output:**

```text
Message Sent Successfully
```

## Step 6- Verify (preview of Lab 04)

In another terminal with `KAFKA_HOME` set:

```powershell
Labs\scripts\run-console-consumer.bat
```

You should see the message (key may appear if consumer prints keys).

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Connection refused | Start broker (Lab 01) |
| UnknownTopicOrPartition | Create `orders-topic` |
| Class not found | Run `mvn compile` from project folder |

## What you learned

- `KafkaProducer` + `Properties` configuration
- `ProducerRecord(topic, key, value)`
- Always `close()` the producer (try-with-resources)

## Next lab

→ [Lab 04- Consumer Verification](../lab-04-consumer-verification/README.md)
