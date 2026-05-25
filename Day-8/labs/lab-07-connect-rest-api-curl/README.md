# Lab 07 — Drive Connect with curl

**Objective:** List, deploy, inspect, restart, and delete connectors using the Kafka Connect REST API.

From **Kafka_Connect_API.pptx** — Slide 38.

---

## Implementation

| Track | Tool |
|-------|------|
| **curl** | Commands below |
| **Python** | `connect_status.py`, `deploy_connector.py` in [python-connect-lab](../../python-connect-lab/) |

---

## Prerequisites

- Connect REST API on `http://localhost:8083`
- Sample connector JSON file (e.g. from Lab 02)

---

## Step 1 — List connectors

```bash
curl -s http://localhost:8083/connectors | jq
```

**Expected:** `[]` or `["postgres-orders-source", ...]`

---

## Step 2 — Deploy connector

```bash
curl -X POST -H "Content-Type: application/json" \
  --data @postgres-orders-source.json \
  http://localhost:8083/connectors
```

**Expected:** HTTP 201 with connector name and config.

---

## Step 3 — Inspect status

```bash
curl -s http://localhost:8083/connectors/postgres-orders-source/status | jq
```

Look for:

```json
{
  "name": "postgres-orders-source",
  "connector": { "state": "RUNNING" },
  "tasks": [{ "id": 0, "state": "RUNNING" }]
}
```

**Partial failure:** connector `RUNNING` but task `FAILED` — restart individual task (Step 5).

---

## Step 4 — Get / update config

```bash
curl -s http://localhost:8083/connectors/postgres-orders-source/config | jq
```

Update poll interval:

```bash
curl -X PUT -H "Content-Type: application/json" \
  --data '{"connector.class":"...","poll.interval.ms":"2000", ...}' \
  http://localhost:8083/connectors/postgres-orders-source/config
```

---

## Step 5 — Restart failed task

```bash
curl -X POST http://localhost:8083/connectors/postgres-orders-source/tasks/0/restart
```

Restart entire connector:

```bash
curl -X POST http://localhost:8083/connectors/postgres-orders-source/restart
```

---

## Step 6 — Pause and resume

```bash
curl -X PUT http://localhost:8083/connectors/postgres-orders-source/pause
curl -X PUT http://localhost:8083/connectors/postgres-orders-source/resume
```

---

## Step 7 — Delete connector

```bash
curl -X DELETE http://localhost:8083/connectors/postgres-orders-source
```

---

## Discussion — CI/CD safety (slide 38)

| Operation | CI-safe? | Notes |
|-----------|----------|-------|
| GET list/status | Yes | Read-only health checks |
| POST new connector | Gate | Review JSON in PR; deploy via pipeline |
| PUT config | Gate | Can cause rebalance; needs approval |
| DELETE | Restricted | Production kill switch |
| Task restart | Usually yes | Automated remediation |

**Detect partial failure:** Poll `/status`; alert if any `tasks[].state != "RUNNING"`.

---

## Checkpoint

- [ ] Deployed connector via POST
- [ ] Status shows RUNNING
- [ ] Restarted task 0 successfully
- [ ] Deleted connector (optional cleanup)

---

## PowerShell equivalent

```powershell
Invoke-RestMethod -Uri http://localhost:8083/connectors
```
