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
| **Scripts** | [scripts](scripts/) | Topics, Schema Registry |

Pick **one** language track; steps in each lab README list both.

## Prerequisites

- Java JDK 17+, Maven 3.8+
- Kafka broker on `localhost:9092` (see [Day-5 lab-00](../../Day-5/labs/lab-00-initial-setup/README.md) or `start-kafka-cluster.bat`)
- Lab 3+: Confluent Platform or Schema Registry standalone on port **8081**
