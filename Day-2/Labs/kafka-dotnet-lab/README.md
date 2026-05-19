# Kafka .NET Lab

Producer + consumer for `lab-messages` on `localhost:9092` (Confluent.Kafka).

**Full guide:** [../kafka-dotnet-lab-guide.md](../kafka-dotnet-lab-guide.md)

**Build**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-dotnet-lab\src\KafkaDotNetLab
dotnet build
```

**Run**

```bat
dotnet run -- consumer
dotnet run -- producer
```

Or from `Labs/`: `run-dotnet-consumer.bat` then `run-dotnet-producer.bat`
