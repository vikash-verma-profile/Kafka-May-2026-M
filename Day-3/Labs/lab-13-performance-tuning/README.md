# Lab 13- Producer Performance Tuning

**Objective:** Tune throughput with batching, linger, and compression.

## Code

`PerformanceTunedProducer.java`

## Important configurations

| Property | Purpose | Lab value |
|----------|---------|-----------|
| `batch.size` | Max bytes per batch | `16384` |
| `linger.ms` | Wait time to fill batch | `10` |
| `compression.type` | Compress batches | `snappy` |
| `buffer.memory` | Total producer buffer | `33554432` (32 MB) |

## Step 1- Run tuned producer

```powershell
mvn -q exec:java -Dexec.mainClass=com.kafka.producer.lab.PerformanceTunedProducer
```

Note messages/sec at the end.

## Step 2- Baseline without tuning

Copy `PerformanceTunedProducer.java` logic into a scratch class with:

- `linger.ms = 0`
- `compression.type = none`

Compare throughput (higher linger → higher throughput, higher latency).

## Step 3- Trade-offs

| Increase | Effect |
|----------|--------|
| `linger.ms` | Better batching, higher latency |
| `batch.size` | Larger network payloads |
| `compression` | Less bandwidth, more CPU |
| `acks=all` | Safer, slower |

## Step 4- Monitor in production

Use broker metrics: `bytes-in`, request rate, producer `record-send-rate`.

## Next lab

→ [Lab 14- Troubleshooting](../lab-14-troubleshooting/README.md)
