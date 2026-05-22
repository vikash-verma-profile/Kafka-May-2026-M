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

Keep the consumer terminal **open and waiting**. In a **second** terminal:

```powershell
cd Day-3\Labs\java-kafka-producer-lab
mvn -q exec:java "-Dexec.mainClass=com.kafka.producer.lab.BasicProducer"
```

If you ran the producer **before** starting the consumer, `--from-beginning` (in our script) should still show old messages — unless your topic is misconfigured (see Troubleshooting).

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
  --formatter-property print.partition=true ^
  --formatter-property print.offset=true ^
  --formatter-property print.key=true ^
  --formatter-property key.separator=" : "
```

Note which **partition** received the message when a key is used.

## Step 5- Reset for next labs (optional)

To avoid confusion from old messages, use a new consumer group or delete/recreate the topic (dev only).

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| Consumer shows only log lines, no messages | Producer not run yet, or consumer started without `--from-beginning` | Run producer in a 2nd terminal; our script uses `--from-beginning` |
| `Reconfiguration failed` / Log4j ERROR | Harmless Kafka 4.x logging noise | Ignore if messages appear |
| `Option --property is deprecated` | Kafka 4.2+ | Use `run-console-consumer.bat` (uses `--formatter-property`) |
| Consumer idle forever, producer says success | Wrong `orders-topic` on cluster (e.g. RF=3, multiple brokers) | See below |

### Verify your topic matches Lab 01

```powershell
Labs\scripts\describe-orders-topic.bat
```

**Labs expect (single local broker):**

```text
PartitionCount: 4
ReplicationFactor: 1
```

If you see `ReplicationFactor: 3` (or more than one broker in replicas), this is **not** the lab topic. The consumer may hang because brokers in metadata are unreachable from `localhost:9092`.

**Dev fix (local KRaft only):** delete and recreate:

```powershell
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --delete --topic orders-topic --bootstrap-server localhost:9092
Labs\scripts\create-orders-topic.bat
```

Then run the producer again and restart the consumer.

### Quick check that messages exist

```powershell
"%KAFKA_HOME%\bin\windows\kafka-get-offsets.bat" --bootstrap-server localhost:9092 --topic orders-topic
```

Non-zero offsets mean data is stored. Re-run `BasicProducer` if all partitions show `0`.

## What you learned

- Producers write; consumers read independently
- `--from-beginning` reads all retained messages
- Keys appear in the consumer when configured

## Next lab

→ [Lab 05- Message Keys](../lab-05-message-keys/README.md)
