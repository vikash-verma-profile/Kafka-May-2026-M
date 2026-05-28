# Connector configs

| File | Lab | Connector name | Purpose |
| ---- | --- | -------------- | ------- |
| [mysql-orders-source.json](mysql-orders-source.json) | 02, 06, 08 | `mysql-orders-source` | MySQL JDBC source → `mysql-orders` |
| [jdbc-source-tuned.json](jdbc-source-tuned.json) | 08 | `mysql-orders-source-tuned` | Tuned JDBC source |
| [orders-sink.json](orders-sink.json) | 03 | `orders-sink` | JDBC sink → `analytics.orders_fact` |
| [jdbc-source-with-smt.json](jdbc-source-with-smt.json) | 05 | `jdbc-source-smt-demo` | SMT chain → `clean-orders` |
| [es-orders-sink.json](es-orders-sink.json) | 04 | `es-orders-sink` | ES sink → index `orders-topic` |

## Credentials

| Connector | Where to set |
| --------- | ------------ |
| MySQL (02, 03, 05, 08) | `connection.user` / `connection.password` — match MySQL Workbench |
| Elasticsearch (04) | `connection.url`: `http://localhost:9200` (Docker on host — see [Lab 04 docker](../lab-04-elasticsearch-sink/docker/README.md)) |

Not in `connect-standalone.properties`.

**Lab 04 plugin:** `es-orders-sink.json` requires `ElasticsearchSinkConnector` in Connect — install `confluent-elasticsearch` under `plugin.path` ([Lab 04 Step 1](../lab-04-elasticsearch-sink/README.md#step-1--install-elasticsearch-connect-plugin)).

## Deploy (PowerShell, from `labs`)

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs
$base = "http://localhost:8083/connectors"
```

| Lab | Commands |
| --- | -------- |
| 02 | `curl.exe -X DELETE $base/mysql-orders-source`; `.\scripts\deploy-connector.bat .\configs\mysql-orders-source.json` |
| 03 | `curl.exe -X DELETE $base/orders-sink`; `.\scripts\deploy-connector.bat .\configs\orders-sink.json` |
| 04 | `curl.exe -X DELETE $base/es-orders-sink`; `.\scripts\deploy-connector.bat .\configs\es-orders-sink.json` |
| 05 | `curl.exe -X DELETE $base/jdbc-source-smt-demo`; `.\scripts\deploy-connector.bat .\configs\jdbc-source-with-smt.json` |
| 08 | `curl.exe -X DELETE $base/mysql-orders-source-tuned`; `.\scripts\deploy-connector.bat .\configs\jdbc-source-tuned.json` |

Status:

```powershell
.\scripts\connect-status.bat <connector-name> http://localhost:8083
```

## Setup guides

| Lab | README |
| --- | ------ |
| 02 | [MySQL source + Connect](../lab-02-postgresql-jdbc-source/README.md) |
| 03 | [JDBC sink design](../lab-03-jdbc-sink-pipeline-design/README.md) |
| 04 | [ES + Kibana Docker](../lab-04-elasticsearch-sink/docker/README.md) |
| 05 | [SMT chain](../lab-05-smt-chain/README.md) |
| 06 | [CDC load test](../lab-06-stream-db-changes-cdc/README.md) |
| 07 | [REST API](../lab-07-connect-rest-api-curl/README.md) |
| 08 | [Tuning](../lab-08-tune-slow-connector/README.md) |
