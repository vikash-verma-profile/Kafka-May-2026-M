# Lab 04 — Produce Avro Messages to Kafka

**Objective:** Publish 10 Avro-encoded `Employee` records to `employees-avro` with auto-registration in Schema Registry.

From **Seralization.pptx** — Slides 28–29.

---

## Implementation

| Track | Command |
|-------|---------|
| **Java** | `mvn -q exec:java -Dexec.mainClass=com.training.kafka.lab04.AvroProducer` |
| **Python** | `python lab04_avro_producer.py` |

Both in their respective project folders under `Day-6/labs/`.

**Before producing:** create the topic (see Step 4).

---

## Prerequisites

- [Lab 03](../lab-03-install-schema-registry/README.md) — [confluent-local](../../confluent-local/) running (`docker compose up -d`)
- Schema Registry: `http://localhost:8081`
- Kafka: `localhost:9092`
- Maven project with Confluent Avro serdes (Java) or `pip install -r requirements.txt` (Python)

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

Add `avro-maven-plugin` to generate the Java class (already in [java-serialization-lab](../../java-serialization-lab/)).

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

**Recommended (Docker — no `KAFKA_HOME` required):**

```powershell
cd Day-6\labs\scripts
.\create-employees-avro-topic.bat
```

The script uses `docker compose` in [confluent-local](../../confluent-local/) when `KAFKA_HOME` is not set.

**Manual (from `confluent-local` folder):**

```powershell
cd Day-6\confluent-local
docker compose exec kafka kafka-topics --create ^
  --topic employees-avro ^
  --bootstrap-server localhost:9092 ^
  --partitions 3 ^
  --replication-factor 1
```

If the topic already exists, you can continue.

**Alternative — local Kafka install:**

```bat
set KAFKA_HOME=C:\kafka
cd Day-6\labs\scripts
.\create-employees-avro-topic.bat
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

Or run the lab main class / Python script from the implementation table above.

---

## Step 6 — Verify Schema Registry

```powershell
Invoke-RestMethod http://localhost:8081/subjects
```

**Expected:** includes `employees-avro-value`

```powershell
Invoke-RestMethod http://localhost:8081/subjects/employees-avro-value/versions
```

**Expected:** `[1]`

Or use Control Center: http://localhost:9021 → Schema Registry.

---

## Step 7 — Inspect raw bytes (optional)

```powershell
cd Day-6\confluent-local
docker compose exec kafka kafka-console-consumer ^
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
| `UnknownHostException` for registry | Use `http://localhost:8081` from host apps |
| Connection refused to Kafka | `docker compose ps` in `confluent-local` |
| `Schema being registered is incompatible` | Delete subject in dev or fix schema |
| ClassNotFound for Avro serializer | Add `kafka-avro-serializer` dependency |
| Topic creation fails | Topic may already exist — proceed to producer |
