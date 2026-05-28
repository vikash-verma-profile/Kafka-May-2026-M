# Lab 03 — JDBC Sink Design Answers

Copy to `design-answers.md` and fill in during the lab.

Reference config: [configs/orders-sink.json](../configs/orders-sink.json)

## 1. pk.fields for upsert

For `pk.mode=record_key` on topic `orders`:

```properties
pk.fields=orderId
```

Explain: MySQL uses `ON DUPLICATE KEY UPDATE` when `insert.mode=upsert`.

## 2. Schema evolution strategy

```properties
auto.evolve=true
```

When to disable in production: _______________________________

## 3. tasks.max for 6 partitions

Recommended: `tasks.max=6`

Why not 12? _______________________________

## 4. DLQ vs fail-fast policy

| Error type | Action |
| ---------- | ------ |
| Bad JSON | |
| DB down | |
| Transient network | |

DLQ topic name: `orders-sink-dlq`
