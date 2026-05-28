# Day 8 Python Connect Lab

Python helpers for Kafka Connect REST API and Kafka topic verification.

Configs: [../configs](../configs/) · Full setup: [Lab 02](../lab-02-postgresql-jdbc-source/README.md)

## Setup

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs\python-connect-lab
pip install -r requirements.txt
```

## Scripts

| Script | Labs | Purpose |
| ------ | ---- | ------- |
| `deploy_connector.py` | 02, 03, 04, 05, 07, 08 | POST connector JSON |
| `connect_status.py` | 06, 07 | GET connector list or status |
| `produce_orders.py` | 03, 04 | Produce JSON to Kafka topic |
| `verify_topic.py` | 06 | Consume from topic |

## Examples

From `python-connect-lab` folder:

```powershell
# Lab 02 — deploy MySQL source
python deploy_connector.py ..\configs\mysql-orders-source.json
python connect_status.py http://localhost:8083 mysql-orders-source

# Lab 03 — produce to topic "orders"
python produce_orders.py localhost:9092 orders 5

# Lab 04 — produce to orders-topic (defaults work if Kafka cluster is up)
python produce_orders.py
python produce_orders.py localhost:9092 orders-topic 10

# Lab 06 — verify mysql-orders
python verify_topic.py localhost:9092 mysql-orders 20
```

From `labs` folder (BAT alternative):

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs
.\scripts\deploy-connector.bat .\configs\mysql-orders-source.json http://localhost:8083
```

## Defaults (`config.py`)

| Setting | Value |
| ------- | ----- |
| Connect URL | `http://localhost:8083` |
| Bootstrap | `localhost:9092` (one broker is enough for discovery) |

Override via command-line arguments on each script.

## Troubleshooting

| Error | Fix |
| ----- | --- |
| `NoBrokersAvailable` | Start Kafka (`start-kafka-cluster.bat`). Scripts pin `api_version` for Kafka 4.x — pull latest `produce_orders.py` / `config.py`. |
| `KafkaTimeoutError` after 60s | Use **one** bootstrap: `localhost:9092` (not a comma list — kafka-python often times out with multi-bootstrap on Windows). Never use **9093** (controller). |
| `kafka-topics` works but Python fails | Same as above; or cluster was down on first try — retry after brokers show **Kafka Server started**. |

## Prerequisites

- Connect REST running ([Lab 02](../lab-02-postgresql-jdbc-source/README.md))
- Kafka cluster running on **9092 / 9094 / 9095**
- For JDBC labs: MySQL + plugins configured
