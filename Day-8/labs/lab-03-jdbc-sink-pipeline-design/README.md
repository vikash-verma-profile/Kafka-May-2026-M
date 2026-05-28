# Lab 03 — Design a JDBC Sink Pipeline

**Objective:** Design connector configuration to stream `orders` from Kafka into a MySQL analytics table.

From **Kafka_Connect_API.pptx** — Slide 19.

**Tested with:** Java 17, Kafka 4.2, MySQL 8, Kafka Connect **standalone** on Windows.

---

## Prerequisites

- Lab 02 completed (Connect running, JDBC plugin + MySQL driver in `plugin.path`)
- Topic `orders` with sample JSON events (record key = order id)
- MySQL database `analytics` (created by [sql/init-ordersdb.sql](../sql/init-ordersdb.sql))

---

## Lab layout (Windows paths)

| Item | Path |
| ---- | ---- |
| Sink config | `labs\configs\orders-sink.json` |
| SQL init | `labs\sql\init-ordersdb.sql` |
| Deploy | `labs\scripts\deploy-connector.bat` |
| Connect URL | `http://localhost:8083` |

---

## Scenario

Your team streams `orders` from Kafka into MySQL table `analytics.orders_fact` for BI.

**Starter config:**

```properties
name=orders-sink
connector.class=io.confluent.connect.jdbc.JdbcSourceConnector
topics=orders
connection.url=jdbc:mysql://db:3306/analytics
insert.mode=upsert
pk.mode=record_key
auto.create=true
```

> Fix class name in your design: use **`JdbcSinkConnector`**, not `JdbcSourceConnector`.

---

## Step 1 — Complete the design document

Answer in `design-answers.md` (create in this folder) or [design-answers.template.md](design-answers.template.md).

### 1. What `pk.fields` should you set?

For `pk.mode=record_key`, set:

```properties
pk.fields=orderId
```

(or match your record key field name). **Upsert** requires a primary key for `ON DUPLICATE KEY UPDATE` in MySQL.

### 2. Schema change (new column in topic)?

Enable:

```properties
auto.evolve=true
```

Sink adds nullable columns. For production, prefer controlled migrations + `auto.evolve=false`.

### 3. `tasks.max` for 6 partitions?

Set **`tasks.max=6`** (one task per partition max). **Not 12** — Connect caps tasks at partition count; extra tasks stay idle.

### 4. DLQ vs fail connector?

| Error type | Action |
| ---------- | ------ |
| Bad JSON, type mismatch | `errors.tolerance=all` + DLQ topic |
| DB down, auth failure | Fail connector; page on-call |
| Transient network | Retry + backoff |

Example DLQ:

```properties
errors.tolerance=all
errors.deadletterqueue.topic.name=orders-sink-dlq
errors.deadletterqueue.context.headers.enable=true
```

---

## Step 2 — Full proposed config

See [configs/orders-sink.json](../configs/orders-sink.json):

```json
{
  "name": "orders-sink",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
    "topics": "orders",
    "connection.url": "jdbc:mysql://localhost:3306/analytics?useSSL=false&allowPublicKeyRetrieval=true",
    "connection.user": "root",
    "connection.password": "root",
    "insert.mode": "upsert",
    "pk.mode": "record_key",
    "pk.fields": "orderId",
    "auto.create": "true",
    "auto.evolve": "true",
    "table.name.format": "orders_fact",
    "tasks.max": "6"
  }
}
```

Edit `connection.user` / `connection.password` to match MySQL Workbench.

---

## Step 3 — Optional: deploy and test

### 3a — Prepare MySQL

```sql
CREATE DATABASE IF NOT EXISTS analytics;
```

Or run full init: `labs\sql\init-ordersdb.sql` (creates `ordersdb` and `analytics`).

### 3b — Create topic and produce sample orders

```bat
cd /d C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-topics.bat --create --topic orders --bootstrap-server localhost:9092 --partitions 6 --replication-factor 1
```

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs\python-connect-lab
python produce_orders.py localhost:9092 orders 5
```

(Change topic name in script invocation if your messages use topic `orders` — default in `produce_orders.py` is `orders-topic`; for Lab 03 use topic **`orders`**.)

### 3c — Deploy sink connector

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs

curl.exe -X DELETE http://localhost:8083/connectors/orders-sink

.\scripts\deploy-connector.bat .\configs\orders-sink.json http://localhost:8083
.\scripts\connect-status.bat orders-sink http://localhost:8083
```

### 3d — Verify in MySQL

```sql
USE analytics;
SELECT * FROM orders_fact;
```

Or:

```bat
mysql -u root -p analytics -e "SELECT * FROM orders_fact;"
```

---

## Checkpoint

- [ ] All four discussion questions answered
- [ ] `tasks.max` justified against partition count
- [ ] DLQ policy documented
- [ ] (Optional) Connector RUNNING and rows in `orders_fact`

---

## Troubleshooting

| Issue | Fix |
| ----- | --- |
| `JdbcSinkConnector` not found | Same as Lab 02: JDBC plugin + `plugin.path` with forward slashes |
| `No suitable driver` for MySQL | Add `mysql-connector-j-*.jar` to `plugins\confluent-jdbc\lib\` |
| Access denied | Update credentials in `orders-sink.json` |
| Unknown database `analytics` | `CREATE DATABASE analytics;` or run `init-ordersdb.sql` |
| No rows in table | Topic must be `orders`; produce messages; connector RUNNING |
| HTTP 409 | `DELETE /connectors/orders-sink` then redeploy |

---

## Deliverable

One-page design: source topic → sink table mapping, PK strategy, error handling.

---

## Related

- [Lab 02 — JDBC source + Connect setup](../lab-02-postgresql-jdbc-source/README.md)
- [configs/orders-sink.json](../configs/orders-sink.json)
- [Lab 04 — Elasticsearch sink](../lab-04-elasticsearch-sink/README.md)
