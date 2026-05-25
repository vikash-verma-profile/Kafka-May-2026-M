# Lab 04 — Push Kafka Data to Elasticsearch

**Objective:** Install the Elasticsearch sink connector, index records from a Kafka topic, and visualize in Kibana.

From **Kafka_Connect_API.pptx** — Slides 23–24.

---

## Implementation

| Track | Deploy | Seed data |
|-------|--------|-----------|
| **curl** | `deploy-connector.bat ..\configs\es-orders-sink.json` | console producer |
| **Python** | `python deploy_connector.py ..\configs\es-orders-sink.json` | `python produce_orders.py` |

---

## Prerequisites

- Kafka + Connect on port 8083
- Elasticsearch 7.x/8.x on `http://localhost:9200`
- Kibana (optional) on `http://localhost:5601`
- Topic `orders-topic` with sample JSON messages

---

## Step 1 — Seed Kafka topic

```bat
bin\windows\kafka-topics.bat --create --topic orders-topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic orders-topic
```

```json
{"orderId":"o-1","customer":"Alice","total":5000}
{"orderId":"o-2","customer":"Bob","total":1200}
```

---

## Step 2 — Install Elasticsearch connector

1. Install from Confluent Hub: `confluentinc/kafka-connect-elasticsearch`
2. Place in `plugin.path`, restart Connect.

---

## Step 3 — Sink connector config (`es-orders-sink.json`)

```json
{
  "name": "es-orders-sink",
  "config": {
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "topics": "orders-topic",
    "connection.url": "http://localhost:9200",
    "type.name": "_doc",
    "key.ignore": "true",
    "schema.ignore": "true",
    "tasks.max": "2"
  }
}
```

Deploy:

```bash
curl -X POST -H "Content-Type: application/json" \
  --data @es-orders-sink.json \
  http://localhost:8083/connectors
```

---

## Step 4 — Verify indexing

```bash
curl http://localhost:9200/orders-topic/_search?pretty
```

**Expected:** Hits for your order documents.

---

## Step 5 — Kibana visualization

1. Open Kibana → **Stack Management** → **Index Patterns**
2. Create pattern `orders-topic*`
3. **Discover** → view documents
4. Create a simple dashboard: count orders, average `total` field

---

## End-to-end pipeline (slide 22)

Optional chain: **PostgreSQL → JDBC Source → `pg.orders` → ES Sink → Elasticsearch**

---

## Checkpoint

- [ ] Connector RUNNING
- [ ] Documents searchable in Elasticsearch
- [ ] Kibana shows at least one visualization

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| `mapper_parsing_exception` | Set `schema.ignore=true` for schemaless JSON |
| Index not created | Check Connect logs; verify ES URL |
| No documents | Consumer lag — check `tasks.max` and topic data |
