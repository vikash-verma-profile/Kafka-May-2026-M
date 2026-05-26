# Lab 04 — Produce Avro Messages to Kafka

**Objective:** Publish 10 Avro-encoded `Employee` records to `employees-avro` with auto-registration in Schema Registry.

From **Seralization.pptx** — Slides 28–29.

---

## Run this lab (copy-paste)

| Step | Where | Command |
|------|--------|---------|
| 1. Topic | `labs\scripts` | `.\create-employees-avro-topic.bat` |
| 2. Producer | `labs\java-serialization-lab` | `mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab04.AvroProducer"` |
| 3. Verify | any | `Invoke-RestMethod http://localhost:8081/subjects` |

Complete **Lab 04 before Lab 05**. Do not run `mvn` from `confluent-local` (no `pom.xml` there).

---

## Implementation

| Track | Directory | Command |
|-------|-----------|---------|
| **Java** | `labs\java-serialization-lab` | `mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab04.AvroProducer"` |
| **Python** | `labs\python-serialization-lab` | `python lab04_avro_producer.py` |

---

## Prerequisites

- [Lab 03](../lab-03-install-schema-registry/README.md) — [confluent-local](../../confluent-local/) running (`docker compose up -d`)
- Schema Registry: `http://localhost:8081`
- Kafka: `localhost:9092`

---

## Step 1 — Avro schema (`employee.avsc`)

Already in [java-serialization-lab](../../java-serialization-lab/) — fields: `id`, `name`, `dept`, `salary`.

---

## Step 2–3 — Dependencies & producer config

Already in `pom.xml` and lab code (`schema.registry.url=http://localhost:8081`, `auto.register.schemas=true`).

---

## Step 4 — Create topic

**Option A — script (from `labs\scripts`):**

```powershell
cd C:\Users\om\Desktop\KafKa\Day-6\labs\scripts
.\create-employees-avro-topic.bat
```

**Option B — manual (from `confluent-local`, one line for PowerShell):**

```powershell
cd C:\Users\om\Desktop\KafKa\Day-6\confluent-local
docker compose exec kafka kafka-topics --create --topic employees-avro --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

If the topic already exists (`Created topic` or "already exists"), continue.

---

## Step 5 — Send 10 records

```powershell
cd C:\Users\om\Desktop\KafKa\Day-6\labs\java-serialization-lab
mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab04.AvroProducer"
```

**Expected:** 10 lines `Sent {"id": ...}` and `Done. Check Schema Registry...`

---

## Step 6 — Verify Schema Registry

See [SCHEMA-REGISTRY.md](../SCHEMA-REGISTRY.md) for all URLs.

**Browser:**

| URL | Expected |
|-----|----------|
| http://localhost:8081/subjects | `["employees-avro-value"]` |
| http://localhost:8081/subjects/employees-avro-value/versions | `[1]` |
| http://localhost:8081/subjects/employees-avro-value/versions/1 | JSON schema with 4 fields |

**PowerShell:**

```powershell
Invoke-RestMethod http://localhost:8081/subjects
Invoke-RestMethod http://localhost:8081/subjects/employees-avro-value/versions
```

Or Control Center: http://localhost:9021

---

## Step 7 — Inspect raw bytes (optional)

From `confluent-local`:

```powershell
docker compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic employees-avro --from-beginning --max-messages 1
```

Wire format: magic byte `0x00` + 4-byte schema ID + Avro payload.

---

## Checkpoint

- [ ] 10 messages on `employees-avro`
- [ ] Subject `employees-avro-value` version 1 registered
- [ ] Producer finished without errors

---

## Next

[Lab 05 — Consume Avro messages](../lab-05-consume-avro-messages/README.md)

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| `no POM in this directory` | `cd labs\java-serialization-lab` before `mvn` |
| `cd Day-6\confluent-local` not found from `labs` | Use `cd ..\confluent-local` |
| PowerShell `--topic` errors | You pasted `^` lines only — use full single-line `docker compose exec` |
| `UnknownHostException` for registry | Use `http://localhost:8081` from host apps |
| Connection refused to Kafka | `docker compose ps` in `confluent-local` |
| Consumer shows nothing (Lab 05) | Run Lab 04 producer first |
