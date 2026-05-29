# Lab 08 — Chaos & Runbook Drill

**Objective:** Practice a real incident flow: inject a **network partition**, measure how fast monitoring alerts you, follow a **written runbook** (no improvisation), then document gaps.

**Source:** Kafka_Security_Monitoring.pptx — Slide 31  
**Time:** ~40 minutes  
**Requires:** 3-broker cluster + monitoring from Labs [04](../lab-04-jmx-prometheus/README.md)–[07](../lab-07-broker-failure-drill/README.md)

---

## What you are learning

- **Chaos engineering** — break things on purpose in a safe environment to learn failure modes.
- **Runbook** — step-by-step doc so anyone on call can respond the same way.
- **Alert latency** — time from failure to firing; course SLO target: **< 2 minutes** (slide).

---

## Safety first

| Rule | Why |
|------|-----|
| **Lab cluster only** | Partition rules can break production traffic |
| **Tell your teammate** | Someone should watch dashboards while you inject fault |
| **Have a rollback plan** | Know how to remove firewall/iptables rules before starting |
| **Never on production** without change control and approval |

---

## Before you start — checklist

- [ ] Labs 04–07 done (metrics, alerts, Grafana, broker drill)
- [ ] 3-broker cluster from [my-config](../my-config/README.md)
- [ ] Stopwatch or phone timer ready
- [ ] Blank doc for post-incident notes
- [ ] WSL/Linux **or** Windows firewall **or** Toxiproxy — pick one isolation method below

---

## Step 0 — Copy runbook template

Create file: `runbook-broker-partition.md` (in this lab folder or your team wiki).

```markdown
# Runbook: Broker network partition

## 1. Detect
- Alert name: ______________________ (e.g. UnderReplicatedPartitions, ConsumerLagHigh)
- Dashboard: ______________________ (Grafana link)

## 2. Triage (first 5 minutes)
- [ ] Check under-replicated partitions: PromQL `sum(kafka_server_replicamanager_underreplicatedpartitions)`
- [ ] Check offline partitions: `sum(kafka_controller_kafkacontroller_offlinepartitionscount)`
- [ ] Check active controller count (= 1?)
- [ ] Note which broker IDs look isolated

## 3. Mitigate
- [ ] Confirm whether single broker or network split
- [ ] If lab: remove partition / restart broker per procedure
- [ ] If prod: escalate before rebooting — fill in your org steps

## 4. Recover
- [ ] Restore connectivity
- [ ] Wait for ISR to heal (URP = 0)
- [ ] Check consumer lag trending down

## 5. Escalate when
- Offline partitions > 0 for > ___ minutes
- Cannot identify controller
- Data loss suspected on `acks=all` topics
```

Fill in alert names and dashboard URLs from **your** Lab 05–06 setup before continuing.

---

## Step 1 — Baseline health (before chaos)

Run through this table and record values:

| Check | Command / location | Healthy value | Your value |
|-------|-------------------|---------------|------------|
| Brokers up | Cluster start scripts / JMX | 3 running | |
| URP | Grafana stat panel | 0 | |
| Offline partitions | Grafana | 0 | |
| Active controller | Grafana | 1 | |
| Consumer lag | Grafana / Prometheus | Low | |

**Do not inject fault until baseline is green.**

---

## Step 2 — Choose partition method

### Option A — Linux / WSL `iptables` (course example)

Block traffic between broker-1 and broker-2 on Kafka port (replace IPs):

```bash
# On broker-1 host — replace with real IPs
sudo iptables -A INPUT -s <broker-2-ip> -j DROP
sudo iptables -A OUTPUT -d <broker-2-ip> -j DROP
```

**Find IPs:** `ip addr` (Linux) or `ipconfig` (Windows).

### Option B — Windows Firewall (beginner-friendly on Windows)

1. **Windows Defender Firewall** → **Advanced settings**
2. **Outbound Rules** → **New Rule** → block traffic from broker-1 machine to broker-2 IP on port **9092** (and other broker ports you use)
3. Document exact rule name so you can delete it in Step 6

### Option C — Toxiproxy

1. Install [Toxiproxy](https://github.com/Shopify/toxiproxy)
2. Put proxy between two broker hosts; enable `timeout` or `partition` toxic
3. Safer for repeated drills — no OS firewall changes

Pick **one** method and write it in your runbook §3.

---

## Step 3 — Inject partition

1. **Start timer** when rule is applied.
2. Do **not** fix anything yet — observe dashboards and alerts.
3. Optional: keep [Lab 07](../lab-07-broker-failure-drill/README.md) perf producer running for extra realism.

**What you might see:**

- URP increases
- Leader elections
- Consumer lag may rise if producers/consumers affected

---

## Step 4 — Measure time to alert

Record in your notes:

| Metric | Value |
|--------|-------|
| Partition start (clock time) | |
| First alert fired (name + time) | |
| Delta (goal < 2 min) | |

Check:

- Prometheus **Alerts** page
- Grafana annotations (if configured)

**If no alert fired:** That is a valid finding — document as false negative in Step 7.

---

## Step 5 — Execute runbook (no improvisation)

1. Open `runbook-broker-partition.md`.
2. Follow **Detect → Triage → Mitigate** in order.
3. Check every box literally.
4. If a step is impossible or unclear, **write it down** — that is a runbook gap.

**Partner role:** One person runs commands; one reads runbook aloud (simulates on-call handoff).

---

## Step 6 — Restore connectivity

### Linux iptables

```bash
sudo iptables -F
```

### Windows

Disable or delete the firewall rules you created.

### Toxiproxy

Remove toxics or stop proxy.

**Verify recovery:**

| Check | Target |
|-------|--------|
| URP | 0 |
| All brokers in cluster | 3 |
| ISR | Full on sample topic `drill-orders` |
| Consumer lag | Decreasing |

```bat
bin\windows\kafka-topics.bat --describe --topic drill-orders --bootstrap-server localhost:9092
```

---

## Step 7 — Post-incident review (required)

Answer in writing:

1. **One runbook gap** — What command, link, or owner was missing?
2. **One alert gap** — Which failure had no alert (false negative)? Or which alert was noisy (false positive)?
3. **Alert timing** — Met < 2 min SLO or not? Why?

Example gap notes:

- “Runbook did not say which broker ID maps to which hostname.”
- “URP alert fired at 90s — OK. ConsumerLagHigh never fired — false negative.”

---

## Checkpoint — you are done when

- [ ] Partition injected safely on lab cluster only
- [ ] Alert latency measured and recorded
- [ ] Runbook followed end-to-end without skipping steps
- [ ] Cluster fully recovered (URP=0, all brokers up)
- [ ] Post-incident doc with 2 gaps + timing

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| Cluster totally unreachable | Firewall too broad — flush rules (`iptables -F`) |
| No metric change | Partition did not affect broker ports — verify IPs/ports |
| Cannot remove Windows rule | Note rule GUID in Advanced Firewall; delete inbound+outbound pair |
| Alert flaps | Increase `for:` in Prometheus rule after drill |

---

## Congratulations

You completed the Day 9 security and monitoring lab track. Review weak areas (SCRAM, ACLs, TLS, or alerting) by re-running only the labs that felt hardest.
