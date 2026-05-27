# Connector configs

| File | Purpose |
| ---- | ------- |
| [mysql-orders-source.json](mysql-orders-source.json) | Lab 02 — MySQL JDBC source → topic `mysql-orders` |
| [orders-sink.json](orders-sink.json) | Lab 03 — JDBC sink to MySQL `analytics` |
| [jdbc-source-with-smt.json](jdbc-source-with-smt.json) | Lab 05 — SMT chain demo |
| [jdbc-source-tuned.json](jdbc-source-tuned.json) | Lab 08 — tuned source |
| [es-orders-sink.json](es-orders-sink.json) | Lab 04 — Elasticsearch sink |

## MySQL credentials (Lab 02)

Edit `connection.user` and `connection.password` in `mysql-orders-source.json` to match your MySQL login (same as MySQL Workbench).

Database access is **not** configured in `connect-standalone.properties` — only in this connector JSON.

## Deploy (Windows)

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs
curl.exe -X DELETE http://localhost:8083/connectors/mysql-orders-source
.\scripts\deploy-connector.bat .\configs\mysql-orders-source.json http://localhost:8083
```

See [Lab 02 README](../lab-02-postgresql-jdbc-source/README.md) for full setup (plugins, `plugin.path`, MySQL driver JAR).
