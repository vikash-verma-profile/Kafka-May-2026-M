# Lab 05-Build an SMT Chain

**Objective:** Chain Single Message Transforms to rename fields, drop metadata, and add an ingest timestamp.

From **Kafka_Connect_API.pptx**-Slide 30.

---

## Prerequisites

- Connect worker with a test sink or source connector
- Records with `customer_id`, `order_total`, `_meta` fields

---

## Step 1-Sample source record

```json
{
  "customer_id": 42,
  "order_total": 199.99,
  "_meta": {"source": "legacy-etl", "batch": 7}
}
```

---

## Step 2-SMT configuration

```properties
transforms=RenameKey,DropMeta,AddTimestamp

transforms.RenameKey.type=org.apache.kafka.connect.transforms.ReplaceField$Value
transforms.RenameKey.renames=customer_id:customerId,order_total:orderTotal

transforms.DropMeta.type=org.apache.kafka.connect.transforms.ReplaceField$Value
transforms.DropMeta.exclude=_meta

transforms.AddTimestamp.type=org.apache.kafka.connect.transforms.InsertField$Value
transforms.AddTimestamp.timestamp.field=ingested_at
```

Add this block to your connector `config` JSON (source or sink side).

---

## Step 3-Deploy and test

1. Add SMTs to an existing JDBC source or file-based test connector.
2. Consume output topic and verify shape:

```json
{
  "customerId": 42,
  "orderTotal": 199.99,
  "ingested_at": "2026-05-25T10:00:00.000Z"
}
```

---

## Step 4-Discussion answers

### Order: what if `AddTimestamp` runs before `DropMeta`?

`_meta` still present when timestamp added-final record may still include `_meta` if `DropMeta` runs after. **Correct order:** rename → drop `_meta` → add timestamp.

### When is an SMT the wrong tool?

- Joins across topics
- Aggregations, windowing, sessionization  
→ Use **Kafka Streams** or **ksqlDB** instead.

### Where to add a fourth SMT?

Append to `transforms` list: `transforms=RenameKey,DropMeta,AddTimestamp,RouteTopic`

---

## Checkpoint

- [ ] Field names camelCase in output
- [ ] `_meta` removed
- [ ] `ingested_at` present
- [ ] Can explain transform order

---

## Reference-common SMTs (slide 29)

| SMT | Purpose |
|-----|---------|
| `ReplaceField` | Rename / exclude fields |
| `InsertField` | Add static or timestamp field |
| `MaskField` | Mask PII |
| `RegexRouter` | Route to different topic by regex |
