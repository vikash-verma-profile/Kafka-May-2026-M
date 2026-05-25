# Lab 03-Filtering and Aggregation

**Objective:** Filter invalid order records, aggregate metrics by key, write results to an output topic, and verify with a console consumer.

From **Kafka_Streams.pptx**-Slide 34.

---

## Implementation

| Track | Command |
|-------|---------|
| **Java** | `com.training.kafka.streams.lab03.OrderFilterAggregateApp` |
| **Python** | `python lab03_filter_aggregate.py` |

---

## Prerequisites

- Kafka Streams project from Labs 01–02
- Topic `orders-raw` and `orders-metrics`

---

## Step 1-Sample event format (JSON string)

```json
{"orderId":"o-1","customerId":"c-9","amount":7500,"currency":"INR","valid":true}
```

Invalid examples: `null` value, missing `amount`, `amount < 0`, `valid:false`.

---

## Step 2-Create topics

```bat
bin\windows\kafka-topics.bat --create --topic orders-raw --bootstrap-server localhost:9092 --partitions 6 --replication-factor 1
bin\windows\kafka-topics.bat --create --topic orders-metrics --bootstrap-server localhost:9092 --partitions 6 --replication-factor 1
```

---

## Step 3-Filter invalid records

```java
KStream<String, String> orders = builder.stream("orders-raw");

KStream<String, String> valid = orders.filter((key, value) -> {
    try {
        JsonNode node = mapper.readTree(value);
        return node.path("valid").asBoolean(true)
            && node.path("amount").asDouble(0) > 0;
    } catch (Exception e) {
        return false;
    }
});
```

---

## Step 4-High-value filter (slide 35)

```java
KStream<String, String> highValue = valid.filter((k, v) -> {
    JsonNode n = mapper.readTree(v);
    return n.path("amount").asDouble() >= 5000;
});
```

Route critical events to `orders-critical` with `.to("orders-critical")` if desired.

---

## Step 5-Aggregate by customer

```java
KTable<String, Double> totalByCustomer = valid
    .groupBy((k, v) -> mapper.readTree(v).path("customerId").asText())
    .aggregate(
        () -> 0.0,
        (customerId, json, total) -> total + mapper.readTree(json).path("amount").asDouble(),
        Materialized.as("customer-totals-store")
    );

totalByCustomer.toStream()
    .mapValues(total -> String.format("{\"customerId\":\"%s\",\"total\":%.2f}", /* key */, total))
    .to("orders-metrics");
```

---

## Step 6-Verify

1. Produce mix of valid/invalid/high-value JSON to `orders-raw`.
2. Consume `orders-metrics`-totals per `customerId` should increase.
3. Confirm invalid lines never appear in aggregates.

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic orders-metrics --from-beginning
```

---

## Discussion tie-in (slide 18)

| Operation | S or T? |
|-----------|---------|
| `filter()` | Stateless |
| `count()` / `aggregate()` | Stateful |
| `join()` | Stateful |

---

## Checkpoint

- [ ] Malformed and invalid orders dropped
- [ ] Aggregates correct per customer
- [ ] Output verifiable via console consumer

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Aggregate always zero | JSON parse path wrong; log filtered stream |
| Repartition storm | Use `groupBy` only after `map` to correct key |
| Duplicate metrics | New `application.id` resets state-use fixed id |
