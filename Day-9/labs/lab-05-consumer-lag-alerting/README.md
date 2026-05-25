# Lab 05 — Consumer Lag Alerting

**Objective:** Deploy lag monitoring, write a Prometheus alert rule, and fire/recover an alert by stopping a consumer.

From **Kafka_Security_Monitoring.pptx** — Slide 21. **Time:** ~20 min.

---

## Implementation

| Track | Tool |
|-------|------|
| **Prometheus** | [monitoring/alert-rules.yml](../monitoring/alert-rules.yml) + kafka-lag-exporter |
| **Python** | `python lab05_lag_check.py localhost:9092 billing-svc` in [python-security-lab](../../python-security-lab/) |

---

## Prerequisites

- Prometheus from [Lab 04](../lab-04-jmx-prometheus/README.md)
- [kafka-lag-exporter](https://github.com/seglo/kafka-lag-exporter) or Burrow (optional)

---

## Step 1 — Deploy kafka-lag-exporter

Configure `application.conf`:

```hocon
kafka-clusters = [
  {
    name = "local"
    bootstrap-brokers = ["localhost:9092"]
  }
]
```

Run exporter (Docker or JAR) exposing port **9999**.

---

## Step 2 — Prometheus scrape lag exporter

```yaml
- job_name: kafka-lag
  static_configs:
    - targets: ['localhost:9999']
```

---

## Step 3 — Alert rule (`kafka-lag-rules.yml`)

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

Load into Prometheus → **Rules** UI.

---

## Step 4 — Simulate lag

**Terminal 1** — continuous producer:

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic orders
```

**Terminal 2** — start consumer, then **stop** it (Ctrl+C):

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic orders --group billing-svc
```

Keep producing. Lag should rise.

---

## Step 5 — Observe alert

Within ~1–2 min, Alertmanager/Prometheus UI shows **ConsumerLagHigh** firing.

Restart consumer → lag drops → alert resolves.

---

## Checkpoint

- [ ] Lag metric visible in Prometheus
- [ ] Alert fires when consumer stopped
- [ ] Alert clears after consumer restart

---

## Best practice (slide 20)

Alert on **lag-in-seconds**, not raw offset — offsets are not comparable across loads.
