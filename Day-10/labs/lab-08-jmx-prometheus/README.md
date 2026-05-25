# Lab 08 — Wire Up JMX and Prometheus

**Objective:** Expose broker JMX via Prometheus JMX exporter and verify `MessagesInPerSec` in Grafana.

From **Kafka_cap.pptx** — Slide 31.

---

## Prerequisites

- `jmx_prometheus_javaagent.jar` + `kafka.yml` rules
- Prometheus server
- Grafana (optional)

> Same flow as [Day-9 Lab 04](../../Day-9/labs/lab-04-jmx-prometheus/README.md) — repeated here for the production track.

---

## Step 1 — Attach JMX exporter

```bat
set KAFKA_OPTS=-javaagent:C:\tools\jmx_prometheus_javaagent.jar=7071:C:\tools\kafka.yml
bin\windows\kafka-server-start.bat config\broker-1.properties
```

---

## Step 2 — Curl metrics

```bat
curl http://localhost:7071/metrics | findstr MessagesInPerSec
```

---

## Step 3 — Prometheus scrape job

```yaml
- job_name: kafka-broker-1
  static_configs:
    - targets: ['localhost:7071']
```

Reload Prometheus → target **UP**.

---

## Step 4 — PromQL in Grafana

```promql
rate(kafka_server_BrokerTopicMetrics_MessagesInPerSec[1m])
```

Produce to any topic — graph should go non-zero.

---

## Checkpoint

- [ ] Prometheus target UP
- [ ] MessagesInPerSec visible while producing
- [ ] UnderReplicatedPartitions metric available for alerting

---

## Production monitoring (slide 30)

Track throughput, latency, partition health, JVM heap/GC.
