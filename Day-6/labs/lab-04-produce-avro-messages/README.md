# Lab 04 — Produce Avro Messages to Kafka

**Objective:** Publish 10 Avro-encoded `Employee` records to `employees-avro` with auto-registration in Schema Registry.

From **Seralization.pptx** — Slides 28–29.

---

## Implementation

| Track | Command |
|-------|---------|
| **Java** | `mvn -q exec:java -Dexec.mainClass=com.training.kafka.lab04.AvroProducer` |
| **Python** | `python lab04_avro_producer.py` |

Both in their respective project folders under `Day-6/labs/`. Create topic first: [scripts/create-employees-avro-topic.bat](../scripts/create-employees-avro-topic.bat).

---

## Prerequisites

- [Lab 03](../lab-03-install-schema-registry/README.md) — Registry on `http://localhost:8081`
- Kafka on `localhost:9092`
- Maven project with Confluent Avro serdes

---

## Step 1 — Avro schema (`employee.avsc`)

```json
{
  "type": "record",
  "name": "Employee",
  "namespace": "com.training.kafka",
  "fields": [
    {"name": "id", "type": "int"},
    {"name": "name", "type": "string"},
    {"name": "dept", "type": "string"},
    {"name": "salary", "type": "double"}
  ]
}
```

Add `avro-maven-plugin` to generate the Java class.

---

## Step 2 — Maven dependencies

```xml
<dependency>
  <groupId>io.confluent</groupId>
  <artifactId>kafka-avro-serializer</artifactId>
  <version>7.6.0</version>
</dependency>
```

Add Confluent Maven repository in `pom.xml`.

---

## Step 3 — Producer properties

```properties
bootstrap.servers=localhost:9092
key.serializer=org.apache.kafka.common.serialization.StringSerializer
value.serializer=io.confluent.kafka.serializers.KafkaAvroSerializer
schema.registry.url=http://localhost:8081
auto.register.schemas=true
```

> **Production note:** Set `auto.register.schemas=false` and register schemas via CI/CD.

---

## Step 4 — Create topic

```bat
cd %KAFKA_HOME%
bin\windows\kafka-topics.bat --create ^
  --topic employees-avro ^
  --bootstrap-server localhost:9092 ^
  --partitions 3 ^
  --replication-factor 1
```

---

## Step 5 — Send 10 records

```java
Properties props = loadFrom("producer.properties");
KafkaProducer<String, Employee> producer =
    new KafkaProducer<>(props);

for (int i = 1; i <= 10; i++) {
    Employee e = Employee.newBuilder()
        .setId(i)
        .setName("Employee-" + i)
        .setDept("Engineering")
        .setSalary(50000 + i * 1000)
        .build();
    producer.send(new ProducerRecord<>("employees-avro", String.valueOf(i), e));
}
producer.flush();
producer.close();
```

---

## Step 6 — Verify Schema Registry

```bash
curl http://localhost:8081/subjects
```

**Expected:** includes `employees-avro-value`

```bash
curl http://localhost:8081/subjects/employees-avro-value/versions
```

**Expected:** `[1]`

---

## Step 7 — Inspect raw bytes (optional)

```bat
bin\windows\kafka-console-consumer.bat ^
  --bootstrap-server localhost:9092 ^
  --topic employees-avro ^
  --from-beginning ^
  --max-messages 1
```

Wire format: magic byte `0x00` + 4-byte schema ID + Avro payload.

---

## Checkpoint

- [ ] 10 messages on `employees-avro`
- [ ] Subject `employees-avro-value` version 1 registered
- [ ] Producer logs show no serialization errors

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| `UnknownHostException` for registry | Use `localhost:8081`, not container hostname |
| `Schema being registered is incompatible` | Delete subject in dev or fix schema |
| ClassNotFound for Avro serializer | Add `kafka-avro-serializer` dependency |
