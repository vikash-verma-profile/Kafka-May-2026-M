# Lab 11- Structured JSON Messages

**Objective:** Send rich order events for analytics and downstream services.

## Example event

```json
{
  "orderId": 1001,
  "customer": "Vikash",
  "amount": 4500,
  "payment": "UPI"
}
```

## Benefits

- Easier parsing in consumers (Spark, Flink, microservices)
- Clear contract for event-driven design
- Room for schema evolution (Avro/JSON Schema in production)

## Java- Step by step

1. Open `JsonOrderProducer.java`
2. Run:

```powershell
cd Day-3\Labs\java-kafka-producer-lab
mvn -q exec:java "-Dexec.mainClass=com.kafka.producer.lab.JsonOrderProducer"
```

3. Consumer shows JSON strings on `orders-topic`

## Python- Step by step

```powershell
cd Day-3\Labs\python-kafka-producer-lab
python json_order_producer.py
```

Uses `key_serializer` + `value_serializer` for order ID keys.

## Exercise

1. Add a field `"currency": "INR"` to both producers.
2. Consume and parse JSON in a small Python script.

## Production note

For strict contracts, use **Schema Registry** (Avro/Protobuf). JSON is fine for learning labs.

## Next lab

→ [Lab 12- Multi-Partition Exercise](../lab-12-multi-partition/README.md)
