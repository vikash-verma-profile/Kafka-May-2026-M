# Lab 05 — Build an SMT Chain

**Objective:** Chain Single Message Transforms to rename fields, drop metadata, and add an ingest timestamp.

From **Kafka_Connect_API.pptx** — Slide 30.

**Tested with:** Java 17, Kafka 4.2, MySQL JDBC source, Kafka Connect standalone on Windows.

---

## Prerequisites

- Lab 02 complete (Connect + JDBC plugin + MySQL)
- MySQL `ordersdb.orders` with columns `customer_id`, `order_total`
- Config: [configs/jdbc-source-with-smt.json](../configs/jdbc-source-with-smt.json)

---

## Lab layout

| Item | Path |
| ---- | ---- |
| SMT connector JSON | `labs\configs\jdbc-source-with-smt.json` |
| Output topic | `clean-orders` (`topic.prefix` + table name) |
| Deploy | `labs\scripts\deploy-connector.bat` |

---

## Step 1 — Sample source record (conceptual)

From MySQL / JDBC source before SMTs:

```json
{
  "customer_id": 42,
  "order_total": 199.99
}
```

If you add a `_meta` column or field in test data, `DropMeta` removes it.

---

## Step 2 — SMT configuration

Full connector config in [jdbc-source-with-smt.json](../configs/jdbc-source-with-smt.json):

```json
"transforms": "RenameKey,DropMeta,AddTimestamp",
"transforms.RenameKey.type": "org.apache.kafka.connect.transforms.ReplaceField$Value",
"transforms.RenameKey.renames": "customer_id:customerId,order_total:total",
"transforms.DropMeta.type": "org.apache.kafka.connect.transforms.ReplaceField$Value",
"transforms.DropMeta.blacklist": "_meta",
"transforms.AddTimestamp.type": "org.apache.kafka.connect.transforms.InsertField$Value",
"transforms.AddTimestamp.timestamp.field": "ingested_at"
```

**Transform order:** Rename → Drop `_meta` → Add timestamp.

---

## Step 3 — Deploy and test

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs

curl.exe -X DELETE http://localhost:8083/connectors/jdbc-source-smt-demo

.\scripts\deploy-connector.bat .\configs\jdbc-source-with-smt.json http://localhost:8083
.\scripts\connect-status.bat jdbc-source-smt-demo http://localhost:8083
```

Consume output topic:

```bat
cd /d C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic clean-orders --from-beginning --max-messages 5
```

**Expected shape (approximate):**

```json
{
  "customerId": 42,
  "total": 199.99,
  "ingested_at": "2026-05-27T10:00:00.000Z"
}
```

Insert a new MySQL row to trigger another record:

```sql
INSERT INTO orders (customer_id, order_total) VALUES (99, 55.00);
```

---

## Step 4 — Discussion answers

### Order: what if `AddTimestamp` runs before `DropMeta`?

`_meta` may still be present in the final record. **Correct order:** rename → drop `_meta` → add timestamp.

### When is an SMT the wrong tool?

- Joins across topics
- Aggregations, windowing, sessionization  

→ Use **Kafka Streams** or **ksqlDB** instead.

### Where to add a fourth SMT?

Append to the list: `transforms=RenameKey,DropMeta,AddTimestamp,RouteTopic`

---

## Checkpoint

- [ ] Field names camelCase in output (`customerId`, `total`)
- [ ] `_meta` removed (if present in source)
- [ ] `ingested_at` present
- [ ] Can explain transform order

---

## Troubleshooting

| Issue | Fix |
| ----- | --- |
| Connector not found | Lab 02 JDBC plugin setup |
| No messages on `clean-orders` | Connector RUNNING; insert new rows; check `topic.prefix` |
| Fields not renamed | Verify `transforms` order and `renames` syntax |
| HTTP 409 | DELETE connector then redeploy |

---

## Reference — common SMTs (slide 29)

| SMT | Purpose |
| --- | ------- |
| `ReplaceField` | Rename / exclude fields |
| `InsertField` | Add static or timestamp field |
| `MaskField` | Mask PII |
| `RegexRouter` | Route to different topic by regex |

---

## Related

- [Lab 01 — SMT concepts](../lab-01-identify-connect-components/README.md)
- [Lab 02 — JDBC source](../lab-02-postgresql-jdbc-source/README.md)
