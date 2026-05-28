# Day 8 — Kafka Connect & API Integration Labs

Hands-on labs from **Kafka_Connect_API.pptx**.

**Stack:** Java 17 · Kafka 4.2 · MySQL 8 · Kafka Connect standalone (Windows) · Elasticsearch 8.15 (Docker, Lab 04)

| Lab | Folder | Topic | Time |
| --- | ------ | ----- | ---- |
| 01 | [lab-01-identify-connect-components](lab-01-identify-connect-components/README.md) | Connector anatomy | ~15 min |
| 02 | [lab-02-postgresql-jdbc-source](lab-02-postgresql-jdbc-source/README.md) | **MySQL** JDBC source | ~45 min |
| 03 | [lab-03-jdbc-sink-pipeline-design](lab-03-jdbc-sink-pipeline-design/README.md) | MySQL JDBC sink design + deploy | ~30 min |
| 04 | [lab-04-elasticsearch-sink](lab-04-elasticsearch-sink/README.md) | ES sink + Kibana (Docker) | ~45 min |
| 05 | [lab-05-smt-chain](lab-05-smt-chain/README.md) | Single Message Transforms | ~25 min |
| 06 | [lab-06-stream-db-changes-cdc](lab-06-stream-db-changes-cdc/README.md) | CDC / load test | ~40 min |
| 07 | [lab-07-connect-rest-api-curl](lab-07-connect-rest-api-curl/README.md) | REST API operations | ~25 min |
| 08 | [lab-08-tune-slow-connector](lab-08-tune-slow-connector/README.md) | Performance tuning | ~30 min |

## Repository layout

| Path | Purpose |
| ---- | ------- |
| [configs/](configs/) | Connector JSON for all labs |
| [sql/](sql/) | MySQL `init-ordersdb.sql` |
| [scripts/](scripts/) | `deploy-connector.bat`, `connect-status.bat`, `load-orders.ps1` |
| [python-connect-lab/](python-connect-lab/) | Deploy, status, produce, verify |
| [docs/connect-standalone.properties.example](docs/connect-standalone.properties.example) | Connect worker config |
| [lab-04-elasticsearch-sink/docker/](lab-04-elasticsearch-sink/docker/) | Elasticsearch + Kibana Compose |

## Prerequisites

- **Java 17** and **Kafka 4.2**
- Kafka cluster (e.g. `start-kafka-cluster.bat` → `9092`, `9094`, `9095`)
- **Kafka Connect** on `http://localhost:8083` — see Lab 02
- **MySQL 8** for Labs 2, 3, 5, 6, 8
- **Docker Desktop** for Lab 4 (Elasticsearch + Kibana)
- `curl.exe` or PowerShell `Invoke-RestMethod`

## Core setup (once per machine)

### 1. Kafka Connect `connect-standalone.properties`

```properties
bootstrap.servers=localhost:9092,localhost:9094,localhost:9095
plugin.path=C:/kafka-bin/kafka_2.13-4.2.0/plugins
listeners=http://localhost:8083
offset.storage.file.filename=C:/kafka-data/connect-offsets/connect.offsets
key.converter.schemas.enable=false
value.converter.schemas.enable=false
```

Use **forward slashes** in `plugin.path`. See [Lab 02](lab-02-postgresql-jdbc-source/README.md).

### 2. Plugins under `plugin.path`

Install **both** folders before Labs 02 and 04 (separate Confluent Hub downloads):

| Plugin folder | JARs | Labs |
| ------------- | ---- | ---- |
| `confluent-jdbc\lib\` | `kafka-connect-jdbc-*.jar`, `mysql-connector-j-*.jar` | 02, 03, 05, 06, 08 |
| `confluent-elasticsearch\lib\` | `kafka-connect-elasticsearch-*.jar` + Hub dependencies | 04 |

After any plugin change: **restart** Connect, then `GET http://localhost:8083/connector-plugins`.

### 3. MySQL

```bat
mysql -u root -p < sql\init-ordersdb.sql
```

### 4. Start Connect

```bat
cd /d C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\connect-standalone.bat config\connect-standalone.properties
```

## Lab quick reference

| Lab | Deploy config | Key topic / target |
| --- | ------------- | ------------------ |
| 02 | `mysql-orders-source.json` | `mysql-orders` |
| 03 | `orders-sink.json` | MySQL `analytics.orders_fact` |
| 04 | `es-orders-sink.json` + [Docker ES](lab-04-elasticsearch-sink/docker/) | `orders-topic` → Elasticsearch |
| 05 | `jdbc-source-with-smt.json` | `clean-orders` |
| 06 | (reuse Lab 02) + `load-orders.ps1` | `mysql-orders` |
| 07 | REST exercises on any connector | `:8083` |
| 08 | `jdbc-source-tuned.json` | `mysql-orders` |

Deploy pattern:

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs
curl.exe -X DELETE http://localhost:8083/connectors/<connector-name>
.\scripts\deploy-connector.bat .\configs\<file>.json http://localhost:8083
.\scripts\connect-status.bat <connector-name> http://localhost:8083
```

## Common errors

| Error | Fix |
| ----- | --- |
| `listNodes` timeout | Start Kafka; fix `bootstrap.servers` |
| `JdbcSourceConnector` not found | Install JDBC Hub ZIP → `confluent-jdbc\`; `plugin.path` with `/`; restart Connect |
| `ElasticsearchSinkConnector` not found; **Available** only JDBC/File/Mirror | Install ES Hub ZIP → `confluent-elasticsearch\` (not inside `confluent-jdbc`); restart Connect |
| `plugin.path` → `C:kafka-bin...` | Use forward slashes |
| `No suitable driver` (MySQL) | Add `mysql-connector-j-*.jar` to `confluent-jdbc\lib\` |
| HTTP 409 on deploy | `DELETE /connectors/<name>` then redeploy |
| HTTP 404 on connector status | Connector not deployed — POST config first |
| MySQL access denied | Update `connection.user` / `connection.password` in JSON |
| ES connection refused | Start Docker Desktop; `lab-04-elasticsearch-sink\docker\start-lab04-es.bat` |
| Docker pipe / engine not found | Docker Desktop not running |
| Kibana won’t open | Wait 2–5 min after ES healthy; try http://127.0.0.1:5601 |

## Suggested order

```
01 → 02 → 07 → 06 → 03 → 05 → 08 → 04
```

Lab 04 (Elasticsearch) can run after Lab 02; Docker required.
