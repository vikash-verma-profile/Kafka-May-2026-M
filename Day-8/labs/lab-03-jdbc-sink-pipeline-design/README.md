# Lab 03 — Design a JDBC Sink Pipeline

**Objective:** Design connector configuration to stream `orders` from Kafka into a PostgreSQL analytics table.

From **Kafka_Connect_API.pptx** — Slide 19.

---

## Prerequisites

- Understanding of JDBC Source (Lab 02)
- Topic `orders` with sample events (key = order id)

---

## Scenario

Your team streams `orders` from Kafka into Postgres table `analytics.orders_fact` for BI.

**Starter config:**

```properties
name=orders-sink
connector.class=io.confluent.connect.jdbc.JdbcSinkConnector
topics=orders
connection.url=jdbc:postgresql://db:5432/analytics
insert.mode=upsert
pk.mode=record_key
auto.create=true
```

---

## Step 1 — Complete the design document

Answer in `design-answers.md` (create in this folder):

### 1. What `pk.fields` should you set?

For `pk.mode=record_key`, set:

```properties
pk.fields=orderId
```

(or match your record key field name). **Upsert** requires a primary key to `ON CONFLICT` update.

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
|------------|--------|
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

```json
{
  "name": "orders-sink",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
    "topics": "orders",
    "connection.url": "jdbc:postgresql://localhost:5432/analytics",
    "connection.user": "postgres",
    "connection.password": "postgres",
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

---

## Step 3 — Optional: deploy and test

If you have Connect + Postgres:

1. POST config to `http://localhost:8083/connectors`
2. Produce 5 test messages to `orders`
3. `SELECT * FROM orders_fact;` in Postgres

---

## Checkpoint

- [ ] All four discussion questions answered
- [ ] `tasks.max` justified against partition count
- [ ] DLQ policy documented

---

## Deliverable

One-page design: source topic → sink table mapping, PK strategy, error handling.
