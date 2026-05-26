# Day 6 — Serialization & Schema Management Labs

Hands-on labs from **Seralization.pptx**. Complete in order; Labs 4–6 require Schema Registry (Lab 3).

| Lab | Folder | Topic | Time |
|-----|--------|-------|------|
| 01 | [lab-01-serialize-pojo-four-formats](lab-01-serialize-pojo-four-formats/README.md) | JSON, XML, Avro, Protobuf | ~20 min |
| 02 | [lab-02-benchmark-formats](lab-02-benchmark-formats/README.md) | Size & speed benchmark | ~25 min |
| 03 | [lab-03-install-schema-registry](lab-03-install-schema-registry/README.md) | Confluent Schema Registry | ~15 min |
| 04 | [lab-04-produce-avro-messages](lab-04-produce-avro-messages/README.md) | Avro producer + Registry | ~30 min |
| 05 | [lab-05-consume-avro-messages](lab-05-consume-avro-messages/README.md) | Avro consumer | ~25 min |
| 06 | [lab-06-schema-evolution](lab-06-schema-evolution/README.md) | Evolution & compatibility | ~30 min |

**Registry cheat sheet:** [SCHEMA-REGISTRY.md](SCHEMA-REGISTRY.md) — subjects, `[1]` vs `[1,2]`, browser URLs.

## Code

| Track | Path | Labs |
|-------|------|------|
| **Java** | [java-serialization-lab](java-serialization-lab/) | 01–02, 04–06 (Maven) |
| **Python** | [python-serialization-lab](python-serialization-lab/) | 01–02, 04–06 |
| **Scripts** | [scripts](scripts/) | Topics, Schema Registry |

Pick **one** language track; steps in each lab README list both.

---

## Project layout (read this first)

**Project root** = folder that contains both `confluent-local` and `labs` (e.g. `C:\Users\om\Desktop\KafKa\Day-6`).

```
Day-6/
├── confluent-local/            ← docker compose ONLY
└── labs/
    ├── java-serialization-lab/ ← mvn ONLY (pom.xml)
    ├── python-serialization-lab/
    ├── scripts/
    └── lab-0x-.../README.md
```

### Where to run each command

| Command type | Working directory |
|--------------|-------------------|
| `docker compose` | `confluent-local` |
| `mvn` | `labs\java-serialization-lab` |
| `python lab*.py` | `labs\python-serialization-lab` |
| `.\*.bat` | `labs\scripts` |

### Navigation (PowerShell)

| You are in… | `confluent-local` | `java-serialization-lab` |
|-------------|-------------------|---------------------------|
| Project root | `cd confluent-local` | `cd labs\java-serialization-lab` |
| `labs` | `cd ..\confluent-local` | `cd java-serialization-lab` |

Do **not** use `cd Day-6\...` from inside `labs`. Do **not** run `mvn` from `confluent-local`.

### PowerShell tips

- Single-line commands only (`^` is for cmd.exe).
- Quote Maven: `"-Dexec.mainClass=..."`
- `Ctrl+C` → **`Y`** to stop a hung consumer.

---

## Lab order (04 → 05 → 06)

1. **Lab 04** — 10 Avro messages → subject `employees-avro-value`, version `[1]`
2. **Lab 05** — consumer reads 10 records once; second run idle = offset resume OK
3. **Lab 06** — `SchemaEvolutionDemo` + register v2 + optional 409/alias exercises

### Completion checklist

| Lab | Done when |
|-----|-----------|
| 03 | `docker compose ps` all Up; `/subjects` → `[]` |
| 04 | Producer prints 10 `Sent ...`; `/subjects` → `["employees-avro-value"]`; `/versions` → `[1]` |
| 05 | First run: 10 lines + `Read 10 records...`; second run: no duplicates (idle OK) |
| 06 | Demo prints `Evolved-Employee`; `/versions` → `[1,2]` after v2; notes in [evolution-notes.md](lab-06-schema-evolution/evolution-notes.md) |

---

## Infrastructure (Labs 03–06)

```powershell
cd C:\Users\om\Desktop\KafKa\Day-6\confluent-local
docker compose up -d
```

| Endpoint | Value |
|----------|--------|
| Kafka | `localhost:9092` |
| Schema Registry | http://localhost:8081 |
| Control Center | http://localhost:9021 |

Details: [confluent-local/README.md](../confluent-local/README.md).

Labs 01–02 need **no** broker.

## Prerequisites

- Java: JDK 17+, Maven 3.8+ **or** Python 3.10+
- Labs 03–06: Docker Desktop + [confluent-local](../confluent-local/)

## Quick start (Java, full day)

```powershell
$ROOT = "C:\Users\om\Desktop\KafKa\Day-6"

# 01–02
cd $ROOT\labs\java-serialization-lab
mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab01.FourFormatsLab"
mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab02.FormatBenchmark"

# 03
cd $ROOT\confluent-local
docker compose up -d
cd $ROOT\labs\scripts
.\verify-schema-registry.bat

# 04
.\create-employees-avro-topic.bat
cd $ROOT\labs\java-serialization-lab
mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab04.AvroProducer"
# Browser: http://localhost:8081/subjects/employees-avro-value/versions  → [1]

# 05
mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab05.AvroConsumer"
# Second run optional (idle = OK)

# 06
mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab06.SchemaEvolutionDemo"
cd $ROOT\labs\scripts
.\register-schema-v2.bat
# Browser: /versions → [1, 2]

# Wrap up
cd $ROOT\confluent-local
docker compose down
```

Python: [python-serialization-lab/README.md](python-serialization-lab/README.md).

## After Day 6

- Fill [lab-06-schema-evolution/evolution-notes.md](lab-06-schema-evolution/evolution-notes.md)
- Stop stack: `docker compose down` in `confluent-local`
