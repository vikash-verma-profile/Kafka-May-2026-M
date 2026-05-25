# Day 8 — Kafka Connect & API Integration Labs

Hands-on labs from **Kafka_Connect_API.pptx**.

| Lab | Folder | Topic | Time |
|-----|--------|-------|------|
| 01 | [lab-01-identify-connect-components](lab-01-identify-connect-components/README.md) | Connector anatomy | ~15 min |
| 02 | [lab-02-postgresql-jdbc-source](lab-02-postgresql-jdbc-source/README.md) | JDBC source connector | ~45 min |
| 03 | [lab-03-jdbc-sink-pipeline-design](lab-03-jdbc-sink-pipeline-design/README.md) | JDBC sink design | ~30 min |
| 04 | [lab-04-elasticsearch-sink](lab-04-elasticsearch-sink/README.md) | ES sink + Kibana | ~45 min |
| 05 | [lab-05-smt-chain](lab-05-smt-chain/README.md) | Single Message Transforms | ~25 min |
| 06 | [lab-06-stream-db-changes-cdc](lab-06-stream-db-changes-cdc/README.md) | CDC / load test | ~40 min |
| 07 | [lab-07-connect-rest-api-curl](lab-07-connect-rest-api-curl/README.md) | REST API operations | ~25 min |
| 08 | [lab-08-tune-slow-connector](lab-08-tune-slow-connector/README.md) | Performance tuning | ~30 min |

## Code

| Track | Path | Purpose |
|-------|------|---------|
| **Configs** | [configs](configs/) | Connector JSON (all tracks) |
| **SQL** | [sql](sql/) | PostgreSQL init |
| **Shell** | [scripts](scripts/) | `.bat` deploy / load |
| **Python** | [python-connect-lab](python-connect-lab/) | REST deploy, verify topics |

## Prerequisites

- Kafka cluster + **Kafka Connect** worker (distributed or standalone) on port **8083**
- Optional: PostgreSQL, Elasticsearch, Kibana for Labs 2, 4, 6
- `curl` and `jq` (or PowerShell equivalents)
