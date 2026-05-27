# Lab 06-Stream DB Changes into Kafka

**Objective:** Generate database load, observe records in Kafka, verify connector status and offsets.

From **Kafka_Connect_API.pptx**-Slide 36.

---

## Prerequisites

- JDBC source (Lab 02) **or** Debezium MySQL connector
- Connect on port 8083
- Topic e.g. `mysql-orders`

---

## Step 1-Start connector

Ensure `mysql-orders-source` (or Debezium `mysql-source`) is **RUNNING**:

```bash
curl -s http://localhost:8083/connectors/mysql-orders-source/status | jq
```

---

## Step 2-Generate load

Run a script inserting ~100 orders/sec for 2–3 minutes.

**PowerShell example:**

```powershell
.\scripts\load-orders.ps1 -Count 500
```

Or manually:

```powershell
mysql -u root -p ordersdb -e "INSERT INTO orders (customer_id, order_total) VALUES (1, 99.50);"
```

---

## Step 3-Observe Kafka topic

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic mysql-orders --from-beginning --max-messages 20
```

Confirm steady flow during load.

---

## Step 4-Verify connector offset progress

```bash
curl -s http://localhost:8083/connectors/mysql-orders-source/status | jq '.tasks[].id'
```

Inspect internal offset topic (distributed mode):

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic connect-offsets --from-beginning --max-messages 5
```

High-water mark should advance after load stops.

---

## Step 5-Tail Connect worker logs

Look for:

- `WARN` / `ERROR`-connection pool, serialization
- Poll rate metrics: `source-record-poll-rate`

---

## Step 6-Debezium variant (optional)

If using Debezium:

1. Enable binlog on MySQL (for Debezium)
2. Connector emits **INSERT/UPDATE/DELETE** with before/after images
3. Topics: `<server>.public.orders`

Compare poll-based JDBC (inserts only) vs log-based CDC (slide 35).

---

## Checkpoint

- [ ] Records visible in Kafka during load
- [ ] Connector status RUNNING, no FAILED tasks
- [ ] Offsets advance (connector resumes after restart)

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Lag grows | Increase `tasks.max`, tune `poll.interval.ms` (Lab 08) |
| Duplicate keys | Expected at-least-once; sink must upsert |
| No DELETE events | JDBC cannot-switch to Debezium |
