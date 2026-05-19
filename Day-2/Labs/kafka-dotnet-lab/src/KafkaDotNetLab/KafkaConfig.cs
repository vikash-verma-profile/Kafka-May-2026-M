namespace KafkaDotNetLab;

internal static class KafkaConfig
{
    public const string DefaultBootstrap = "localhost:9092";
    public const string DefaultTopic = "lab-messages";
    public const string DefaultGroupId = "dotnet-lab-group";
    public const int DefaultMessageCount = 5;

    public static string Arg(string[] args, int index, string defaultValue) =>
        args.Length > index ? args[index] : defaultValue;

    public static int ParseInt(string value, int defaultValue) =>
        int.TryParse(value, out var n) ? n : defaultValue;
}
