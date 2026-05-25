# Lab 10 — Capacity Plan a Cluster

**Objective:** Size brokers, partitions, and disk from peak throughput, retention, and replication factor.

From **Kafka_cap.pptx** — Slide 38.

---

## Implementation

| Track | Command |
|-------|---------|
| **Manual** | Fill [capacity-plan.template.md](capacity-plan.template.md) |
| **Python** | `python lab10_capacity_plan.py` in [python-production-lab](../../python-production-lab/) |

---

## Workload spec (from slide)

| Parameter | Value |
|-----------|-------|
| Peak throughput | 100 MB/s |
| Retention | 7 days |
| Replication factor | 3 |
| Headroom | 30% |

---

## Step 1 — Daily ingest (raw)

```
100 MB/s × 86,400 s/day = 8,640 GB/day ≈ 8.64 TB/day
```

---

## Step 2 — Total storage with retention × RF

```
8.64 TB/day × 7 days × 3 (RF) = 181.44 TB
```

---

## Step 3 — Add headroom

```
181.44 TB × 1.30 = 235.87 TB ≈ 236 TB provisioned
```

---

## Step 4 — Broker count

Assume **30 TB usable** per broker (after OS + overhead):

```
236 TB / 30 TB ≈ 8 brokers → round to 6–9 depending on CPU/network
```

**Slide expected answer:** 6–9 brokers, 235 TB with 30% headroom.

---

## Step 5 — Partition sizing

Target **25–50 MB/s per partition**:

```
100 MB/s / 25 MB/s = 4 partitions minimum
```

For parallelism and hot-key spread, plan **12–24 partitions** per high-throughput topic.

---

## Step 6 — Document in `capacity-plan.md`

| Item | Your calculation |
|------|------------------|
| Daily ingest | |
| Total disk (7d × RF) | |
| With headroom | |
| Broker count | |
| Partitions per topic | |
| Network (Gbps) | 100 MB/s ≈ 0.8 Gbps + replication overhead |

---

## Checkpoint

- [ ] Spreadsheet or markdown with all formulas
- [ ] Broker count justified
- [ ] Partition count justified
- [ ] Headroom explained

---

## Capacity tips (Day 9 slide 30)

- Cap brokers at ~70% disk, ~60% CPU at peak
- Keep < 4,000 partitions per broker (rule of thumb)
- Forecast monthly from `BytesInPerSec` metrics
