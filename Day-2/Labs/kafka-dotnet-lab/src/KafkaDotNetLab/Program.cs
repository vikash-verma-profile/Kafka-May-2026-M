using KafkaDotNetLab;

// Usage:
//   dotnet run -- consumer
//   dotnet run -- producer
//   dotnet run -- producer localhost:9092 lab-messages 3

var mode = args.FirstOrDefault()?.ToLowerInvariant() ?? "producer";
var rest = args.Skip(1).ToArray();

if (mode is "consumer" or "c")
{
    SimpleConsumer.Run(rest);
}
else if (mode is "producer" or "p")
{
    await SimpleProducer.RunAsync(rest);
}
else
{
    Console.WriteLine("Usage:");
    Console.WriteLine("  dotnet run -- consumer [bootstrap] [topic] [groupId]");
    Console.WriteLine("  dotnet run -- producer  [bootstrap] [topic] [count]");
    Environment.Exit(1);
}
