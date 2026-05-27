# Day 8 — Kafka Connect & API Integration Labs

Hands-on labs from **Kafka_Connect_API.pptx**.

| Lab | Folder | Topic | Time |
| --- | ------ | ----- | ---- |
| 01 | [lab-01-identify-connect-components](lab-01-identify-connect-components/README.md) | Connector anatomy | ~15 min |
| 02 | [lab-02-postgresql-jdbc-source](lab-02-postgresql-jdbc-source/README.md) | **MySQL** JDBC source connector | ~45 min |
| 03 | [lab-03-jdbc-sink-pipeline-design](lab-03-jdbc-sink-pipeline-design/README.md) | JDBC sink design | ~30 min |
| 04 | [lab-04-elasticsearch-sink](lab-04-elasticsearch-sink/README.md) | ES sink + Kibana | ~45 min |
| 05 | [lab-05-smt-chain](lab-05-smt-chain/README.md) | Single Message Transforms | ~25 min |
| 06 | [lab-06-stream-db-changes-cdc](lab-06-stream-db-changes-cdc/README.md) | CDC / load test | ~40 min |
| 07 | [lab-07-connect-rest-api-curl](lab-07-connect-rest-api-curl/README.md) | REST API operations | ~25 min |
| 08 | [lab-08-tune-slow-connector](lab-08-tune-slow-connector/README.md) | Performance tuning | ~30 min |

## Code

| Track | Path | Purpose |
| ----- | ---- | ------- |
| **Configs** | [configs](configs/) | Connector JSON (`mysql-orders-source.json`, etc.) |
| **SQL** | [sql](sql/) | MySQL init (`init-ordersdb.sql`) |
| **Shell** | [scripts](scripts/) | `deploy-connector.bat`, `connect-status.bat`, `load-orders.ps1` |
| **Python** | [python-connect-lab](python-connect-lab/) | REST deploy, verify topics |
| **Example** | [docs/connect-standalone.properties.example](docs/connect-standalone.properties.example) | Connect worker config (Windows) |

## Prerequisites

- **Java 17** and **Kafka 4.2** (tested locally)
- Kafka cluster + **Kafka Connect** (standalone or distributed) on port **8083**
- **MySQL 8** for Labs 2, 3, 6 (not PostgreSQL)
- Optional: Elasticsearch, Kibana for Labs 4, 6
- `curl` (or PowerShell `curl.exe` / `Invoke-RestMethod`)

## Windows quick start (Lab 02)

Full details: [lab-02 README](lab-02-postgresql-jdbc-source/README.md).

1. **Start Kafka** (e.g. `start-kafka-cluster.bat` → brokers on `9092`, `9094`, `9095`).
2. **Plugins** — under `plugins\confluent-jdbc\lib\` put:
   - `kafka-connect-jdbc-10.9.3.jar`
   - `mysql-connector-j-9.7.0.jar` ([MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) → **Platform Independent** → ZIP)
3. **`connect-standalone.properties`** — use forward slashes:

   ```properties
   bootstrap.servers=localhost:9092,localhost:9094,localhost:9095
   plugin.path=C:/kafka-bin/kafka_2.13-4.2.0/plugins
   listeners=http://localhost:8083
   offset.storage.file.filename=C:/kafka-data/connect-offsets/connect.offsets
   ```

4. **Start Connect:** `bin\windows\connect-standalone.bat config\connect-standalone.properties`
5. **MySQL:** run [sql/init-ordersdb.sql](sql/init-ordersdb.sql); set password in [configs/mysql-orders-source.json](configs/mysql-orders-source.json).
6. **Deploy:**

   ```powershell
   cd labs
   .\scripts\deploy-connector.bat .\configs\mysql-orders-source.json http://localhost:8083
   ```

7. **Verify:** `http://localhost:8083/connectors/mysql-orders-source/status` → `RUNNING`

## Common errors

| Error | See |
| ----- | --- |
| `listNodes` / cannot describe Kafka cluster | Start Kafka; fix `bootstrap.servers` |
| `JdbcSourceConnector` not found (400) | Fix `plugin.path` (use `/`, parent `plugins` folder); restart Connect |
| `No suitable driver` for `jdbc:mysql` | Add `mysql-connector-j-*.jar` to `lib\`; restart Connect |
| Connector already exists (409) | `DELETE /connectors/mysql-orders-source` then redeploy |
| Access denied to MySQL | Update `connection.user` / `connection.password` in connector JSON |
