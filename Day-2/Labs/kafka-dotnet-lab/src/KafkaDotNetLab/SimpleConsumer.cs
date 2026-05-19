using Confluent.Kafka;

namespace KafkaDotNetLab;

internal static class SimpleConsumer
{
    public static void Run(string[] args)
    {
        var bootstrap = KafkaConfig.Arg(args, 0, KafkaConfig.DefaultBootstrap);
        var topic = KafkaConfig.Arg(args, 1, KafkaConfig.DefaultTopic);
        var groupId = KafkaConfig.Arg(args, 2, KafkaConfig.DefaultGroupId);

        Console.WriteLine(
            $"Connecting to {bootstrap}, topic={topic}, group={groupId} (Ctrl+C to stop)");

        var config = new ConsumerConfig
        {
            BootstrapServers = bootstrap,
            GroupId = groupId,
            AutoOffsetReset = AutoOffsetReset.Earliest,
            EnableAutoCommit = true,
            ClientId = "dotnet-lab-consumer",
        };

        using var consumer = new ConsumerBuilder<string, string>(config).Build();
        consumer.Subscribe(topic);

        using var cts = new CancellationTokenSource();
        Console.CancelKeyPress += (_, e) =>
        {
            e.Cancel = true;
            cts.Cancel();
        };

        try
        {
            while (!cts.Token.IsCancellationRequested)
            {
                try
                {
                    var record = consumer.Consume(cts.Token);
                    Console.WriteLine(
                        $"Received: \"{record.Message.Value}\" | " +
                        $"partition={record.Partition.Value} offset={record.Offset.Value} " +
                        $"key={record.Message.Key ?? "null"}");
                }
                catch (ConsumeException ex)
                {
                    Console.Error.WriteLine($"Consume error: {ex.Error.Reason}");
                }
            }
        }
        catch (OperationCanceledException)
        {
            Console.WriteLine("\nConsumer stopped.");
        }
        finally
        {
            consumer.Close();
        }
    }
}
