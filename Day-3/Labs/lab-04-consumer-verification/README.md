# Lab 04- Kafka Consumer Verification

**Objective:** Confirm producer output using the Kafka console consumer.

## Prerequisites

- Broker running
- At least one message on `orders-topic` (run [Lab 03](../lab-03-java-basic-producer/README.md) first)

## Step 1- Start the console consumer

With `KAFKA_HOME` set:

```powershell
bin\windows\kafka-console-consumer.bat ^
  --topic orders-topic ^
  --from-beginning ^
  --bootstrap-server localhost:9092
```

Or:

```powershell
Day-3\Labs\scripts\run-console-consumer.bat
```

## Step 2- Run the Java producer again

In a **second** terminal:

```powershell
cd Day-3\Labs\java-kafka-producer-lab
mvn -q exec:java -Dexec.mainClass=com.kafka.producer.lab.BasicProducer
```

## Step 3- Observe consumer output

**Expected (example):**

```text
101 : Order Created
```

Format depends on consumer properties; our script enables key printing.

## Step 4- Consumer with partition info (optional)

```powershell
bin\windows\kafka-console-consumer.bat ^
  --topic orders-topic ^
  --from-beginning ^
  --bootstrap-server localhost:9092 ^
  --property print.partition=true ^
  --property print.offset=true ^
  --property print.key=true ^
  --property key.separator=" : "
```

Note which **partition** received the message when a key is used.

## Step 5- Reset for next labs (optional)

To avoid confusion from old messages, use a new consumer group or delete/recreate the topic (dev only).

## What you learned

- Producers write; consumers read independently
- `--from-beginning` reads all retained messages
- Keys appear in the consumer when configured

## Next lab

→ [Lab 05- Message Keys](../lab-05-message-keys/README.md)
