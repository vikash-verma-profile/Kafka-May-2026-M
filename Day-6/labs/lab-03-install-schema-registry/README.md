# Lab 03 — Install Confluent Schema Registry

**Objective:** Start Kafka + Schema Registry locally and verify the REST API and `_schemas` topic.

From **Seralization.pptx** — Slides 22–23.

---

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- Labs 01–02 complete (optional; no broker needed for those)

---

## Option A — Docker Compose (recommended)

Uses [confluent-local](../../confluent-local/) in this repo.

### Step 1 — Start the stack

```powershell
cd Day-6\confluent-local
docker compose up -d
```

First run downloads images (~2–5 min). Later starts are faster.

### Step 2 — Check containers

```powershell
docker compose ps
```

Expect **Up** for: `zookeeper`, `kafka`, `schema-registry`, `control-center`.

| Service | Port on host |
|---------|----------------|
| Kafka | 9092 |
| Schema Registry | 8081 |
| Control Center | 9021 |

### Step 3 — Verify Schema Registry

```powershell
cd Day-6\labs\scripts
.\verify-schema-registry.bat
```

**Expected:**

- `GET /subjects` → `[]` (HTTP 200)
- `GET /config` → `"compatibilityLevel":"BACKWARD"`

PowerShell equivalent:

```powershell
Invoke-RestMethod http://localhost:8081/subjects
Invoke-RestMethod http://localhost:8081/config
```

### Step 4 — Verify Kafka (optional)

```powershell
cd Day-6\confluent-local
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

`_schemas` may not appear until Lab 04 registers the first schema — that is normal.

### Step 5 — Control Center (optional)

Open http://localhost:9021 to browse brokers, topics, and schemas after Lab 04.

### Stop when finished

```powershell
cd Day-6\confluent-local
docker compose down
```

---

## Option B — Confluent CLI (`confluent local`)

1. Install [Confluent Platform](https://www.confluent.io/download/) or Confluent CLI.
2. Set environment:

```powershell
$env:CONFLUENT_HOME = "C:\confluent"
$env:PATH = "$env:CONFLUENT_HOME\bin;$env:PATH"
```

3. Start and check:

```bash
confluent local services start
confluent local services status
```

Expect Schema Registry on **8081** and Kafka on **9092**. Then run **Step 3** verification above.

---

## Option C — Schema Registry against existing Kafka

If you already have Kafka from [Day-5](../../../Day-5/labs/lab-00-initial-setup/README.md):

1. Download `confluent-schema-registry`.
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

## Checkpoint

- [ ] `GET http://localhost:8081/subjects` returns 200 and `[]`
- [ ] Default compatibility is `BACKWARD`
- [ ] Kafka reachable on `localhost:9092` (Lab 04)
- [ ] `_schemas` topic exists **or** will appear after first registration in Lab 4

---

## Next lab

[Lab 04 — Produce Avro messages](../lab-04-produce-avro-messages/README.md) (keep `docker compose` running).

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Port 8081 or 9092 in use | `docker compose down` in other projects; stop standalone Kafka/Registry |
| Registry cannot connect to Kafka | `docker compose ps` — restart: `docker compose up -d` |
| `curl` hangs on Windows | Use `Invoke-RestMethod` or `verify-schema-registry.bat` |
| `_schemas` missing until first register | Normal — topic appears on first schema registration (Lab 4) |
