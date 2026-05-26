# Schema Registry — Quick reference (Day 6)

Use while running Labs 04–06. Registry base URL: **http://localhost:8081**

## Subject names

| Subject | Meaning |
|---------|--------|
| `employees-avro-value` | Avro schema for the **value** on Kafka topic `employees-avro` |
| `employees-avro-key` | Would be the key schema (not used in these labs) |

Confluent naming: `<topic>-value` and `<topic>-key`.

## URLs to open in a browser

| What | URL | Expected after Lab 04 |
|------|-----|------------------------|
| All subjects | http://localhost:8081/subjects | `["employees-avro-value"]` |
| All versions | http://localhost:8081/subjects/employees-avro-value/versions | `[1]` |
| Schema JSON for v1 | http://localhost:8081/subjects/employees-avro-value/versions/1 | Record with `id`, `name`, `dept`, `salary` |
| Global compatibility | http://localhost:8081/config | `"compatibilityLevel":"BACKWARD"` |

After Lab 06 registers v2 (add `email`):

| What | Expected |
|------|----------|
| `/versions` | `[1, 2]` (or `[1, 2, 3]` after alias exercise) |

**Before Lab 04:** `/subjects` is `[]` and `/versions` does not exist yet.

## PowerShell equivalents

```powershell
Invoke-RestMethod http://localhost:8081/subjects
Invoke-RestMethod http://localhost:8081/subjects/employees-avro-value/versions
Invoke-RestMethod http://localhost:8081/subjects/employees-avro-value/versions/1
Invoke-RestMethod http://localhost:8081/config
```

## What version numbers mean

- **`[1]`** — First schema registered when Lab 04 producer sent Avro messages.
- **`[1, 2]`** — Compatible evolution (e.g. added `email` with default).
- **`[1, 2, 3]`** — Further compatible change (e.g. `department` field with alias `dept`).

Each version gets a **schema ID** embedded in every Kafka message (magic byte `0x00` + 4-byte ID + Avro bytes).

## Control Center

http://localhost:9021 → **Schema** tab → browse `employees-avro-value` versions.
