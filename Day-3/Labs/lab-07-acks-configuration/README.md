# Lab 07- Acknowledgment (`acks`) Configuration

**Objective:** Configure producer durability via `acks`.

## Code

`AcksProducer.java`

## Acks modes

| Config | Meaning | Latency | Durability |
|--------|---------|---------|------------|
| `acks=0` | No wait for broker | Lowest | Lowest |
| `acks=1` | Leader ack only | Medium | Medium |
| `acks=all` | All ISR replicas | Higher | Highest |

## Step 1- Run with `acks=all` (default in lab)

```powershell
mvn -q exec:java "-Dexec.mainClass=com.kafka.producer.lab.AcksProducer" "-Dexec.args=localhost:9092 orders-topic all"
```

## Step 2- Run with `acks=1`

```powershell
mvn -q exec:java "-Dexec.mainClass=com.kafka.producer.lab.AcksProducer" "-Dexec.args=localhost:9092 orders-topic 1"
```

## Step 3- Run with `acks=0` (careful)

```powershell
mvn -q exec:java "-Dexec.mainClass=com.kafka.producer.lab.AcksProducer" "-Dexec.args=localhost:9092 orders-topic 0"
```

Producer returns quickly; messages can be lost if the broker fails immediately.

## Step 4- Observe in code

In `AcksProducer.java`:

```java
props.put(ProducerConfig.ACKS_CONFIG, acks);
```

## Production guidance

- **Critical data** (payments, orders): `acks=all` + idempotence (Lab 09)
- **Metrics / logs** where loss is acceptable: `acks=1` or `0`

## Next lab

→ [Lab 08- Producer Retries](../lab-08-producer-retries/README.md)
