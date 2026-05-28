# Lab 04 — JMX + Prometheus Exporter

**Objective:** Expose Kafka broker metrics in a format Prometheus can scrape, then confirm metrics appear in Prometheus (and optionally Grafana).

**Source:** Kafka_Security_Monitoring.pptx — Slide 19  
**Time:** ~25 minutes  
**Requires:** A running Kafka broker (Labs 01–03 security setup optional for metrics lab)

---

## What you are learning

- **JMX** — Java Management Extensions; Kafka exposes rich stats internally.
- **jmx_exporter** — A small Java agent that translates JMX → **Prometheus text format** on an HTTP port (e.g. `7071`).
- **Prometheus** — Pulls `/metrics` periodically and stores time series.

**Why not scrape JMX directly?** JMX is hard to operate at scale; Prometheus format is standard for alerts and Grafana.

---

## Before you start — checklist

- [ ] Kafka broker can start normally
- [ ] Download folder ready, e.g. `C:\tools\`
- [ ] Prometheus: **native install** or **Docker** (see below)
- [ ] Browser for `http://localhost:7071/metrics` and `http://localhost:9090`

---

## Docker for this lab (optional)

| Step | Docker required? | Notes |
|------|------------------|-------|
| jmx_exporter on broker | **No** | Java agent on host JVM — always |
| Run Prometheus | **Optional** | `docker compose` or `prometheus.exe` |
| Run Grafana | **No** (Lab 06) | — |

**Start Prometheus (and lag-exporter for Lab 05) via compose:**

```bat
cd c:\Users\om\Desktop\KafKa\Day-9\labs\docker
docker compose up -d prometheus
```

Uses [monitoring/prometheus-docker.yml](../monitoring/prometheus-docker.yml) so Prometheus inside Docker scrapes `host.docker.internal:7071` (your broker metrics on the PC).

**Native Prometheus** on Windows: use [monitoring/prometheus.yml](../monitoring/prometheus.yml) with `localhost:7071`.

Full map: **[docker/README.md](../docker/README.md)**.

---

## Step 0 — Download jmx_exporter (one-time)

1. Open: [https://github.com/prometheus/jmx_exporter/releases](https://github.com/prometheus/jmx_exporter/releases)
2. Download **jmx_prometheus_javaagent-*.jar** (pick a recent version).
3. Download example rules: **kafka-2_0_0.yml** from the same repo (`example_configs/` folder).
4. Save both to `C:\tools\`:

   ```text
   C:\tools\jmx_prometheus_javaagent-0.20.0.jar
   C:\tools\kafka-2_0_0.yml
   ```

Repo copy of scrape ideas: [monitoring/kafka-jmx.yml](../monitoring/kafka-jmx.yml)

---

## Step 1 — Attach Java agent when starting the broker

**Important:** Stop any running broker first. You must set `KAFKA_OPTS` **before** `kafka-server-start`.

### 1.1 Combine JAAS (if from Lab 01) with jmx agent

Example — **only jmx** (no SASL):

```bat
set KAFKA_OPTS=-javaagent:C:\tools\jmx_prometheus_javaagent-0.20.0.jar=7071:C:\tools\kafka-2_0_0.yml
```

Example — **JAAS + jmx** (both needed if SASL enabled):

```bat
set KAFKA_OPTS=-Djava.security.auth.login.config=%KAFKA_HOME%\config\kafka_server_jaas.conf -javaagent:C:\tools\jmx_prometheus_javaagent-0.20.0.jar=7071:C:\tools\kafka-2_0_0.yml
```

### 1.2 Start broker

```bat
cd /d %KAFKA_HOME%
bin\windows\kafka-server-start.bat config\server.properties
```

**What success looks like:** Startup logs mention javaagent; no “agent failed to load” errors.

**Port meaning:** `7071` in the command is where HTTP metrics listen.

---

## Step 2 — Verify metrics endpoint (first win)

Open a browser or use curl:

```bat
curl http://localhost:7071/metrics
```

Or PowerShell:

```powershell
Invoke-WebRequest http://localhost:7071/metrics | Select-Object -ExpandProperty Content
```

Search the output for:

```text
kafka_server_BrokerTopicMetrics_BytesInPerSec
```

**What success looks like:** Thousands of lines starting with `# HELP` and metric names containing `kafka_server_`.

**If empty or connection refused:** Broker not running with agent — recheck `KAFKA_OPTS` and restart.

---

## Step 3 — Configure Prometheus to scrape the broker

### 3.1 Use lab config or edit your own

File: [monitoring/prometheus.yml](../monitoring/prometheus.yml)

```yaml
scrape_configs:
  - job_name: kafka-broker-jmx
    static_configs:
      - targets: ['localhost:7071']
```

### 3.2 Start Prometheus

**Option A — Docker (no local `prometheus.exe` install):**

```bat
cd c:\Users\om\Desktop\KafKa\Day-9\labs\docker
docker compose up -d prometheus
```

**Option B — Native Windows install:**

```bat
prometheus.exe --config.file=c:\Users\om\Desktop\KafKa\Day-9\labs\monitoring\prometheus.yml
```

### 3.3 Check target is UP

1. Open `http://localhost:9090`
2. **Status → Targets**
3. Find job `kafka-broker-jmx` — state should be **UP**

**First-time tip:** If **DOWN**, click the target for the error (often `connection refused` = wrong port or agent not attached).

---

## Step 4 — Run a query in Prometheus UI

1. Go to **Graph**.
2. Enter:

   ```promql
   kafka_server_BrokerTopicMetrics_BytesInPerSec
   ```

3. Click **Execute**.

### 4.1 Generate traffic (so graph is not flat)

In another terminal, produce messages:

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic orders
```

Type a few lines. Refresh the Prometheus graph — rates should move.

---

## Step 5 — (Optional) Quick Grafana preview

Full dashboard build is [Lab 06](../lab-06-grafana-dashboard/README.md). For a quick check now:

1. Install Grafana; open `http://localhost:3000`
2. **Connections → Data sources → Add Prometheus** → URL `http://localhost:9090`
3. **Dashboards → Import** → ID **7589** (Confluent Kafka overview)
4. Confirm panels show data while producing

---

## Checkpoint — you are done when

- [ ] `http://localhost:7071/metrics` returns Kafka metrics
- [ ] Prometheus target **UP** for port 7071
- [ ] PromQL query returns series (not “empty result”)
- [ ] You can explain why jmx_exporter sits *on* the broker JVM

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| Agent JAR not found | Use full path in `-javaagent:` |
| Metrics 404 | Wrong port in agent string `=7071:` |
| Prometheus DOWN | Windows firewall blocking 7071; broker not running |
| Metric names differ | Kafka version changed MBean names — update `kafka-2_0_0.yml` from jmx_exporter repo |
| `KAFKA_OPTS` lost after restart | Set in same terminal before every `kafka-server-start` |

---

## What’s next?

[Lab 05](../lab-05-consumer-lag-alerting/README.md) — alert when consumer groups fall behind.
