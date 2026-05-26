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

## Code

| Track | Path | Labs |
|-------|------|------|
| **Java** | [java-serialization-lab](java-serialization-lab/) | 01–02, 04–06 (Maven) |
| **Python** | [python-serialization-lab](python-serialization-lab/) | 01–02, 04–06 |
| **Scripts** | [scripts](scripts/) | Topics, Schema Registry checks |

Pick **one** language track; steps in each lab README list both.

## Infrastructure (Labs 03–06)

Use the **Docker Compose** stack in this repo (recommended):

```powershell
cd Day-6\confluent-local
docker compose up -d
```

Details: [confluent-local/README.md](../confluent-local/README.md).

| Endpoint | Value |
|----------|--------|
| Kafka | `localhost:9092` |
| Schema Registry | `http://localhost:8081` |
| Control Center (optional) | http://localhost:9021 |

Labs 01–02 are **offline** (no broker). Labs 04–06 need the stack running before you produce or consume.

## Prerequisites

- **Java track:** JDK 17+, Maven 3.8+
- **Python track:** Python 3.10+, `pip install -r requirements.txt` in [python-serialization-lab](python-serialization-lab/)
- **Labs 03–06:** Docker Desktop + [confluent-local](../confluent-local/) (`docker compose up -d`)

**Alternative Kafka:** [Day-5 lab-00](../../Day-5/labs/lab-00-initial-setup/README.md) or a local `KAFKA_HOME` install — still need Schema Registry on port **8081** for labs 04–06.

## Quick start (full day)

```powershell
# 1–2: serialization only (no Docker)
cd Day-6\labs\java-serialization-lab   # or python-serialization-lab
mvn -q exec:java -Dexec.mainClass=com.training.kafka.lab01.FourFormatsLab
mvn -q exec:java -Dexec.mainClass=com.training.kafka.lab02.FormatBenchmark

# 3: start stack + verify registry
cd Day-6\confluent-local
docker compose up -d
cd ..\labs\scripts
.\verify-schema-registry.bat

# 4–6: Avro on Kafka
.\create-employees-avro-topic.bat
cd ..\java-serialization-lab
mvn -q exec:java -Dexec.mainClass=com.training.kafka.lab04.AvroProducer
mvn -q exec:java -Dexec.mainClass=com.training.kafka.lab05.AvroConsumer
mvn -q exec:java -Dexec.mainClass=com.training.kafka.lab06.SchemaEvolutionDemo
```

Python: replace `mvn exec:java` lines with `python lab04_avro_producer.py`, etc. (see [python-serialization-lab/README.md](python-serialization-lab/README.md)).
