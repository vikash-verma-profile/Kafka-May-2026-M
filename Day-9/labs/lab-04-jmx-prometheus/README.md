# Lab 04 — JMX + Prometheus Exporter

**Objective:** Attach `jmx_prometheus_javaagent` to a broker and scrape metrics into Prometheus.

From **Kafka_Security_Monitoring.pptx** — Slide 19. **Time:** ~25 min.

---

## Prerequisites

- Running Kafka broker
- [jmx_exporter](https://github.com/prometheus/jmx_exporter) JAR
- Prometheus installed (or Docker)

---

## Step 1 — Download jmx_exporter

Get `jmx_prometheus_javaagent-*.jar` and `kafka-2_0_0.yml` rules file.

---

## Step 2 — Attach Java agent to broker

Before starting broker:

```bat
set KAFKA_OPTS=-javaagent:C:\tools\jmx_prometheus_javaagent.jar=7071:C:\tools\kafka.yml
bin\windows\kafka-server-start.bat config\broker-1.properties
```

`kafka.yml` — use upstream example mapping Kafka MBeans to Prometheus names.

---

## Step 3 — Verify metrics endpoint

```bat
curl http://localhost:7071/metrics
```

Search for:

```text
kafka_server_BrokerTopicMetrics_BytesInPerSec
```

---

## Step 4 — Prometheus scrape config

```yaml
scrape_configs:
  - job_name: kafka-broker
    static_configs:
      - targets: ['localhost:7071']
```

Reload Prometheus → **Targets** UI shows **UP**.

---

## Step 5 — Grafana dashboard

1. Add Prometheus datasource
2. Import dashboard **7589** (Confluent Kafka)
3. Confirm non-zero throughput while producing to a test topic

---

## Checkpoint

- [ ] `/metrics` returns Kafka broker metrics
- [ ] Prometheus target UP
- [ ] Grafana panel shows BytesIn/Out

---

## Why not scrape JMX directly? (slide 16)

JMX is verbose; `jmx_exporter` exposes Prometheus text format — standard for alerting.
