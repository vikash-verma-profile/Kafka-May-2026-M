# Kafka .NET Lab -Create, Build & Run Guide

.NET 6+ console app with **producer** and **consumer** using [Confluent.Kafka](https://github.com/confluentinc/confluent-kafka-dotnet). Same broker and topic as the Java and Python labs.

**Related docs**

- Kafka broker: [kafka-kraft-setup-windows.md](./kafka-kraft-setup-windows.md)
- Java lab: [kafka-java-lab-guide.md](./kafka-java-lab-guide.md)
- Python lab: [kafka-python-lab-guide.md](./kafka-python-lab-guide.md)

---

## 1. What this lab does

| Component | Role |
|-----------|------|
| **SimpleProducer** | Sends 5 messages to `lab-messages` |
| **SimpleConsumer** | Subscribes and prints each message |
| **KafkaConfig** | Defaults: `localhost:9092`, topic, group `dotnet-lab-group` |

Run with:

```bat
dotnet run -- consumer
dotnet run -- producer
```

---

## 2. Prerequisites

| Requirement | Check |
|-------------|--------|
| **.NET 6 SDK or newer** | `dotnet --version` |
| **Kafka on `localhost:9092`** | [kafka-kraft-setup-windows.md](./kafka-kraft-setup-windows.md) |
| **Topic `lab-messages`** | Create once (below) |

Install .NET SDK: https://dotnet.microsoft.com/download (this lab targets **net6.0**)

---

## 3. Lab folder layout

```text
Day-2/Labs/
  kafka-dotnet-lab-guide.md     ← this file
  run-dotnet-producer.bat
  run-dotnet-consumer.bat
  kafka-dotnet-lab/
    KafkaDotNetLab.sln
    src/KafkaDotNetLab/
      KafkaDotNetLab.csproj
      Program.cs
      KafkaConfig.cs
      SimpleProducer.cs
      SimpleConsumer.cs
```

---

## 4. How this lab was created

### Step 4.1 -Create solution and project

```bat
dotnet new sln -n KafkaDotNetLab
dotnet new console -n KafkaDotNetLab -o src/KafkaDotNetLab -f net6.0
dotnet sln add src/KafkaDotNetLab/KafkaDotNetLab.csproj
```

### Step 4.2 -Add NuGet package

```bat
dotnet add package Confluent.Kafka
```

In `KafkaDotNetLab.csproj`:

```xml
<PackageReference Include="Confluent.Kafka" Version="2.6.1" />
```

### Step 4.3 -Source files

| File | Purpose |
|------|---------|
| `KafkaConfig.cs` | Bootstrap, topic, group id constants |
| `SimpleProducer.cs` | `ProducerBuilder`, `ProduceAsync`, print offset |
| `SimpleConsumer.cs` | `ConsumerBuilder`, `Subscribe`, `Consume` loop |
| `Program.cs` | `dotnet run -- consumer` or `producer` |

### Step 4.4 -Confluent.Kafka basics

**Producer**

- `ProducerConfig.BootstrapServers`
- `ProducerBuilder<string, string>`
- `ProduceAsync(topic, message)`

**Consumer**

- `ConsumerConfig.GroupId`, `AutoOffsetReset.Earliest`
- `ConsumerBuilder<string, string>`
- `Subscribe(topic)` then `Consume(cancellationToken)`

---

## 5. One-time setup

### Step 5.1 -Start Kafka (Terminal 1)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-server-start.bat config\server.properties
```

### Step 5.2 -Create topic (once)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --create --topic lab-messages --partitions 1 --replication-factor 1
```

### Step 5.3 -Build the .NET project

**Command 1**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-dotnet-lab\src\KafkaDotNetLab
```

**Command 2**

```bat
dotnet restore
dotnet build
```

Expected: `Build succeeded`.

---

## 6. How to run the lab

**3 terminals:** Kafka broker, consumer, producer.

### Method A -From project folder

#### Terminal 2 -Consumer (start first)

**Command 1**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-dotnet-lab\src\KafkaDotNetLab
```

**Command 2**

```bat
dotnet run -- consumer
```

**Expected:**

```text
Connecting to localhost:9092, topic=lab-messages, group=dotnet-lab-group (Ctrl+C to stop)
```

#### Terminal 3 -Producer

**Command 1**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-dotnet-lab\src\KafkaDotNetLab
```

**Command 2**

```bat
dotnet run -- producer
```

**Expected (producer):**

```text
Connecting to localhost:9092, topic=lab-messages, messages=5
Sent: "Hello from .NET producer - message 1" -> partition=0 offset=25
...
Producer finished.
```

**Expected (consumer):**

```text
Received: "Hello from .NET producer - message 1" | partition=0 offset=25 key=null
...
```

---

### Method B -From `Labs` folder

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs
run-dotnet-consumer.bat
```

New terminal:

```bat
run-dotnet-producer.bat
```

---

## 7. Optional arguments

```text
dotnet run -- producer  [bootstrap] [topic] [messageCount]
dotnet run -- consumer  [bootstrap] [topic] [groupId]
```

**3 messages:**

```bat
dotnet run -- producer localhost:9092 lab-messages 3
```

**Custom consumer group:**

```bat
dotnet run -- consumer localhost:9092 lab-messages my-group
```

---

## 8. End-to-end checklist

| # | Step | Done? |
|---|------|-------|
| 1 | Kafka on `localhost:9092` | ☐ |
| 2 | Topic `lab-messages` | ☐ |
| 3 | `dotnet build` succeeds | ☐ |
| 4 | `dotnet run -- consumer` | ☐ |
| 5 | `dotnet run -- producer` | ☐ |
| 6 | **Received:** lines in consumer | ☐ |

---

## 9. Troubleshooting

| Issue | Fix |
|-------|-----|
| `dotnet` not found | Install .NET 6+ SDK |
| `NETSDK1045` / .NET 8.0 not supported | Project uses `net6.0`; install .NET 6 SDK or change `TargetFramework` in `.csproj` |
| `No connection could be made` | Start Kafka broker |
| `Unknown topic` | Create `lab-messages` |
| Run from wrong folder | `cd` to `src\KafkaDotNetLab` or use `run-dotnet-*.bat` |
| `Local: All broker connections are down` | Check `localhost:9092`, firewall |
| Consumer silent | Start consumer before producer; same topic name |

---

## 10. Compare all three labs

| | Java | Python | .NET |
|---|------|--------|------|
| Client | `kafka-clients` | `kafka-python` | `Confluent.Kafka` |
| Topic | `lab-messages` | `lab-messages` | `lab-messages` |
| Group | `java-lab-group` | `python-lab-group` | `dotnet-lab-group` |
| Run consumer | `SimpleConsumer` | `simple_consumer.py` | `dotnet run -- consumer` |
| Run producer | `SimpleProducer` | `simple_producer.py` | `dotnet run -- producer` |

Any producer can talk to any consumer on the same topic.

---

## 11. Quick reference

**Build**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-dotnet-lab\src\KafkaDotNetLab
dotnet build
```

**Consumer**

```bat
dotnet run -- consumer
```

**Producer**

```bat
dotnet run -- producer
```

**From Labs**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs
run-dotnet-consumer.bat
run-dotnet-producer.bat
```
