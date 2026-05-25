# Lab 06 — Grafana Dashboard Build

**Objective:** Build a cluster overview dashboard with health stats, throughput, lag heatmap, and Alertmanager annotations.

From **Kafka_Security_Monitoring.pptx** — Slide 24. **Time:** ~30 min.

---

## Prerequisites

- Prometheus + Grafana from Labs 04–05
- Multi-broker cluster recommended (optional)

---

## Step 1 — Stat panels (top row)

| Panel | PromQL (example) |
|-------|------------------|
| Broker count | `count(kafka_server_replicamanager_leadercount)` |
| Under-replicated partitions | `sum(kafka_server_replicamanager_underreplicatedpartitions)` |
| Offline partitions | `sum(kafka_controller_kafkacontroller_offlinepartitionscount)` |
| Active controller | `sum(kafka_controller_kafkacontroller_activecontrollercount)` |

**Expected:** URP = 0, offline = 0, controller = 1.

---

## Step 2 — Throughput time series

```promql
topk(10, sum by (topic) (rate(kafka_server_BrokerTopicMetrics_BytesInPerSec[1m])))
```

Duplicate panel for `BytesOutPerSec`.

---

## Step 3 — Consumer lag heatmap

```promql
kafka_consumergroup_group_lag_seconds
```

Visualization: **Heatmap** — axes: consumer group × partition.

---

## Step 4 — Alertmanager annotations

1. Configure Grafana **Alertmanager** datasource
2. Dashboard settings → **Annotations** → add Alertmanager source
3. Incidents overlay on throughput charts

---

## Step 5 — Export dashboards-as-code

**Dashboard settings → JSON Model → Save to file**

Commit to `Day-9/labs/dashboards/kafka-cluster-overview.json`.

---

## Checkpoint

- [ ] Four stat panels at top
- [ ] Bytes in/out by topic
- [ ] Lag heatmap populated
- [ ] JSON exported to repo

---

## SLO tie-in (slide 25)

Define consumer-facing SLOs (e.g. p99 end-to-end < 5s) and page on **symptoms**: lag, error rate, URP — not CPU alone.
