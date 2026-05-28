# Lab 06 — Stream DB Changes into Kafka

**Objective:** Generate database load, observe records in Kafka, verify connector status and offsets.

From **Kafka_Connect_API.pptx** — Slide 36.

**Tested with:** Java 17, Kafka 4.2, MySQL 8, JDBC source (incrementing mode).

---

## Prerequisites

- Lab 02 complete: `mysql-orders-source` connector, topic `mysql-orders`
- Connect on `http://localhost:8083`
- MySQL `ordersdb.orders` table
- Script: [scripts/load-orders.ps1](../scripts/load-orders.ps1)

---

## Lab layout

| Item | Path / URL |
| ---- | ---------- |
| Source connector | `configs\mysql-orders-source.json` |
| Kafka topic | `mysql-orders` |
| Load script | `scripts\load-orders.ps1` |
| Verify (Python) | `python-connect-lab\verify_topic.py` |

---

## Step 0 — Prerequisites check

```powershell
Invoke-RestMethod http://localhost:8083/connectors/mysql-orders-source/status
```

**Expected:** `"state": "RUNNING"`.

If not deployed, see [Lab 02](../lab-02-postgresql-jdbc-source/README.md).

---

## Step 1 — Generate load

**PowerShell (recommended):**

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs\scripts
.\load-orders.ps1 -Count 500 -User root -Password YOUR_PASSWORD
```

Edit `-Password` to match MySQL.

**Single insert (MySQL Workbench or CLI):**

```sql
INSERT INTO orders (customer_id, order_total) VALUES (1, 99.50);
```

---

## Step 2 — Observe Kafka topic

**Console consumer:**

```bat
cd /d C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic mysql-orders --from-beginning --max-messages 20
```

**Python:**

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs\python-connect-lab
python verify_topic.py localhost:9092 mysql-orders 20
```

Confirm steady flow during load.

---

## Step 3 — Verify connector status

```powershell
Invoke-RestMethod http://localhost:8083/connectors/mysql-orders-source/status
```

Or:

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs
.\scripts\connect-status.bat mysql-orders-source http://localhost:8083
```

---

## Step 4 — Offsets (standalone vs distributed)

**Standalone** (local lab): offsets stored in file:

```properties
offset.storage.file.filename=C:/kafka-data/connect-offsets/connect.offsets
```

Restart Connect — connector should resume from last `id`.

**Distributed** (optional): inspect internal topic:

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic connect-offsets --from-beginning --max-messages 5
```

---

## Step 5 — Tail Connect worker logs

Log file (example):

```
C:\kafka-bin\kafka_2.13-4.2.0\logs\connect.log
```

Look for:

- `WARN` / `ERROR` — connection pool, serialization
- Poll metrics: `source-record-poll-rate`

---

## Step 6 — Debezium variant (optional)

JDBC **incrementing** mode captures **INSERTs only**, not DELETEs.

For full CDC on MySQL:

1. Enable **binlog** on MySQL
2. Use **Debezium MySQL** connector
3. Topics: e.g. `dbserver.ordersdb.orders` with before/after images

Compare poll-based JDBC vs log-based CDC (slide 35).

---

## Checkpoint

- [ ] Records visible in Kafka during load
- [ ] Connector status RUNNING, no FAILED tasks
- [ ] Offsets advance (connector resumes after restart)

---

## Troubleshooting

| Issue | Fix |
| ----- | --- |
| No new records | Connector FAILED? MySQL password in JSON; driver JAR present |
| Lag grows | Tune `poll.interval.ms`, `tasks.max` — [Lab 08](../lab-08-tune-slow-connector/README.md) |
| Duplicate keys | Expected at-least-once; use upsert sink in Lab 03 |
| No DELETE events | JDBC limitation — use Debezium |
| `load-orders.ps1` fails | MySQL in PATH; correct `-Password`; `ordersdb` exists |

---

## Related

- [Lab 02 — JDBC source](../lab-02-postgresql-jdbc-source/README.md)
- [Lab 08 — Tuning](../lab-08-tune-slow-connector/README.md)
