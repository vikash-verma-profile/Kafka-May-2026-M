# Day 9 — Python Security Lab

**Python track** for client exercises. The same labs are available in **Java**: [java-security-lab](../java-security-lab/README.md).

Shell steps in each lab README remain the source of truth for broker setup; Python scripts run **after** the broker is configured.

| Track | Folder |
|-------|--------|
| Shell (CLI) | [../scripts](../scripts/) + lab README |
| **Java** | [../java-security-lab](../java-security-lab/) |
| **Python** | this folder |

---

## What is in this folder?

| File | Used in | Purpose |
|------|---------|---------|
| `lab01_scram_producer.py` | Lab 01 | Send one message using SCRAM |
| `lab05_lag_check.py` | Lab 05 | Print consumer group lag from CLI |
| `config.py` | Both | Shared connection helpers |
| `requirements.txt` | Setup | Python dependencies |

ACL (Lab 02) and TLS (Lab 03) still use `kafka-acls.bat` and broker configs — see [../scripts](../scripts/) and [../configs](../configs/).

---

## Before you start — checklist

- [ ] Python 3.9+ installed: `python --version`
- [ ] Lab 01 broker steps done (SASL on port **9093**, user `alice` / password `secret`)
- [ ] `KAFKA_HOME` set if you use helper batch files from [../scripts](../scripts/)

---

## Step 1 — Open terminal in this folder

```powershell
cd c:\Users\om\Desktop\KafKa\Day-9\labs\python-security-lab
```

---

## Step 2 — Create a virtual environment (recommended first time)

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
```

**If activation is blocked:** Run once (PowerShell as your user):

```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```

**What success looks like:** Prompt shows `(.venv)`.

---

## Step 3 — Install dependencies

```powershell
pip install -r requirements.txt
```

**Expected:** Packages such as `kafka-python` install without errors.

---

## Step 4 — Create SCRAM user (if not done via shell)

From `labs\scripts`:

```bat
set KAFKA_HOME=C:\path\to\kafka
cd c:\Users\om\Desktop\KafKa\Day-9\labs\scripts
create-scram-user.bat localhost:9092 alice secret
```

Client config must match: [../configs/client-scram.properties](../configs/client-scram.properties).

---

## Lab 01 — SCRAM producer

### When to use

After broker listens on **SASL_PLAINTEXT** port **9093** (see [Lab 01 README](../lab-01-sasl-scram-authentication/README.md)).

### Command

```powershell
python lab01_scram_producer.py localhost:9093 orders alice secret
```

Arguments:

| # | Argument | Example | Meaning |
|---|----------|---------|---------|
| 1 | bootstrap | `localhost:9093` | Broker SASL port |
| 2 | topic | `orders` | Topic name |
| 3 | username | `alice` | SCRAM user |
| 4 | password | `secret` | SCRAM password |

### What success looks like

- Script prints confirmation (message sent / metadata)
- No `NoBrokersAvailable` or `Authentication failed`
- Message visible via shell consumer with same user (Lab 01 Step 4.3)

### Common errors

| Error | Fix |
|-------|-----|
| `Authentication failed` | Run `create-scram-user.bat`; check password |
| `NoBrokersAvailable` | Broker down or wrong port (use 9093 not 9092) |
| `UnknownTopicOrPartition` | Create topic first (Lab 01 Step 4.1) |

---

## Lab 05 — Consumer lag check

### When to use

After topic has messages and group `billing-svc` exists (from [Lab 05](../lab-05-consumer-lag-alerting/README.md)).

### Command

```powershell
python lab05_lag_check.py localhost:9092 billing-svc
```

Arguments: `bootstrap-server` `consumer-group-id`

### What success looks like

- Prints lag per partition or summary
- Lag near **0** when consumer is running
- Lag **increases** when consumer is stopped and producer keeps writing

### Compare with Prometheus

Same group name as in alert rules: `billing-svc` in [../monitoring/alert-rules.yml](../monitoring/alert-rules.yml).

---

## Step 5 — Deactivate virtual environment when done

```powershell
deactivate
```

---

## Relationship to shell and Java labs

```text
Broker setup (JAAS, listeners, ACLs, TLS)  →  Shell / README in lab-01..03
Produce / lag demo                          →  Java OR Python (this folder)
```

**Docker:** not required for Python scripts. Optional for Labs 04–06 monitoring — [docker/README.md](../docker/README.md).

Always complete broker configuration in the markdown lab first; then use Python to repeat the client-side action.

---

## Next steps

- Finished Lab 01 Python? Complete [Lab 02 ACLs](../lab-02-kafka-acls/README.md) with shell commands.
- Finished Lab 05 Python? Continue to [Lab 06 Grafana](../lab-06-grafana-dashboard/README.md).
