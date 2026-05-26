# Lab 06 — Schema Evolution End-to-End

**Objective:** Evolve `Employee` schema safely (v2), trigger a breaking change (409), then fix with Avro aliases.

From **Seralization.pptx** — Slide 35.

**Registry reference:** [SCHEMA-REGISTRY.md](../SCHEMA-REGISTRY.md)

---

## Run this lab (ordered steps)

### A — Automated demo (Java)

```powershell
cd C:\Users\om\Desktop\KafKa\Day-6\labs\java-serialization-lab
mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab06.SchemaEvolutionDemo"
```

**Expected output:**

```text
Sent evolved record: {"id": 99, "name": "Evolved-Employee", "dept": "Sales", "salary": 60000.0}
Verify versions: curl http://localhost:8081/subjects/employees-avro-value/versions
```

This sends **one** new Kafka message. Schema may still be **v1** until you register v2 (step B).

| Track | Command |
|-------|---------|
| **Java** | above |
| **Python** | `python lab06_schema_evolution.py` in `labs\python-serialization-lab` |

### B — Register schema v2 (add `email`)

File: `java-serialization-lab\src\main\avro\employee_v2.avsc`

```powershell
cd C:\Users\om\Desktop\KafKa\Day-6\labs\scripts
.\register-schema-v2.bat
```

Or see [scripts/README.md](../scripts/README.md) for PowerShell `Invoke-RestMethod` if the batch file fails.

**Verify in browser:**

http://localhost:8081/subjects/employees-avro-value/versions

**Expected:** `[1, 2]`

View v2 JSON: http://localhost:8081/subjects/employees-avro-value/versions/2

### C — Optional: read evolved message

Lab 05 consumer reads **10** records per run. After only one new message, it may print **one** line (`id=99`) then idle.

```powershell
mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab05.AvroConsumer"
```

`Ctrl+C` → **`Y`** if it hangs before 10 lines.

### D — Breaking change + alias (classroom / optional)

| Step | Action | Expected |
|------|--------|----------|
| 3 | Rename `dept` → `department` without alias | HTTP **409** from Registry |
| 4 | Register with `"aliases": ["dept"]` | New version; `/versions` → `[1, 2, 3]` |

### E — Deliverable

Copy or edit [evolution-notes.md](evolution-notes.md).

---

## Prerequisites

- Labs 04–05 complete
- [confluent-local](../../confluent-local/) running
- Baseline: http://localhost:8081/subjects/employees-avro-value/versions → `[1]`

---

## Schema Registry — what you should see

| Stage | `/subjects` | `/versions` |
|-------|-------------|-------------|
| After Lab 04 | `["employees-avro-value"]` | `[1]` |
| After demo only | same | often still `[1]` |
| After v2 register | same | `[1, 2]` |
| After alias exercise | same | `[1, 2, 3]` |

Subject name **`employees-avro-value`** = value schema for topic **`employees-avro`**. Not an error.

---

## Step details (slides)

### Step 1 — Baseline (v1)

Fields: `id`, `name`, `dept`, `salary`. Versions URL shows `[1]`.

### Step 2 — v2 with `email`

```json
{"name": "email", "type": "string", "default": ""}
```

Old consumers still read old messages; `email` defaults to `""`.

### Step 3 — Breaking rename

`dept` → `department` without alias → **409** `IncompatibleSchema` (BACKWARD).

### Step 4 — Fix with alias

```json
{"name": "department", "type": "string", "aliases": ["dept"]}
```

### Step 5 — Compatibility API (optional)

```powershell
# POST proposed schema to compatibility endpoint (see Confluent docs)
```

---

## Checkpoint

- [ ] `SchemaEvolutionDemo` sent `Evolved-Employee` (id 99)
- [ ] `/versions` shows `[1, 2]` after v2
- [ ] (Optional) 409 on bad rename; alias registers v3
- [ ] [evolution-notes.md](evolution-notes.md) completed

---

## Next — wrap up Day 6

```powershell
cd C:\Users\om\Desktop\KafKa\Day-6\confluent-local
docker compose down
```

---

## Reference — compatibility modes

| Mode | Rule of thumb |
|------|----------------|
| BACKWARD | New consumer reads old data (default) |
| FORWARD | Old consumer reads new data |
| FULL | Both directions |
| NONE | No checks (dev only) |
