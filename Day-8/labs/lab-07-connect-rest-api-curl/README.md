# Lab 07 — Drive Connect with curl

**Objective:** List, deploy, inspect, restart, and delete connectors using the Kafka Connect REST API.

From **Kafka_Connect_API.pptx** — Slide 38.

**Tested with:** Kafka Connect standalone on `http://localhost:8083`, Windows PowerShell.

---

## Implementation

| Track | Tool |
| ----- | ---- |
| **BAT / curl** | `deploy-connector.bat`, `connect-status.bat`, `curl.exe` |
| **Python** | [python-connect-lab](../../python-connect-lab/) — `deploy_connector.py`, `connect_status.py` |
| **Browser** | `http://localhost:8083/connectors` |

---

## Prerequisites

- Connect REST API on `http://localhost:8083` ([Lab 02](../lab-02-postgresql-jdbc-source/README.md))
- Sample connector JSON: [configs/mysql-orders-source.json](../configs/mysql-orders-source.json)

---

## REST API base URL

```
http://localhost:8083
```

---

## Step 1 — List connectors

**PowerShell:**

```powershell
Invoke-RestMethod -Uri http://localhost:8083/connectors
```

**curl:**

```powershell
curl.exe -s http://localhost:8083/connectors
```

**Expected:** `[]` or `["mysql-orders-source", "es-orders-sink", ...]`

**Browser:** http://localhost:8083/connectors

---

## Step 2 — Deploy connector

**PowerShell (from `labs`):**

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs

curl.exe -X DELETE http://localhost:8083/connectors/mysql-orders-source

.\scripts\deploy-connector.bat .\configs\mysql-orders-source.json http://localhost:8083
```

**Python:**

```powershell
cd python-connect-lab
python deploy_connector.py ..\configs\mysql-orders-source.json
```

**Expected:** HTTP **201** with connector name and config.  
**409** = already exists → DELETE first.

---

## Step 3 — Inspect status

```powershell
Invoke-RestMethod http://localhost:8083/connectors/mysql-orders-source/status
```

Or:

```powershell
.\scripts\connect-status.bat mysql-orders-source http://localhost:8083
```

**Browser:** http://localhost:8083/connectors/mysql-orders-source/status

Look for:

```json
{
  "name": "mysql-orders-source",
  "connector": { "state": "RUNNING" },
  "tasks": [{ "id": 0, "state": "RUNNING" }]
}
```

**Partial failure:** connector `RUNNING` but task `FAILED` → restart task (Step 5).

---

## Step 4 — Get / update config

**GET:**

```powershell
Invoke-RestMethod http://localhost:8083/connectors/mysql-orders-source/config
```

**PUT** (example — change poll interval): export full config JSON with updated `poll.interval.ms`, then:

```powershell
curl.exe -X PUT -H "Content-Type: application/json" `
  --data "@configs\mysql-orders-source.json" `
  http://localhost:8083/connectors/mysql-orders-source/config
```

> PUT requires the **flat** config object (inner `config` fields only), not the wrapper with `"name"`.

---

## Step 5 — Restart failed task

```powershell
curl.exe -X POST http://localhost:8083/connectors/mysql-orders-source/tasks/0/restart
```

Restart entire connector:

```powershell
curl.exe -X POST http://localhost:8083/connectors/mysql-orders-source/restart
```

---

## Step 6 — Pause and resume

```powershell
curl.exe -X PUT http://localhost:8083/connectors/mysql-orders-source/pause
curl.exe -X PUT http://localhost:8083/connectors/mysql-orders-source/resume
```

---

## Step 7 — Delete connector

```powershell
curl.exe -X DELETE http://localhost:8083/connectors/mysql-orders-source
```

---

## Other useful endpoints

| Method | Path | Purpose |
| ------ | ---- | ------- |
| GET | `/connector-plugins` | List installed connector types |
| GET | `/connectors/{name}/tasks` | List tasks |
| GET | `/connectors/{name}/topics` | Topics used by connector |

```powershell
Invoke-RestMethod http://localhost:8083/connector-plugins
```

---

## Discussion — CI/CD safety (slide 38)

| Operation | CI-safe? | Notes |
| --------- | -------- | ----- |
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

## Troubleshooting

| Issue | Fix |
| ----- | --- |
| Connection refused :8083 | Start `connect-standalone.bat` |
| 400 on POST | Missing plugin — JDBC: [Lab 02](../lab-02-postgresql-jdbc-source/README.md); ES: [Lab 04 Step 1](../lab-04-elasticsearch-sink/README.md#step-1--install-elasticsearch-connect-plugin) |
| 409 on POST | DELETE connector, redeploy |
| 500 on POST | Check `/status` trace; MySQL driver, credentials |

---

## Related

- [configs/README.md](../configs/README.md) — all connector JSON files
- [scripts/README.md](../scripts/README.md) — BAT helpers
