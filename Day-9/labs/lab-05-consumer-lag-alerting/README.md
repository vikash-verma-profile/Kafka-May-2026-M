# Lab 05 — Consumer Lag Alerting

**Objective:** Measure how far behind a consumer group is (“lag”), expose that as a Prometheus metric, and fire an alert when lag stays high.

**Source:** Kafka_Security_Monitoring.pptx — Slide 21  
**Time:** ~20 minutes  
**Requires:** [Lab 04](../lab-04-jmx-prometheus/README.md) (Prometheus running)

---

## What you are learning

- **Consumer lag** — difference between the latest message on a partition and what the consumer has committed.
- **Why seconds, not offsets?** Raw offset lag is hard to compare across topics; **lag in seconds** matches user-facing SLAs (slide 20).
- **Alert rule** — Prometheus expression + `for:` duration before paging.

---

## Implementation tracks

| Track | Tool |
|-------|------|
| **Prometheus + lag exporter** | [monitoring/alert-rules.yml](../monitoring/alert-rules.yml) + [kafka-lag-exporter](https://github.com/seglo/kafka-lag-exporter) |
| **Java** | `Lab05LagCheck` — [java-security-lab](../java-security-lab/README.md) |
| **Python** | `python lab05_lag_check.py` — [python-security-lab](../python-security-lab/README.md) |

---

## Docker for this lab (optional)

| Component | Required? | How |
|-----------|-----------|-----|
| Kafka broker | **No Docker** | Host `%KAFKA_HOME%` |
| kafka-lag-exporter | **Optional** | [docker compose](../docker/README.md) or `docker run` below |
| Prometheus | **Optional** | [docker compose](../docker/README.md) or native install |

See full map: **[docker/README.md](../docker/README.md)**.

---

## Before you start — checklist

- [ ] Prometheus from Lab 04 is running (`http://localhost:9090`)
- [ ] Topic `orders` exists with some messages (from earlier labs)
- [ ] You can run producer and consumer in **separate** terminals

---

## Step 0 — Understand the demo scenario

You will:

1. Run a **consumer** in group `billing-svc`.
2. **Stop** the consumer but keep **producing** messages.
3. Lag grows → alert **fires**.
4. **Restart** consumer → lag drops → alert **clears**.

---

## Step 1 — Deploy kafka-lag-exporter

### Option A — Docker (easiest for first time)

**Prerequisite:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) running.

**All monitoring services at once (Lab 04–06):**

```bat
cd c:\Users\om\Desktop\KafKa\Day-9\labs\docker
docker compose up -d
```

**Only lag exporter:**

```bat
docker run -d --name kafka-lag-exporter -p 9999:9999 ^
  -e KAFKA_LAG_EXPORTER_CLUSTERS_0_NAME=local ^
  -e KAFKA_LAG_EXPORTER_CLUSTERS_0_BOOTSTRAP_BROKERS=host.docker.internal:9092 ^
  seglo/kafka-lag-exporter:latest
```

> On Windows, use `host.docker.internal:9092` (not `localhost:9092`) so the container reaches Kafka on your PC.

### Option B — JAR with `application.conf`

Create `application.conf`:

```hocon
kafka-clusters = [
  {
    name = "local"
    bootstrap-brokers = ["localhost:9092"]
  }
]
```

Run the exporter JAR from the project’s releases page; it should listen on port **9999**.

**Verify:**

```bat
curl http://localhost:9999/metrics
```

Search for `kafka_consumergroup` or `lag` in the output.

**What success looks like:** HTTP 200 and metrics mentioning consumer groups.

---

## Step 2 — Add lag exporter to Prometheus

Edit [monitoring/prometheus.yml](../monitoring/prometheus.yml) — ensure this job exists:

```yaml
  - job_name: kafka-lag-exporter
    static_configs:
      - targets: ['localhost:9999']
```

Reload or restart Prometheus. Check **Status → Targets** — `kafka-lag-exporter` should be **UP**.

---

## Step 3 — Add alert rule

Lab file: [monitoring/alert-rules.yml](../monitoring/alert-rules.yml)

Example rule:

```yaml
groups:
  - name: kafka_consumer_lag
    rules:
      - alert: ConsumerLagHigh
        expr: kafka_consumergroup_group_max_lag_seconds > 30
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "Consumer group {{ $labels.group }} lag > 30s"
```

Ensure `prometheus.yml` includes:

```yaml
rule_files:
  - alert-rules.yml
```

Restart Prometheus. Open **Alerts** tab — rule should appear (may be inactive/green initially).

**First-time tip:** Metric name must match what *your* lag exporter version exposes — search `/metrics` on port 9999 and adjust `expr` if needed.

---

## Step 4 — Simulate lag (three terminals)

### Terminal A — continuous producer

```bat
cd /d %KAFKA_HOME%
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic orders
```

Leave it open; type messages occasionally or paste many lines. (PLAINTEXT is fine for lag demo; cluster from [my-config](../my-config/README.md).)

### Terminal B — consumer (then stop it)

Start consumer:

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 ^
  --topic orders --group billing-svc
```

Let it read a few messages, then press **Ctrl+C** to stop.

### Terminal C — watch lag (optional)

**Prometheus UI** → query:

```promql
kafka_consumergroup_group_max_lag_seconds{group="billing-svc"}
```

**Java track:**

```bat
cd c:\Users\om\Desktop\KafKa\Day-9\labs\java-security-lab
mvn -q exec:java -Dexec.mainClass=day9.labs.Lab05LagCheck -Dexec.args="localhost:9092 billing-svc"
```

**Python track:**

```bat
cd c:\Users\om\Desktop\KafKa\Day-9\labs\python-security-lab
python lab05_lag_check.py localhost:9092 billing-svc
```

**What success looks like:** Lag value increases while producer runs and consumer is stopped.

---

## Step 5 — Observe alert fire and resolve

1. Wait **~1–2 minutes** (`for: 1m` in the rule).
2. Prometheus **Alerts** → `ConsumerLagHigh` should show **Pending** then **Firing**.
3. Restart consumer in Terminal B (same group `billing-svc`).
4. Lag should fall; within a minute alert returns to **Inactive**.

| Phase | What you should see |
|-------|---------------------|
| Consumer running | Lag near 0 |
| Consumer stopped, producer active | Lag climbing |
| After `for: 1m` | Alert Firing |
| Consumer restarted | Alert clears |

---

## Checkpoint — you are done when

- [ ] Lag metric visible in Prometheus for `billing-svc`
- [ ] Alert fires when consumer is stopped
- [ ] Alert clears after consumer restarts
- [ ] You can explain why alerting on **seconds** is better than raw offsets

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| No lag metrics | Lag exporter not running or wrong bootstrap server |
| Alert never fires | Threshold too high; increase producer rate; check metric name in `expr` |
| Lag always 0 | Wrong consumer group name in query vs `--group` flag |
| Instant alert | Lower `for:` only after demo works — keep `1m` for learning |

---

## What’s next?

[Lab 06](../lab-06-grafana-dashboard/README.md) — build a visual cluster overview dashboard.
