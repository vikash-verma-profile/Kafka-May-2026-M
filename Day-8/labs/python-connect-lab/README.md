# Day 8 Python Connect Lab

Python helpers for Kafka Connect REST API and topic verification.

Uses connector JSON from [../configs](../configs/) (MySQL JDBC source).

## Setup

```powershell
pip install -r requirements.txt
```

## Scripts

| Script | Lab |
| ------ | --- |
| `deploy_connector.py` | 02, 04, 07 — POST connector config |
| `connect_status.py` | 06, 07 — GET status |
| `produce_orders.py` | 04 — seed orders-topic |
| `verify_topic.py` | 06 — consume `mysql-orders` |

## Examples (from `labs` folder)

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs

# Delete first if you get HTTP 409
curl.exe -X DELETE http://localhost:8083/connectors/mysql-orders-source

python deploy_connector.py configs\mysql-orders-source.json
python connect_status.py http://localhost:8083 mysql-orders-source
python verify_topic.py localhost:9092 mysql-orders 10
```

Or use BAT scripts:

```powershell
.\scripts\deploy-connector.bat .\configs\mysql-orders-source.json http://localhost:8083
.\scripts\connect-status.bat mysql-orders-source http://localhost:8083
```

## Prerequisites

- Connect REST on `http://localhost:8083`
- MySQL driver in Connect `plugin.path` (see [Lab 02 README](../lab-02-postgresql-jdbc-source/README.md))
