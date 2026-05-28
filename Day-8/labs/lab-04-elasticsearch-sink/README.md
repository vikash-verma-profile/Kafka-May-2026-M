# Lab 04 — Push Kafka Data to Elasticsearch

**Objective:** Install the Elasticsearch sink connector, index records from a Kafka topic, and visualize in Kibana.

From **Kafka_Connect_API.pptx** — Slides 23–24.

**Tested with:** Java 17, Kafka 4.2, Elasticsearch 8.15 (Docker), Kafka Connect **standalone** on Windows.

---

## Implementation

| Track | Deploy | Seed data |
| ----- | ------ | --------- |
| **BAT** | `.\scripts\deploy-connector.bat .\configs\es-orders-sink.json` | Kafka console producer |
| **Python** | `python deploy_connector.py configs\es-orders-sink.json` | `python produce_orders.py` |

---

## Prerequisites

- Kafka cluster + **Kafka Connect** on `http://localhost:8083` (see [Lab 02](../lab-02-postgresql-jdbc-source/README.md) for Connect + `plugin.path` setup)
- **Elasticsearch** on `http://localhost:9200` and **Kibana** on `http://localhost:5601` ([Docker](#step-0--start-elasticsearch--kibana-docker))
- Confluent **Elasticsearch** connector in Connect `plugin.path`
- Topic `orders-topic` with sample JSON messages

---

## Lab layout (Windows paths)

| Item | Example path |
| ---- | ------------- |
| Kafka home | `C:\kafka-bin\kafka_2.13-4.2.0` |
| Labs root | `C:\Users\om\Desktop\KafKa\Day-8\labs` |
| ES sink config | `labs\configs\es-orders-sink.json` |
| Docker compose | `labs\lab-04-elasticsearch-sink\docker\` |
| Deploy script | `labs\scripts\deploy-connector.bat` |

---

## Step 0 — Start Elasticsearch & Kibana (Docker)

Requires [Docker Desktop](https://www.docker.com/products/docker-desktop/) (4 GB+ RAM recommended).

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs\lab-04-elasticsearch-sink\docker
docker compose up -d
```

Or:

```bat
lab-04-elasticsearch-sink\docker\start-lab04-es.bat
```

| Service | URL |
| ------- | --- |
| Elasticsearch | http://localhost:9200 |
| Kibana | http://localhost:5601 |

Verify:

```powershell
curl.exe http://localhost:9200
docker compose ps
curl.exe http://localhost:5601/api/status
```

**Docker Desktop must be running** before `docker compose up`. If you see `open //./pipe/dockerDesktopLinuxEngine: The system cannot find the file specified`, start Docker Desktop and retry.

`docker compose ps` should show **healthy** for `elasticsearch` before Kibana finishes starting. First boot can take **1–3 minutes**; if ES stays `unhealthy`, run `docker compose restart elasticsearch` and wait again.

**Kibana** often needs **2–5 minutes** after ES is healthy. An empty reply or browser spinner on `http://localhost:5601` is normal during startup — try `http://127.0.0.1:5601` or refresh. The welcome / “Explore on my own” screen means Kibana is up.

Stop when finished: `docker\stop-lab04-es.bat` or `docker compose down`.

Full details: [docker/README.md](docker/README.md).

> Connect runs on the **Windows host**; use `http://localhost:9200` in `es-orders-sink.json` (not the Docker service name `elasticsearch`).

---

## Step 1 — Install Elasticsearch Connect plugin

Lab 02 installs **JDBC only**. Lab 04 needs a **second** Hub plugin folder — JDBC does not include Elasticsearch.

### Full `plugins` layout (JDBC + ES)

```
C:\kafka-bin\kafka_2.13-4.2.0\plugins\
  confluent-jdbc\
    manifest.json
    lib\
      kafka-connect-jdbc-10.9.3.jar
      mysql-connector-j-9.7.0.jar
  confluent-elasticsearch\
    manifest.json
    lib\
      kafka-connect-elasticsearch-*.jar
      (all dependency jars from the Hub ZIP)
```

1. Download [Kafka Connect Elasticsearch](https://www.confluent.io/hub/confluentinc/kafka-connect-elasticsearch) from Confluent Hub (ZIP).
2. Extract the **entire** package into `plugins\confluent-elasticsearch\` (must contain `manifest.json` + `lib\`, not only a single JAR at the root).
3. Ensure `connect-standalone.properties` has:

```properties
plugin.path=C:/kafka-bin/kafka_2.13-4.2.0/plugins
```

Use **forward slashes** — see [Lab 02 troubleshooting](../lab-02-postgresql-jdbc-source/README.md).

4. **Restart** Connect:

```bat
cd /d C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\connect-standalone.bat config\connect-standalone.properties
```

5. Verify plugin:

```powershell
Invoke-RestMethod -Uri http://localhost:8083/connector-plugins |
  Where-Object { $_.class -like '*elasticsearch*' }
```

Expected: `io.confluent.connect.elasticsearch.ElasticsearchSinkConnector`

If deploy still returns **400** and the error **Available connector plugins** lists only built-ins plus JDBC, the ES ZIP is missing or not under `plugin.path` — fix layout in Step 1 and restart Connect (closing the worker window is not enough if a stale process holds port 8083).

---

## Step 2 — Seed Kafka topic `orders-topic`

**Create topic** (adjust bootstrap if using 3-broker cluster):

```bat
cd /d C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-topics.bat --create --topic orders-topic ^
  --bootstrap-server localhost:9092,localhost:9094,localhost:9095 ^
  --partitions 3 --replication-factor 1
```

**Option A — console producer:**

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic orders-topic
```

Paste JSON lines:

```json
{"orderId":"o-1","customer":"Alice","total":5000}
{"orderId":"o-2","customer":"Bob","total":1200}
```

**Option B — Python** ([python-connect-lab](../../python-connect-lab/)):

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs\python-connect-lab
pip install -r requirements.txt
python produce_orders.py localhost:9092 orders-topic 10
```

---

## Step 3 — Deploy sink connector (`es-orders-sink.json`)

Config: [configs/es-orders-sink.json](../configs/es-orders-sink.json)

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

**Deploy (PowerShell from `labs`):**

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs

curl.exe -X DELETE http://localhost:8083/connectors/es-orders-sink

.\scripts\deploy-connector.bat .\configs\es-orders-sink.json http://localhost:8083
.\scripts\connect-status.bat es-orders-sink http://localhost:8083
```

**Browser:**

- `http://localhost:8083/connectors/es-orders-sink/status` → `"state": "RUNNING"`

---

## Step 4 — Verify indexing in Elasticsearch

```powershell
curl.exe "http://localhost:9200/orders-topic/_search?pretty"
```

**Expected:** `"hits"` with your order documents.

List indices:

```powershell
curl.exe "http://localhost:9200/_cat/indices?v"
```

---

## Step 5 — Kibana visualization

1. Open http://localhost:5601
2. **Stack Management** → **Data views** (or Index Patterns in older UI)
3. Create data view: `orders-topic*`
4. **Discover** → select the data view → see documents
5. Optional dashboard: count orders, average `total` field

---

## End-to-end pipeline (slide 22)

Optional chain:

**MySQL → JDBC Source (`mysql-orders`) → (replicate/map topic) → ES Sink → Elasticsearch**

For a quick Lab 04-only path, use `orders-topic` + `produce_orders.py` as in Step 2.

---

## Checkpoint

- [ ] Elasticsearch + Kibana running (Docker)
- [ ] `ElasticsearchSinkConnector` in `/connector-plugins`
- [ ] Connector **RUNNING**
- [ ] `_search` returns documents
- [ ] Kibana data view `orders-topic*` shows data

---

## Troubleshooting

| Issue | Fix |
| ----- | --- |
| **400** `Failed to find any class ... ElasticsearchSinkConnector` and **Available** lists only `JdbcSource`, `JdbcSink`, `FileStream`, `Mirror` | **ES plugin not installed.** Add `plugins\confluent-elasticsearch\` from Confluent Hub (see Step 1). Restart Connect. Re-check `/connector-plugins`. |
| `ElasticsearchSinkConnector` not found (400) | Same as above — separate folder from `confluent-jdbc`; `plugin.path=C:/.../plugins` with `/`; restart Connect |
| **404** on `/connectors/es-orders-sink/status` | Connector not deployed yet — run Step 3 deploy first |
| `Connection refused` to `:9200` | Start Docker ES: `docker compose up -d` in `lab-04-elasticsearch-sink\docker`; Docker Desktop running |
| Kibana won’t load / `dependency failed to start: elasticsearch is unhealthy` | Wait for ES healthy (`docker compose ps`); `docker compose restart`; give Kibana 2–5 min |
| Connector FAILED, ES unreachable | Connect on host must use `http://localhost:9200`, not `http://elasticsearch:9200` |
| `mapper_parsing_exception` | Keep `schema.ignore=true` for schemaless JSON |
| Index not created | Check Connect log; confirm ES up: `curl.exe http://localhost:9200` |
| No documents in ES | Produce to `orders-topic` first; check connector status; topic name must match config |
| HTTP 409 on deploy | `curl.exe -X DELETE http://localhost:8083/connectors/es-orders-sink` then redeploy |
| Docker ES OOM / exits | Docker Desktop → 4 GB+ RAM; `docker compose logs elasticsearch` |
| Kibana Discover empty | Create data view `orders-topic*`; produce messages **after** sink is RUNNING |
| `deploy-connector.bat` not found (PowerShell) | Use `.\scripts\deploy-connector.bat` from `labs` folder |
| `deploy-connector.bat` `: was unexpected at this time` | Run from `labs` with `.\scripts\...`; script echoes use escaped `^(` `^)` for cmd |

### Useful commands

```powershell
Invoke-RestMethod http://localhost:8083/connectors
Invoke-RestMethod http://localhost:8083/connectors/es-orders-sink/status
curl.exe "http://localhost:9200/orders-topic/_search?pretty"
```

---

## Related

- [Docker setup](docker/README.md)
- [Connector configs](../configs/es-orders-sink.json)
- [Lab 02 — Connect + plugins](../lab-02-postgresql-jdbc-source/README.md)
- [Lab 03 — JDBC sink design](../lab-03-jdbc-sink-pipeline-design/README.md)
