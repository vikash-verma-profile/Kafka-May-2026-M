# Day 4 - Kafka Consumers & Consumer Groups Labs

Hands-on labs from **Kafka_Consumers.pptx**. Each folder has a step-by-step guide you can follow on Windows (PowerShell or CMD).

## Prerequisites

| Software | Version |
|----------|---------|
| Java JDK | 17+ |
| Apache Kafka | 3.x / 4.x (KRaft) |
| Maven | 3.8+ (Java labs) |
| Python | 3.10+ (optional) |
| kafka-python | `pip install kafka-python` |

**Broker:** `localhost:9092`  
**Lab topics:** `demo-topic` (3 partitions), `lag-demo` (3 partitions)

## Quick start

1. Complete **[lab-00-initial-setup](./lab-00-initial-setup/README.md)** - install Kafka, start broker, create topics.
   - **Multi-node cluster (controller + broker-1 + broker-3):** from this folder run `start-kafka-cluster.bat` (opens three CMD windows).
2. Run Java consumers from `java-kafka-consumer-lab/` or Python from `python-kafka-consumer-lab/`.
3. Use helper scripts in `scripts/` (set `KAFKA_HOME` first).

## Lab index

| Lab | Topic | Code / tools |
|-----|--------|----------------|
| [lab-00](./lab-00-initial-setup/README.md) | Initial setup & topics | `scripts/` |
| [lab-01](./lab-01-build-kafka-consumer/README.md) | Build a Kafka consumer | `BasicConsumer.java` / `basic_consumer.py` |
| [lab-02](./lab-02-consumer-groups/README.md) | Consumer groups & rebalancing | `NamedConsumer.java` / `named_consumer.py` |
| [lab-03](./lab-03-manual-vs-auto-commit/README.md) | Auto vs manual offset commit | `AutoCommitConsumer`, `ManualSyncCommitConsumer`, `ManualAsyncCommitConsumer` |
| [lab-04](./lab-04-consumer-lag/README.md) | Simulate & measure lag | `SlowConsumer.java` / `slow_consumer.py` |

## Project layout

```text
Day-4/labs/
  README.md
  scripts/
  java-kafka-consumer-lab/     ← Maven project (all Java consumers)
  python-kafka-consumer-lab/   ← Python consumers
  lab-00-initial-setup/
  lab-01-build-kafka-consumer/
  lab-02-consumer-groups/
  lab-03-manual-vs-auto-commit/
  lab-04-consumer-lag/
  run-java-consumer.bat
```

## Related (earlier days)

- [Kafka KRaft setup (Windows)](../../Day-2/Labs/kafka-kraft-setup-windows.md)
- [Day 3 Producer labs](../../Day-3/Labs/README.md)
