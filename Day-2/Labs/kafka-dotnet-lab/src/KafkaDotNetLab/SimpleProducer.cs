using Confluent.Kafka;

namespace KafkaDotNetLab;

internal static class SimpleProducer
{
    public static async Task RunAsync(string[] args)
    {
        var bootstrap = KafkaConfig.Arg(args, 0, KafkaConfig.DefaultBootstrap);
        var topic = KafkaConfig.Arg(args, 1, KafkaConfig.DefaultTopic);
        var messageCount = KafkaConfig.ParseInt(KafkaConfig.Arg(args, 2, "5"), 5);

        Console.WriteLine(
            $"Connecting to {bootstrap}, topic={topic}, messages={messageCount}");

        var config = new ProducerConfig
        {
            BootstrapServers = bootstrap,
            ClientId = "dotnet-lab-producer",
            Acks = Acks.All,
        };

        using var producer = new ProducerBuilder<string, string>(config).Build();

        for (var i = 1; i <= messageCount; i++)
        {
            var value = $"Hello from .NET producer - message {i}";

            try
            {
                var result = await producer.ProduceAsync(
                    topic,
                    new Message<string, string> { Value = value });

                Console.WriteLine(
                    $"Sent: \"{value}\" -> partition={result.Partition.Value} offset={result.Offset.Value}");
            }
            catch (ProduceException<string, string> ex)
            {
                Console.Error.WriteLine($"Failed to send message {i}: {ex.Error.Reason}");
                Environment.Exit(1);
            }
        }

        producer.Flush(TimeSpan.FromSeconds(10));
        Console.WriteLine("Producer finished.");
    }
}
