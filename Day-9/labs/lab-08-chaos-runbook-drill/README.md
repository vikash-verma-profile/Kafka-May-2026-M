# Lab 08 — Chaos & Runbook Drill

**Objective:** Inject a network partition, measure alert time, follow a runbook, and document gaps.

From **Kafka_Security_Monitoring.pptx** — Slide 31. **Time:** ~40 min.

---

## Prerequisites

- 3-broker cluster + monitoring (Labs 04–07)
- Written runbook template (below)
- Linux VM or WSL for `iptables` **or** Windows firewall rules

---

## Step 1 — Prepare runbook

Create `runbook-broker-partition.md`:

1. **Detect** — alert name, dashboard link
2. **Triage** — check URP, offline partitions, controller
3. **Mitigate** — isolate network / restart broker
4. **Recover** — verify ISR, consumer lag
5. **Escalate** — when to page platform team

---

## Step 2 — Baseline health

Confirm all brokers RUNNING, URP=0, active controller=1.

---

## Step 3 — Inject partition (Linux example)

Block broker-1 ↔ broker-2 on port 9092:

```bash
sudo iptables -A INPUT -s <broker-2-ip> -j DROP
sudo iptables -A OUTPUT -d <broker-2-ip> -j DROP
```

**Windows alternative:** Disable network adapter on one broker VM briefly, or use [Toxiproxy](https://github.com/Shopify/toxiproxy).

---

## Step 4 — Time to alert

Start timer. **SLO target:** alert fires in **< 2 minutes** (per slide).

Record:

| Metric | Value |
|--------|-------|
| Partition start | |
| Alert fired | |
| Delta | |

---

## Step 5 — Execute runbook

Follow steps without improvising. Note any missing command or unclear ownership.

---

## Step 6 — Restore connectivity

```bash
sudo iptables -F
```

Verify cluster healed: URP=0, all brokers in ISR, consumers catching up.

---

## Step 7 — Post-incident notes

Answer:

1. **One runbook gap** — what was missing?
2. **One alert that did not fire** — false negative?

---

## Checkpoint

- [ ] Partition injected safely (lab only)
- [ ] Alert latency measured
- [ ] Runbook followed end-to-end
- [ ] Cluster fully recovered
- [ ] Post-incident doc completed

---

## Safety

Never run chaos exercises on production without change control and stakeholder approval.
