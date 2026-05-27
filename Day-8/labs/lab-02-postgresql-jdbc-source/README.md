# Lab 02 — Configure MySQL JDBC Source Connector

**Objective:** Stream new rows from MySQL into Kafka using the Confluent JDBC Source connector.

From **Kafka_Connect_API.pptx** — Slides 16–17.

---

## Prerequisites

- Kafka + Connect worker on `http://localhost:8083`
- MySQL with database `ordersdb`, table `orders` with monotonic `id`
- JDBC driver JAR in Connect `plugin.path`

---

## Step 1 — Prepare MySQL

Run the init script from the repo root:

```powershell
mysql -u root -p < ..\sql\init-ordersdb.sql
```

Or manually:

```sql
CREATE DATABASE IF NOT EXISTS ordersdb;
USE ordersdb;
CREATE TABLE orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL,
  order_total DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO orders (customer_id, order_total) VALUES (1, 99.50), (2, 250.00);
```

Adjust `connection.user` / `connection.password` in the connector JSON if your local MySQL credentials differ from `root` / `root`.

---

## Step 2 — Install JDBC connector plugin

1. Download [Kafka Connect JDBC](https://www.confluent.io/hub/confluentinc/kafka-connect-jdbc) from Confluent Hub.
2. Extract to `%KAFKA_HOME%\plugins\confluent-jdbc\` (or your `plugin.path`).
3. Copy `mysql-connector-j-8.x.jar` into the same plugin folder.
4. Restart Connect worker.

---

## Step 3 — Connector JSON (`mysql-orders-source.json`)

```json
{
  "name": "mysql-orders-source",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
    "connection.url": "jdbc:mysql://localhost:3306/ordersdb?useSSL=false&allowPublicKeyRetrieval=true",
    "connection.user": "root",
    "connection.password": "root",
    "table.whitelist": "orders",
    "mode": "incrementing",
    "incrementing.column.name": "id",
    "poll.interval.ms": "5000",
    "topic.prefix": "mysql-",
    "tasks.max": "2"
  }
}
```

Topic name: **`mysql-orders`** (`topic.prefix` + table name).

---

## Step 4 — Deploy via REST API

**Shell:**

```bash
curl -X POST -H "Content-Type: application/json" \
  --data @mysql-orders-source.json \
  http://localhost:8083/connectors
```

**Python** ([python-connect-lab](../../python-connect-lab/)):

```powershell
python deploy_connector.py ..\configs\mysql-orders-source.json
```

---

## Step 5 — Verify status

```bash
curl -s http://localhost:8083/connectors/mysql-orders-source/status | jq
```

**Expected:** `"state": "RUNNING"` for connector and tasks.

---

## Step 6 — Insert new rows and consume

```sql
INSERT INTO orders (customer_id, order_total) VALUES (3, 1200.00);
```

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic mysql-orders --from-beginning
```

**Expected:** JSON or Avro records (per converter config) for each row.

---

## Checkpoint

- [ ] Connector RUNNING
- [ ] Topic `mysql-orders` created
- [ ] New INSERTs appear within `poll.interval.ms`

---

## Limitations (slide 14)

JDBC **incrementing** mode does not capture **DELETEs**. For full CDC use Debezium (Lab 06).

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| `ClassNotFoundException` JDBC | Add MySQL Connector/J driver to plugin path |
| No records | Check `incrementing.column.name`; reset connector with new name |
| Connection refused | Verify `connection.url`, port `3306`, and firewall |
| Auth failed | Match `connection.user` / `connection.password` to your MySQL account |
