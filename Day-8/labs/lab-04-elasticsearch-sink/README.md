# Lab 04 — Push Kafka Data to Elasticsearch

**Objective:** Install the Elasticsearch sink connector, index records from a Kafka topic, and visualize in Kibana.

From **Kafka_Connect_API.pptx** — Slides 23–24.

**Tested with:** Java 17, Kafka 4.2, Elasticsearch 8.15 (Docker), Kafka Connect **standalone** on Windows, **kafka-python 2.3.1**.

---

## Suggested flow (Windows)

Do steps in this order — skipping plugin install or Docker causes the errors listed in [Troubleshooting](#troubleshooting).

1. Start **Kafka cluster** (`start-kafka-cluster.bat`) and **Connect** (`connect-standalone.bat`).
2. Start **Docker** Desktop → [Step 0](#step-0--start-elasticsearch--kibana-docker) (ES + Kibana).
3. Install **Elasticsearch Connect plugin** → [Step 1](#step-1--install-elasticsearch-connect-plugin) → verify `/connector-plugins`.
4. Confirm topic `orders-topic` exists → [Step 2](#step-2--seed-kafka-topic-orders-topic) (list/create).
5. **Deploy** `es-orders-sink` → [Step 3](#step-3--deploy-sink-connector-es-orders-sinkjson) → status **RUNNING**.
6. **Produce** JSON to `orders-topic` → [Step 2](#step-2--seed-kafka-topic-orders-topic) (after sink is up, or before — both work; re-run produce if ES was empty).
7. **Verify** Elasticsearch `_search` → [Step 4](#step-4--verify-indexing-in-elasticsearch).
8. **Kibana** data view → [Step 5](#step-5--kibana-visualization).

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

### Verify Kafka cluster and topic (PowerShell)

Run **`kafka-topics.bat` from the Kafka install directory**, not from `labs`:

```powershell
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092,localhost:9094,localhost:9095 --list
```

**Expected topics** (among others): `__consumer_offsets`, `mysql-orders`, `orders-topic`.

You may see a Log4j line like `main ERROR Reconfiguration failed...` — **ignore it** if exit code is **0** and topic names print.

| Port | Role |
| ---- | ---- |
| **9092**, **9094**, **9095** | Broker clients (`kafka-topics`, console tools, Connect) |
| **9093** | KRaft **controller only** — do **not** use in producers or Python |

**PowerShell note:** `cd /d C:\kafka-bin\...` is **cmd** syntax and fails in PowerShell. Use `cd C:\kafka-bin\kafka_2.13-4.2.0` only.

### Create topic (if missing)

```bat
cd /d C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-topics.bat --create --topic orders-topic ^
  --bootstrap-server localhost:9092,localhost:9094,localhost:9095 ^
  --partitions 3 --replication-factor 1
```

If the topic already exists with replication factor 3, `--create` is not needed.

**Option A — console producer** (from Kafka home):

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
python produce_orders.py localhost:9092 orders-topic 5
```

**Argument order** (all three required if you pass any):

```text
python produce_orders.py <bootstrap> <topic> <count>
```

| Command | Result |
| ------- | ------ |
| `python produce_orders.py localhost:9092 orders-topic 5` | Correct |
| `python produce_orders.py orders-topic 5` | **Wrong** — treats `orders-topic` as bootstrap → timeout |
| `python produce_orders.py localhost:9092,localhost:9094,localhost:9095 orders-topic 5` | **Often fails** on Windows — `KafkaTimeoutError` after 60s |

### kafka-python + Kafka 4.2 (observed)

| Symptom | Cause | Fix |
| ------- | ----- | --- |
| `NoBrokersAvailable` | kafka-python cannot auto-detect Kafka **4.2** API | Use repo `produce_orders.py` + `config.py` (`api_version=(2, 8, 0)`) |
| `KafkaTimeoutError` (60s) | Multi-broker bootstrap string in Python | Use **`localhost:9092` only** (one broker is enough for metadata) |
| Slow first run (30–90s) | Normal with pinned `api_version` | Wait; success prints `Produced N records to orders-topic` |

`kafka-topics --list` with three brokers can work while Python fails with the same three hosts — that is expected; use a **single** bootstrap for Python.

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
    "tasks.max": "2",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false"
  }
}
```

`produce_orders.py` sends **string keys** (`o-0`, `o-1`, …). Do **not** use `JsonConverter` on keys — tasks fail with `Unrecognized token 'o'` and nothing new reaches Kibana.

**Deploy (PowerShell from `labs`):**

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs

curl.exe -X DELETE http://localhost:8083/connectors/es-orders-sink

.\scripts\deploy-connector.bat .\configs\es-orders-sink.json http://localhost:8083
.\scripts\connect-status.bat es-orders-sink http://localhost:8083
```

**Success:** POST returns JSON with `"name":"es-orders-sink"` and `"tasks":[...]` (not an `error_code`).

**Before ES plugin:** **400** with message `Failed to find any class ... ElasticsearchSinkConnector` and **Available** plugins listing only JDBC + built-ins (`FileStream`, `Mirror`, etc.).

**After ES plugin + Connect restart:** same deploy returns connector config and task list; status **RUNNING**.

**404** on `/connectors/es-orders-sink/status` means the connector was **never deployed** (or wrong name) — run POST deploy first.

**409** means connector already exists — `DELETE` then redeploy.

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

### Connect & deploy

| Issue | Fix |
| ----- | --- |
| **400** `Failed to find any class ... ElasticsearchSinkConnector` and **Available** lists only `JdbcSource`, `JdbcSink`, `FileStream`, `Mirror` | **ES plugin not installed.** Add `plugins\confluent-elasticsearch\` from Confluent Hub (see Step 1). Restart Connect. Re-check `/connector-plugins`. |
| `ElasticsearchSinkConnector` not found (400) | Same as above — separate folder from `confluent-jdbc`; `plugin.path=C:/.../plugins` with `/`; restart Connect |
| **404** on `/connectors/es-orders-sink/status` | Connector not deployed yet — run Step 3 deploy first |
| HTTP **409** on deploy | `curl.exe -X DELETE http://localhost:8083/connectors/es-orders-sink` then redeploy |
| `deploy-connector.bat` not found (PowerShell) | Use `.\scripts\deploy-connector.bat` from `labs` folder |
| `deploy-connector.bat` `: was unexpected at this time` | Run from `labs` with `.\scripts\...` (not from another directory without path) |
| Connector FAILED, ES unreachable | Connect on host must use `http://localhost:9200`, not `http://elasticsearch:9200` |

### Docker, Elasticsearch, Kibana

| Issue | Fix |
| ----- | --- |
| `dockerDesktopLinuxEngine` / pipe not found | Start **Docker Desktop**; wait until engine is **Running**; retry `docker compose up -d` |
| `unable to get image` / cannot connect to Docker API | Same — Docker was not running when compose started |
| `elasticsearch is unhealthy` / Kibana **Error dependency** | Wait 1–3 min; `docker compose ps`; `docker compose restart elasticsearch`; then `docker compose up -d` |
| `Connection refused` to `:9200` | Start Docker ES in `lab-04-elasticsearch-sink\docker` |
| Kibana blank / curl empty reply / slow browser | Wait **2–5 min** after ES healthy; try http://127.0.0.1:5601 |
| Kibana welcome / “Explore on my own” | Kibana is up — create data view `orders-topic*` in Step 5 |
| Kibana Discover empty | Create data view; produce to `orders-topic`; confirm sink **RUNNING** and `_search` has hits |
| Docker ES OOM / exits | Docker Desktop → 4 GB+ RAM; `docker compose logs elasticsearch` |

### Kafka & Python producers

| Issue | Fix |
| ----- | --- |
| `kafka-topics.bat` not found from `labs` | `cd C:\kafka-bin\kafka_2.13-4.2.0` first |
| `cd /d` fails in PowerShell | Use `cd C:\kafka-bin\kafka_2.13-4.2.0` (no `/d`) |
| Log4j `Reconfiguration failed` on `--list` | Harmless if topics print and exit code 0 |
| `NoBrokersAvailable` (Python) | Cluster down, or missing `api_version` in `produce_orders.py` / `config.py` |
| `KafkaTimeoutError` 60s (Python) | Use `localhost:9092` only; not `9092,9094,9095`; not `9093` |
| Wrong args `produce_orders.py orders-topic 5` | Use `produce_orders.py localhost:9092 orders-topic 5` |
| `mapper_parsing_exception` | Keep `schema.ignore=true` for schemaless JSON |
| No documents in ES / empty Kibana | Check task status — if **FAILED** with `Unrecognized token 'o'`, set `key.converter` to `StringConverter` (see config above), redeploy, re-produce |
| Tasks FAILED `JsonParseException` token `o` | String keys + `JsonConverter` on key — use `StringConverter` for `key.converter` |
| No documents in ES | Produce to `orders-topic`; connector tasks **RUNNING** (not just connector RUNNING); topic name matches config |
| Kibana empty but `_search` has hits | Create data view `orders-topic*`; widen time range; refresh Discover |

### Useful commands

```powershell
# Kafka — from C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092,localhost:9094,localhost:9095 --list

# Connect
Invoke-RestMethod http://localhost:8083/connector-plugins |
  Where-Object { $_.class -like '*elasticsearch*' }
Invoke-RestMethod http://localhost:8083/connectors/es-orders-sink/status

# Elasticsearch
curl.exe "http://localhost:9200/orders-topic/_search?pretty"

# Produce (from python-connect-lab)
python produce_orders.py localhost:9092 orders-topic 5
```

---

## Observations from Windows lab runs

Summary of behavior seen while completing this lab on a **3-broker KRaft** cluster (`9092` / `9094` / `9095`) with Connect on **8083**:

| Area | Observation |
| ---- | ------------- |
| **Docker** | First `docker compose up` failed if Docker Desktop was off (`dockerDesktopLinuxEngine`). After starting Docker, images pulled (~several minutes). ES may show `unhealthy` briefly; Kibana waits on ES health. |
| **Connect plugin** | Deploying `es-orders-sink` before installing `confluent-elasticsearch` always returned **400** with JDBC-only available plugins. After Hub install + Connect restart, deploy returned full connector JSON. |
| **Kafka CLI** | `kafka-topics --list` with three bootstrap servers succeeded; topics included `orders-topic`. |
| **kafka-python** | Same machine: `localhost:9092` produced records; `localhost:9092,localhost:9094,localhost:9095` often hit **60s metadata timeout**. Pinning `api_version` for Kafka 4.2 avoided `NoBrokersAvailable`. |
| **Ports** | `Test-NetConnection` showed 9092, 9094, 9095 open; **9093** is controller-only and must not be used in client bootstrap strings. |
| **Kibana** | Welcome screen after several minutes is normal; indexing visibility requires data view `orders-topic*` and messages on the topic. |

---

## Related

- [Docker setup](docker/README.md)
- [Connector configs](../configs/es-orders-sink.json)
- [Lab 02 — Connect + plugins](../lab-02-postgresql-jdbc-source/README.md)
- [Lab 03 — JDBC sink design](../lab-03-jdbc-sink-pipeline-design/README.md)
