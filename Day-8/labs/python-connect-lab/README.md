# Day 8 Python Connect Lab

Python helpers for Kafka Connect REST API and topic verification.

Uses connector JSON from [../configs](../configs/).

## Setup

```powershell
pip install -r requirements.txt
```

## Scripts

| Script | Lab |
|--------|-----|
| `deploy_connector.py` | 02, 04, 07 — POST connector config |
| `connect_status.py` | 06, 07 — GET status |
| `produce_orders.py` | 04 — seed orders-topic |
| `verify_topic.py` | 06 — consume postgres-orders |

## Examples

```powershell
python deploy_connector.py ..\configs\postgres-orders-source.json
python connect_status.py http://localhost:8083 postgres-orders-source
python verify_topic.py localhost:9092 postgres-orders 10
```
