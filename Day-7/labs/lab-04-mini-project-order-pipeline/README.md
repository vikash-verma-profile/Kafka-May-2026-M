# Lab 04-Mini Project: Real-Time Order Processing Pipeline

**Objective:** Build an end-to-end pipeline: ingest orders, validate, aggregate sales, detect high-value orders, emit analytics.

From **Kafka_Streams.pptx**-Slides 36–38.

---

## Architecture

```text
Order Producer → orders (topic)
                      ↓
              Kafka Streams App
         (filter + aggregate + branch)
                      ↓
         ┌────────────┴────────────┐
   order-analytics          high-value-orders
         ↓                        ↓
   Dashboard consumer      Alert consumer
```

---

## Implementation

| Track | Command |
|-------|---------|
| **Java** | `com.training.kafka.streams.lab04.OrderPipelineApp` + `OrderProducer` |
| **Python** | `python order_producer.py` then `python lab04_order_pipeline.py` |

---

## Prerequisites

- Labs 01–03
- Optional: Schema Registry for Avro orders (Day 6)

---

## Step 1-Topics

| Topic | Purpose |
|-------|---------|
| `orders` | Raw order events |
| `order-analytics` | Per-region totals, counts |
| `high-value-orders` | Orders ≥ ₹5,000 |
| `invalid-orders` | Quarantine (optional) |

```bat
for %%T in (orders order-analytics high-value-orders invalid-orders) do (
  bin\windows\kafka-topics.bat --create --topic %%T --bootstrap-server localhost:9092 --partitions 6 --replication-factor 1 2>nul
)
```

---

## Step 2-Order producer

Generate events with fields: `orderId`, `customerId`, `region`, `amount`, `timestamp`, `sku`.

Include ~10% invalid (negative amount, blank region) for filter testing.

Run producer in a loop or use a small Java `OrderProducer` class.

---

## Step 3-Streams topology

Implement:

1. **Validation**-`filter` valid; `branch` invalid to `invalid-orders`
2. **High-value**-`amount >= 5000` → `high-value-orders`
3. **Aggregation**-`groupBy` region → `count` + `aggregate` sum of amount → `order-analytics`
4. **Windowing (stretch)**-1-minute tumbling window for hourly-style demo:

```java
valid.groupBy((k, v) -> region)
    .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
    .aggregate(...);
```

---

## Step 4-Suggested features (slide 38)

| Feature | Implementation hint |
|---------|---------------------|
| Order validation | Schema check or JSON field rules |
| Inventory tracking | `KTable` join `orders` with `inventory` topic |
| Fraud rules | `amount > 100000` OR velocity per customer in window |
| Live notifications | Consumer on `high-value-orders` prints alert |

---

## Step 5-Run integration test

1. Start Streams app (`order-pipeline-app`).
2. Start analytics consumer on `order-analytics`.
3. Start alert consumer on `high-value-orders`.
4. Run producer for 2–3 minutes.
5. Verify counts and high-value stream.

---

## Step 6-Fault tolerance check (optional, slide 30)

1. Kill Streams process mid-run.
2. Restart with same `application.id`.
3. Confirm processing resumes from committed offset (no duplicate alerts if EOS enabled).

---

## Deliverables

- [ ] Runnable Streams JAR or Maven module
- [ ] README section: topology diagram (ASCII or mermaid)
- [ ] Screenshot or log snippet showing analytics + at least one high-value alert

---

## Checkpoint

- [ ] Invalid orders excluded from analytics
- [ ] Regional totals update in real time
- [ ] High-value orders appear on dedicated topic
- [ ] End-to-end demo runs without manual topic creation errors

---

## Production notes (slides 39–41)

- Containerize app; set `num.standby.replicas=1` for faster recovery
- Monitor consumer lag on output topics
- Use Schema Registry for order schema governance
