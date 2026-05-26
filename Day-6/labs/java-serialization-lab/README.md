# Day 6 Java Serialization Lab

Maven project for Labs 01–02 (four formats), 04–06 (Avro + Schema Registry).

Python: [python-serialization-lab](../python-serialization-lab/) · Registry URLs: [SCHEMA-REGISTRY.md](../SCHEMA-REGISTRY.md)

> **All `mvn` commands run in this folder.** Docker: [confluent-local](../../confluent-local/).

## Full command list

```powershell
$LAB = "C:\Users\om\Desktop\KafKa\Day-6\labs\java-serialization-lab"
$SCRIPTS = "C:\Users\om\Desktop\KafKa\Day-6\labs\scripts"
$DOCKER = "C:\Users\om\Desktop\KafKa\Day-6\confluent-local"

# 01–02 (no Docker)
cd $LAB
mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab01.FourFormatsLab"
mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab02.FormatBenchmark"

# 03 — infrastructure
cd $DOCKER
docker compose up -d

# 04 — producer
cd $SCRIPTS
.\create-employees-avro-topic.bat
cd $LAB
mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab04.AvroProducer"
# Verify: http://localhost:8081/subjects/employees-avro-value/versions → [1]

# 05 — consumer (once)
mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab05.AvroConsumer"

# 06 — evolution
mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab06.SchemaEvolutionDemo"
cd $SCRIPTS
.\register-schema-v2.bat
# Verify: /versions → [1, 2]

# Stop
cd $DOCKER
docker compose down
```

## Run (summary)

| Lab | Command |
|-----|---------|
| 01 | `mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab01.FourFormatsLab"` |
| 02 | `mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab02.FormatBenchmark"` |
| 04 | topic script + `...lab04.AvroProducer` |
| 05 | `...lab05.AvroConsumer` — see [lab-05 README](../lab-05-consume-avro-messages/README.md) for second-run behavior |
| 06 | `...SchemaEvolutionDemo` + `register-schema-v2.bat` |

## Lab 05 notes

- **First run:** 10 employee lines, `Read 10 records...`, process exits.
- **Second run:** SLF4J only, no lines → offsets committed; `Ctrl+C` **`Y`**.
- Offsets like `6–11` mean you ran the Lab 04 producer more than once (extra messages on topic).

## Lab 06 notes

- Demo prints `Evolved-Employee` id **99**.
- `/versions` may stay `[1]` until you run `register-schema-v2.bat` → then `[1, 2]`.
- Deliverable: [evolution-notes.md](../lab-06-schema-evolution/evolution-notes.md)

Build: `mvn -q compile` (network needed first time for Confluent deps).
