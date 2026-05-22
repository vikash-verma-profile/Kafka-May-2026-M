# Lab 12- Multi-Partition Producer Exercise

**Objective:** Send 100 messages with 4 customer keys and verify partition distribution.

## Requirements

| Requirement | Value |
|-------------|-------|
| Topic | `orders-topic` |
| Partitions | 4 |
| Messages | 100 |
| Key pattern | `customer-0` … `customer-3` (`i % 4`) |

## Code

`MultiPartitionProducer.java`

## Step 1- Run the exercise

```powershell
mvn -q exec:java "-Dexec.mainClass=com.kafka.producer.lab.MultiPartitionProducer"
```

## Step 2- Read the summary

At the end you should see something like:

```text
--- Partition distribution ---
Partition 0: 25 messages
Partition 1: 25 messages
...
```

(~25 per partition if keys hash evenly across 4 customers)

## Step 3- Verify same customer → same partition

Filter output for `key=customer-0`- all lines share one partition ID.

## Step 4- Callback pattern

The lab uses send callbacks to print partition and offset:

```java
producer.send(record, (metadata, exception) -> { ... });
```

Use this in production for metrics and error handling.

## Challenge

Change to 5 customers (`i % 5`) on a 4-partition topic- some customers will share partitions (hash collision), but each **key** is still stable.

## Next lab

→ [Lab 13- Performance Tuning](../lab-13-performance-tuning/README.md)
