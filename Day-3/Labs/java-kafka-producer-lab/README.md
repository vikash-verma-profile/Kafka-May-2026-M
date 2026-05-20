# Java Kafka Producer Lab (Day 3)

Maven project containing all Java producer examples for Day-3 labs.

## Build

```powershell
mvn -q compile
```

## Run a lab class

```powershell
mvn -q exec:java -Dexec.mainClass=com.kafka.producer.lab.BasicProducer
```

| Class | Lab |
|-------|-----|
| `BasicProducer` | 03 |
| `KeyedProducer` | 05 |
| `RoundRobinProducer` | 06 |
| `AcksProducer` | 07 |
| `RetriesProducer` | 08 |
| `IdempotentProducer` | 09 |
| `JsonOrderProducer` | 11 |
| `MultiPartitionProducer` | 12 |
| `PerformanceTunedProducer` | 13 |

Default topic: `orders-topic` on `localhost:9092`.
