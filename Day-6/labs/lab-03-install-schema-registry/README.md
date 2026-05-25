# Lab 03 — Install Confluent Schema Registry

**Objective:** Start Kafka + Schema Registry locally and verify the REST API and `_schemas` topic.

From **Seralization.pptx** — Slides 22–23.

---

## Prerequisites

- Kafka broker on `localhost:9092`
- Confluent Platform tarball **or** Schema Registry standalone JAR

---

## Option A — Confluent local (recommended for class)

### Step 1 — Install Confluent Platform

1. Download [Confluent Platform](https://www.confluent.io/download/) or use Confluent CLI.
2. Set environment:

```powershell
$env:CONFLUENT_HOME = "C:\confluent"
$env:PATH = "$env:CONFLUENT_HOME\bin;$env:PATH"
```

### Step 2 — Start services

```bash
confluent local services start
```

### Step 3 — Check status

```bash
confluent local services status
```

Expect **Schema Registry** on port **8081** and **Kafka** on **9092**.

---

## Option B — Schema Registry against existing Kafka

1. Download `confluent-schema-registry` package.
2. Edit `etc/schema-registry/schema-registry.properties`:

```properties
listeners=http://0.0.0.0:8081
kafkastore.bootstrap.servers=PLAINTEXT://localhost:9092
```

3. Start:

```bash
schema-registry-start etc/schema-registry/schema-registry.properties
```

---

## Step 4 — Verify REST API

```bash
curl http://localhost:8081/subjects
```

**Expected:** `[]` (HTTP 200)

```bash
curl http://localhost:8081/config
```

**Expected:** `{"compatibilityLevel":"BACKWARD"}` (or similar)

---

## Step 5 — Verify backing topic

```bat
cd %KAFKA_HOME%
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list
```

Look for **`_schemas`** (internal, compacted topic).

---

## Checkpoint

- [ ] `GET /subjects` returns 200 and `[]`
- [ ] `_schemas` topic exists
- [ ] Default compatibility is BACKWARD

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Port 8081 in use | Change `listeners` in schema-registry.properties |
| Registry cannot connect to Kafka | Fix `kafkastore.bootstrap.servers` |
| `_schemas` missing until first register | Normal — topic appears on first schema registration (Lab 4) |
