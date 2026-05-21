# Java Kafka Consumer Lab (Day 4)

Maven project for Day 4 consumer hands-on labs.

## Build

```powershell
mvn -q compile
```

## Main classes

| Class | Lab |
|-------|-----|
| `com.kafka.consumer.lab.BasicConsumer` | 01 |
| `com.kafka.consumer.lab.NamedConsumer` | 02 |
| `com.kafka.consumer.lab.AutoCommitConsumer` | 03 |
| `com.kafka.consumer.lab.NoCommitConsumer` | 03 |
| `com.kafka.consumer.lab.ManualSyncCommitConsumer` | 03 |
| `com.kafka.consumer.lab.ManualAsyncCommitConsumer` | 03 |
| `com.kafka.consumer.lab.SlowConsumer` | 04 |
| `com.kafka.consumer.lab.DemoTopicSeeder` | Setup |
| `com.kafka.consumer.lab.CommitLabProducer` | 03 |
| `com.kafka.consumer.lab.LagLoadProducer` | 04 |

## Run (from `labs/`)

```powershell
run-java-consumer.bat com.kafka.consumer.lab.BasicConsumer
run-java-consumer.bat com.kafka.consumer.lab.NamedConsumer lab-group Consumer-1
```

Guides: `../lab-00-initial-setup/` through `../lab-04-consumer-lag/`.
