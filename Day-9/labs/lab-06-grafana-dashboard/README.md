# Lab 06 — Grafana Dashboard Build

**Objective:** Build a single-pane view of Kafka health: broker stats, throughput, consumer lag, and alert overlays.

**Source:** Kafka_Security_Monitoring.pptx — Slide 24  
**Time:** ~30 minutes  
**Requires:** Prometheus + metrics from [Lab 04](../lab-04-jmx-prometheus/README.md) and ideally lag metrics from [Lab 05](../lab-05-consumer-lag-alerting/README.md)

---

## What you are learning

- **Grafana panel** — one chart or stat tied to a PromQL query.
- **Dashboard-as-code** — export JSON so your dashboard can live in Git.
- **SLO mindset** — watch symptoms users feel (lag, errors, URP), not only CPU (slide 25).

---

## Before you start — checklist

- [ ] Grafana: **Docker** or **native** install (see below)
- [ ] Prometheus running from [Lab 04](../lab-04-jmx-prometheus/README.md) (`http://localhost:9090`)
- [ ] Broker metrics flowing (Lab 04)
- [ ] Optional: 3-broker cluster for richer replica metrics

---

## Docker for this lab (optional)

| Component | Docker required? | Command |
|-----------|------------------|---------|
| Kafka broker | **No** | Host `%KAFKA_HOME%` |
| Prometheus | **Optional** | Already from Lab 04 — `docker compose up -d prometheus` |
| **Grafana** | **Optional** | `docker compose up -d grafana` |

**Start full monitoring stack (Prometheus + Grafana + lag-exporter):**

```bat
cd c:\Users\om\Desktop\KafKa\Day-9\labs\docker
docker compose up -d
```

Open Grafana: `http://localhost:3000` (user `admin` / password `admin`).

**Prometheus datasource URL in Grafana:**

| Grafana runs as… | Prometheus URL |
|----------------|----------------|
| Native (browser on PC) | `http://localhost:9090` |
| Docker (this compose) | `http://prometheus:9090` |

Details: **[docker/README.md](../docker/README.md)**.

---

## Step 0 — Add Prometheus as a data source

1. Grafana → **Connections** → **Data sources** → **Add data source**
2. Choose **Prometheus**
3. URL: `http://localhost:9090`
4. Click **Save & test** — message should be green (“Data source is working”).

---

## Step 1 — Create a new dashboard

1. **Dashboards** → **New** → **New dashboard**
2. Click **Add visualization**
3. Select **Prometheus** datasource

You will add four **Stat** panels in the top row.

---

## Step 2 — Top row — four Stat panels

For each panel: set visualization type to **Stat**, then use the query below.

| Panel title | PromQL query | Healthy value (typical) |
|-------------|--------------|-------------------------|
| Broker count | `count(kafka_server_replicamanager_leadercount)` | Matches number of brokers (e.g. 1 or 3) |
| Under-replicated partitions | `sum(kafka_server_replicamanager_underreplicatedpartitions)` | **0** |
| Offline partitions | `sum(kafka_controller_kafkacontroller_offlinepartitionscount)` | **0** |
| Active controller | `sum(kafka_controller_kafkacontroller_activecontrollercount)` | **1** |

**How to add each panel (first time):**

1. **Add visualization** → enter query → **Run queries**
2. See a number? Good — set panel title in the right sidebar
3. **Apply** → back to dashboard
4. Repeat for all four

**What success looks like:** URP and offline = 0; controller = 1 on a healthy cluster.

**If “No data”:** Broker jmx_exporter not scraped — fix Lab 04 first.

---

## Step 3 — Throughput time series (Bytes In / Out)

### 3.1 Bytes In panel

1. Add visualization → **Time series**
2. Query:

   ```promql
   topk(10, sum by (topic) (rate(kafka_server_BrokerTopicMetrics_BytesInPerSec[1m])))
   ```

3. Title: `Bytes In per Topic`

### 3.2 Bytes Out panel

Duplicate panel; change metric to `BytesOutPerSec`:

```promql
topk(10, sum by (topic) (rate(kafka_server_BrokerTopicMetrics_BytesOutPerSec[1m])))
```

### 3.3 Generate traffic so graphs move

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic orders
```

**Expected:** Lines rise while producing.

---

## Step 4 — Consumer lag heatmap

1. Add visualization → choose **Heatmap** (or Time series if heatmap unavailable in your version)
2. Query:

   ```promql
   kafka_consumergroup_group_lag_seconds
   ```

3. Requires lag exporter (Lab 05). If empty, re-run Lab 05 simulation.

**Reading the heatmap:** Brighter cells = more lag for that group/partition.

---

## Step 5 — Alertmanager annotations (optional)

Shows when alerts fired on the same timeline as throughput.

1. Add **Alertmanager** datasource (if you run Alertmanager) — URL e.g. `http://localhost:9093`
2. Dashboard **Settings** (gear) → **Annotations** → **Add annotation query**
3. Select Alertmanager source

**Without Alertmanager:** Skip this step; Prometheus **Alert** history is enough for the course.

---

## Step 6 — Export dashboard JSON (dashboard-as-code)

1. Dashboard **Settings** → **JSON Model** → **Copy** or **Save to file**
2. Save in repo as:

   ```text
   Day-9/labs/dashboards/kafka-cluster-overview.json
   ```

3. Create `dashboards` folder if it does not exist.

**Why export?** Teammates can **Import** the same dashboard without rebuilding panels.

---

## Checkpoint — you are done when

- [ ] Four stat panels on top row with sensible values
- [ ] Bytes in/out charts react to producer traffic
- [ ] Lag panel shows data (after Lab 05)
- [ ] JSON file saved under `labs/dashboards/`

---

## SLO tie-in (slide 25)

Example consumer-facing SLO: *p99 end-to-end delivery under 5 seconds.*

| Symptom to page on | Why |
|--------------------|-----|
| Lag in seconds | Users see stale data |
| Under-replicated partitions | Risk of loss on next failure |
| Offline partitions | Availability breach |
| Error rate on produce/consume | App cannot publish |

CPU alone is a poor page trigger — Kafka can be unhealthy while CPU looks fine.

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| All panels “No data” | Prometheus datasource URL wrong; targets DOWN |
| Topic list empty | No traffic — run producer |
| Lag panel empty | Start kafka-lag-exporter (Lab 05) |
| Too many topics in legend | `topk(10, ...)` limits clutter |

---

## What’s next?

[Lab 07](../lab-07-broker-failure-drill/README.md) — stop a broker and verify producers with `acks=all` survive.
