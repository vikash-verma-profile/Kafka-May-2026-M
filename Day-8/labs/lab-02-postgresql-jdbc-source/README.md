# Lab 02 — Configure PostgreSQL JDBC Source Connector

**Objective:** Stream new rows from PostgreSQL into Kafka using the Confluent JDBC Source connector.

From **Kafka_Connect_API.pptx** — Slides 16–17.

---

## Prerequisites

- Kafka + Connect worker on `http://localhost:8083`
- PostgreSQL with database `ordersdb`, table `orders` with monotonic `id`
- JDBC driver JAR in Connect `plugin.path`

---

## Step 1 — Prepare PostgreSQL

```sql
CREATE DATABASE ordersdb;
\c ordersdb
CREATE TABLE orders (
  id SERIAL PRIMARY KEY,
  customer_id INT,
  total NUMERIC(10,2),
  created_at TIMESTAMP DEFAULT NOW()
);
INSERT INTO orders (customer_id, total) VALUES (1, 99.50), (2, 250.00);
```

---

## Step 2 — Install JDBC connector plugin

1. Download [Kafka Connect JDBC](https://www.confluent.io/hub/confluentinc/kafka-connect-jdbc) from Confluent Hub.
2. Extract to `%KAFKA_HOME%\plugins\confluent-jdbc\` (or your `plugin.path`).
3. Copy `postgresql-42.x.jar` into the same plugin folder.
4. Restart Connect worker.

---

## Step 3 — Connector JSON (`postgres-orders-source.json`)

```json
{
  "name": "postgres-orders-source",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
    "connection.url": "jdbc:postgresql://localhost:5432/ordersdb",
    "connection.user": "postgres",
    "connection.password": "postgres",
    "table.whitelist": "orders",
    "mode": "incrementing",
    "incrementing.column.name": "id",
    "poll.interval.ms": "5000",
    "topic.prefix": "postgres-",
    "tasks.max": "2"
  }
}
```

Topic name: **`postgres-orders`** (`topic.prefix` + table name).

---

## Step 4 — Deploy via REST API

**Shell:**

```bash
curl -X POST -H "Content-Type: application/json" \
  --data @postgres-orders-source.json \
  http://localhost:8083/connectors
```

**Python** ([python-connect-lab](../../python-connect-lab/)):

```powershell
python deploy_connector.py ..\configs\postgres-orders-source.json
```

---

## Step 5 — Verify status

```bash
curl -s http://localhost:8083/connectors/postgres-orders-source/status | jq
```

**Expected:** `"state": "RUNNING"` for connector and tasks.

---

## Step 6 — Insert new rows and consume

```sql
INSERT INTO orders (customer_id, total) VALUES (3, 1200.00);
```

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic postgres-orders --from-beginning
```

**Expected:** JSON or Avro records (per converter config) for each row.

---

## Checkpoint

- [ ] Connector RUNNING
- [ ] Topic `postgres-orders` created
- [ ] New INSERTs appear within `poll.interval.ms`

---

## Limitations (slide 14)

JDBC **incrementing** mode does not capture **DELETEs**. For full CDC use Debezium (Lab 06).

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| `ClassNotFoundException` JDBC | Add PostgreSQL driver to plugin path |
| No records | Check `incrementing.column.name`; reset connector with new name |
| Connection refused | Verify `connection.url` and firewall |
