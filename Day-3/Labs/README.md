# Day 3- Kafka Producers Labs

Hands-on labs from **Kafka Producers-lab document.docx**. Each folder has a step-by-step guide and points to runnable code.

## Prerequisites

| Software | Version |
|----------|---------|
| Java JDK | 17+ |
| Apache Kafka | 3.x / 4.x (KRaft) |
| Maven | 3.8+ |
| Python | 3.10+ |
| kafka-python | `pip install kafka-python` |

Broker: `localhost:9092`  
Lab topic: **`orders-topic`** (4 partitions)

## Quick start

1. Complete [lab-01-environment-setup](./lab-01-environment-setup/README.md)- start broker and create `orders-topic`.
2. Run Java labs from `java-kafka-producer-lab/` (quote `-D` properties in PowerShell) or use `run-java-lab.ps1` / `run-java-lab.bat` in this folder.
3. Run Python labs from `python-kafka-producer-lab/`.

## Lab index

| Lab | Topic | Code |
|-----|--------|------|
| [lab-01](./lab-01-environment-setup/README.md) | Start Kafka & create `orders-topic` | `scripts/` |
| [lab-02](./lab-02-producer-workflow/README.md) | Producer workflow (theory) |- |
| [lab-03](./lab-03-java-basic-producer/README.md) | First Java producer | `BasicProducer.java` |
| [lab-04](./lab-04-consumer-verification/README.md) | Verify with console consumer | CLI |
| [lab-05](./lab-05-message-keys/README.md) | Key-based partition routing | `KeyedProducer.java` |
| [lab-06](./lab-06-round-robin/README.md) | No-key round-robin | `RoundRobinProducer.java` |
| [lab-07](./lab-07-acks-configuration/README.md) | `acks=0`, `1`, `all` | `AcksProducer.java` |
| [lab-08](./lab-08-producer-retries/README.md) | Retries & backoff | `RetriesProducer.java` |
| [lab-09](./lab-09-idempotent-producer/README.md) | Idempotent producer | `IdempotentProducer.java` |
| [lab-10](./lab-10-python-producer/README.md) | Python producer | `basic_producer.py` |
| [lab-11](./lab-11-structured-json/README.md) | JSON order events | Java + Python |
| [lab-12](./lab-12-multi-partition/README.md) | 100 messages, 4 customer keys | `MultiPartitionProducer.java` |
| [lab-13](./lab-13-performance-tuning/README.md) | batch.size, linger, compression | `PerformanceTunedProducer.java` |
| [lab-14](./lab-14-troubleshooting/README.md) | Errors & fixes | Reference |
| [lab-15](./lab-15-industry-example/README.md) | Food delivery architecture | Reference |

## Project layout

```text
Day-3/Labs/
  README.md
  scripts/
  run-java-lab.ps1             ← PowerShell helper for Java labs
  run-java-lab.bat             ← cmd.exe helper for Java labs
  java-kafka-producer-lab/     ← Maven project (all Java producers)
  python-kafka-producer-lab/   ← Python producers
  lab-01-environment-setup/
  lab-02-producer-workflow/
  ...
```

## Related (Day 2)

- [Kafka KRaft setup (Windows)](../../Day-2/Labs/kafka-kraft-setup-windows.md)
