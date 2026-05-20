# Lab 05- Message Keys & Partition Routing

**Objective:** See that the **same key** always maps to the **same partition**.

## Code

`java-kafka-producer-lab/src/main/java/com/kafka/producer/lab/KeyedProducer.java`

## Step 1- Run the keyed producer

Sends 10 messages, all with key `customer-1`:

```powershell
cd Day-3\Labs\java-kafka-producer-lab
mvn -q exec:java -Dexec.mainClass=com.kafka.producer.lab.KeyedProducer
```

Custom args: `bootstrap topic key count`

```powershell
mvn -q exec:java -Dexec.mainClass=com.kafka.producer.lab.KeyedProducer ^
  -Dexec.args="localhost:9092 orders-topic customer-1 10"
```

## Step 2- Record partition numbers

**Expected:** Every line shows the **same** `partition=` value.

Example:

```text
Order-1 -> partition=2 offset=...
Order-2 -> partition=2 offset=...
...
```

## Step 3- Change the key and compare

```powershell
mvn -q exec:java -Dexec.mainClass=com.kafka.producer.lab.KeyedProducer ^
  -Dexec.args="localhost:9092 orders-topic customer-2 10"
```

Partition number may differ from `customer-1`.

## Step 4- Explain the behavior

Kafka computes:

```text
partition = murmur2(key) % numPartitions
```

Same key → same hash → same partition → **ordering per key**.

## Exercise

Run two producers in parallel with keys `customer-A` and `customer-B`. Verify each key’s stream stays on one partition.

## Next lab

→ [Lab 06- Round Robin (No Key)](../lab-06-round-robin/README.md)
