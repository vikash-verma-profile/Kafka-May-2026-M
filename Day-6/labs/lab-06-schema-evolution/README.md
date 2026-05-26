# Lab 06 — Schema Evolution End-to-End

**Objective:** Evolve `Employee` schema safely (v2), trigger a breaking change (409), then fix with Avro aliases.

From **Seralization.pptx** — Slide 35.

---

## Implementation

| Track | Command |
|-------|---------|
| **Java** | `mvn -q exec:java -Dexec.mainClass=com.training.kafka.lab06.SchemaEvolutionDemo` |
| **Python** | `python lab06_schema_evolution.py` then re-run Lab 05 consumer |

---

## Prerequisites

- Labs 04–05 complete (`employees-avro` subject exists)
- [confluent-local](../../confluent-local/) running — Schema Registry `http://localhost:8081`, Kafka `localhost:9092`

---

## Step 1 — Baseline (v1)

Current schema: `{ id, name, dept, salary }`. Confirm:

```bash
curl http://localhost:8081/subjects/employees-avro-value/versions
```

**Expected:** `[1]` (or higher if you re-ran Lab 4)

---

## Step 2 — Evolve to v2 (add `email` with default)

Update `employee.avsc`:

```json
{"name": "email", "type": "string", "default": ""}
```

Re-run producer with new field; or POST schema manually:

```bash
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data @employee-v2.avsc \
  http://localhost:8081/subjects/employees-avro-value/versions
```

**Verify:** Lab 05 consumer still reads **old** messages (email defaults to `""`).

---

## Step 3 — Breaking change: rename `dept` → `department`

Change field name without alias. Register or produce with new schema.

**Expected:** HTTP **409** `IncompatibleSchema` from Registry (BACKWARD compatibility failure).

```bash
curl -i -X POST ...  # observe 409 in response
```

---

## Step 4 — Fix with Avro alias

```json
{"name": "department", "type": "string", "aliases": ["dept"]}
```

Register successfully. Versions should include v2 (email) and v3 (department alias).

```bash
curl http://localhost:8081/subjects/employees-avro-value/versions
```

**Expected:** `[1,2,3]` (exact numbers may vary)

---

## Step 5 — Compatibility API (optional)

Test a proposed schema before deploy:

```bash
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  -d '{"schema": "..."}' \
  "http://localhost:8081/compatibility/subjects/employees-avro-value/versions/latest"
```

---

## Step 6 — Deliverable writeup

Document in `evolution-notes.md` (or your lab journal):

| Change | Compatible? | Why |
|--------|-------------|-----|
| Add `email` with default `""` | Yes (BACKWARD) | Old readers ignore new field; new readers get default for old data |
| Rename `dept` → `department` | No | Avro treats as delete + add |
| Rename with `aliases: ["dept"]` | Yes | Registry maps old field name |

---

## Checkpoint

- [ ] v2 registered; old consumers still work
- [ ] Breaking rename rejected with 409
- [ ] Alias fix registers as new version
- [ ] Short writeup completed

---

## Reference — compatibility types (slide 25)

| Mode | Rule of thumb |
|------|----------------|
| BACKWARD | New consumer reads old data (default) |
| FORWARD | Old consumer reads new data |
| FULL | Both directions |
| NONE | No checks (dev only) |
