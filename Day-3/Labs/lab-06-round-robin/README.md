# Lab 06- Round Robin / No-Key Partitioning

**Objective:** Send messages **without a key** and observe distribution across partitions.

## Code

`RoundRobinProducer.java`

## Step 1- Run the producer

Default: 20 messages, no key:

```powershell
mvn -q exec:java -Dexec.mainClass=com.kafka.producer.lab.RoundRobinProducer
```

## Step 2- Compare with Lab 05

| Lab | Key | Expected partitions |
|-----|-----|---------------------|
| 05 | `customer-1` | One partition only |
| 06 | *(none)* | Multiple partition IDs in output |

## Step 3- Understand sticky partitioning

Modern Kafka producers use **sticky** partitioning per batch: records in one batch go to one partition, then the next batch may rotate. You should still see **more than one** partition over 20 messages.

## Step 4- When to omit keys

Use **no key** when:

- Strict ordering is not required
- You want maximum spread for load balancing

Use **keys** when:

- Per-entity ordering matters (per customer, per order ID)

## Next lab

→ [Lab 07- Acks Configuration](../lab-07-acks-configuration/README.md)
