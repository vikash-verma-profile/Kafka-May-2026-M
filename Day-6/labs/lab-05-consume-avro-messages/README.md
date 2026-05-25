# Lab 05 — Consume Avro Messages

**Objective:** Deserialize records from Lab 4 into typed `Employee` objects and verify offset resume after restart.

From **Seralization.pptx** — Slides 30–31.

---

## Implementation

| Track | Command |
|-------|---------|
| **Java** | `mvn -q exec:java -Dexec.mainClass=com.training.kafka.lab05.AvroConsumer` |
| **Python** | `python lab05_avro_consumer.py` |

---

## Prerequisites

- [Lab 04](../lab-04-produce-avro-messages/README.md) — topic `employees-avro` with data
- Schema Registry running on port 8081

---

## Step 1 — Consumer properties

```properties
bootstrap.servers=localhost:9092
group.id=avro-consumer-grp
key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
value.deserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer
schema.registry.url=http://localhost:8081
specific.avro.reader=true
auto.offset.reset=earliest
```

`specific.avro.reader=true` returns your generated `Employee` class, not `GenericRecord`.

---

## Step 2 — Poll and print

```java
KafkaConsumer<String, Employee> consumer = new KafkaConsumer<>(props);
consumer.subscribe(Collections.singletonList("employees-avro"));

while (true) {
    ConsumerRecords<String, Employee> records = consumer.poll(Duration.ofSeconds(1));
    for (ConsumerRecord<String, Employee> r : records) {
        Employee e = r.value();
        System.out.printf("id=%d name=%s dept=%s salary=%.0f%n",
            e.getId(), e.getName(), e.getDept(), e.getSalary());
    }
}
```

**Expected:** All 10 employees print with correct fields.

---

## Step 3 — Test offset commit and resume

1. Run consumer until all 10 records are printed.
2. Stop with `Ctrl+C` **before** processing more (if you add a producer loop later).
3. Restart the same `group.id=avro-consumer-grp`.
4. **Expected:** No duplicate output (offsets committed); or only new messages if you produced more.

To force commit explicitly:

```java
consumer.commitSync();
```

---

## Step 4 — Compare GenericRecord mode (optional)

Set `specific.avro.reader=false` and print fields via `GenericRecord.get("name")`. Note the loss of type safety.

---

## Checkpoint

- [ ] All 10 records deserialize as `Employee` POJOs
- [ ] `getName()`, `getDept()`, `getSalary()` work without cast
- [ ] Restart does not re-read entire topic from beginning (with same group id)

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| `ClassCastException` / GenericRecord | Set `specific.avro.reader=true` |
| `SerializationException` | Registry down or wrong `schema.registry.url` |
| Reads from beginning every time | New `group.id` each run — reuse `avro-consumer-grp` |
