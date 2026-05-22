# Lab 09- Idempotent Producer

**Objective:** Enable idempotence so retries do not create duplicate records.

## Code

`IdempotentProducer.java`

## Step 1- Review settings

```java
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
props.put(ProducerConfig.ACKS_CONFIG, "all");
props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
```

Idempotence automatically sets safe defaults for `acks`, `retries`, and `max.in.flight.requests.per.connection`.

## Step 2- Run the producer

```powershell
mvn -q exec:java "-Dexec.mainClass=com.kafka.producer.lab.IdempotentProducer"
```

## Step 3- Verify offsets are sequential per partition

Each message should get the next offset on its partition- no gaps from duplicates when retries occur.

## Step 4- Compare with Lab 08

| Setting | Duplicates on retry? |
|---------|-------------------|
| Retries only | Possible |
| Idempotence + retries | Prevented (per producer instance) |

> **Note:** Idempotence prevents duplicate **writes from the same producer PID** during retries. Application-level “exactly-once” across restarts needs transactions or dedup on the consumer.

## Best practice

Enable idempotence for all production producers sending critical events.

## Next lab

→ [Lab 10- Python Producer](../lab-10-python-producer/README.md)
