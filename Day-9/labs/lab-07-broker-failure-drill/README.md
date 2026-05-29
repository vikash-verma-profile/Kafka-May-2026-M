# Lab 07 — Broker Failure Drill

**Objective:** While continuously producing with strong durability settings (`acks=all`), **kill one broker** in a 3-broker cluster and confirm messages are not lost and the cluster recovers.

**Source:** Kafka_Security_Monitoring.pptx — Slide 28  
**Time:** ~30 minutes  
**Requires:** **3-broker** cluster + Grafana from [Lab 06](../lab-06-grafana-dashboard/README.md) (recommended)

---

## What you are learning

- **Replication factor (RF)** — copies of each partition across brokers.
- **min.insync.replicas** — minimum in-sync copies required for `acks=all` writes.
- **ISR** — replicas caught up enough to be promoted if leader dies.
- **URP** — under-replicated partitions (temporary during failure).

---

## Before you start — checklist

- [ ] Three brokers running (example ports: **9092**, **9094**, **9095**)
- [ ] You know which terminal/window is **broker-2** (you will kill this one)
- [ ] Grafana dashboard open (URP panel from Lab 06)
- [ ] ~30 minutes uninterrupted time

---

## Step 0 — Start the 3-broker cluster

Use [my-config/README.md](../my-config/README.md): controller + `broker-1` / `broker-2` / `broker-3` properties.

**Verify all brokers registered:**

```bat
cd /d %KAFKA_HOME%
bin\windows\kafka-broker-api-versions.bat --bootstrap-server localhost:9092,localhost:9094,localhost:9095
```

**What success looks like:** Response from cluster without timeout.

---

## Step 1 — Create resilient topic `drill-orders`

### Option A — Lab script

```bat
cd /d c:\Users\om\Desktop\KafKa\Day-9\labs\scripts
create-drill-topic.bat
```

### Option B — Manual command

```bat
bin\windows\kafka-topics.bat --create --topic drill-orders ^
  --bootstrap-server localhost:9092 ^
  --partitions 6 --replication-factor 3 ^
  --config min.insync.replicas=2
```

**If topic exists:** Skip create; go to describe.

### Verify partition layout

```bat
bin\windows\kafka-topics.bat --describe --topic drill-orders --bootstrap-server localhost:9092
```

**What success looks like (for each partition):**

- **Leader** — one broker id
- **Replicas** — three broker ids (e.g. 1,2,3)
- **Isr** — all three ids listed

**First-time tip:** If RF is only 1, this lab is invalid — you need 3 brokers and RF=3.

---

## Step 2 — Run continuous producer with `acks=all`

Open a dedicated terminal — leave it running **5+ minutes**.

```bat
cd /d %KAFKA_HOME%
bin\windows\kafka-producer-perf-test.bat --topic drill-orders ^
  --num-records 1000000 --record-size 256 --throughput 1000 ^
  --producer-props acks=all enable.idempotence=true bootstrap.servers=localhost:9092,localhost:9094,localhost:9095,localhost:9094,localhost:9095
```

**What these flags mean:**

| Property | Meaning |
|----------|---------|
| `acks=all` | Wait until all in-sync replicas ack (durable) |
| `enable.idempotence=true` | Avoid duplicate sequence numbers on retry |
| Multiple bootstrap servers | Client finds alive brokers after failure |

**What success looks like:** Steady output of throughput stats; occasional pause OK during failover.

---

## Step 3 — Kill broker-2 (controlled failure)

**Do this only on a lab cluster.**

1. Identify broker-2’s process (second CMD window or service).
2. Stop it: close window or:

   ```bat
   taskkill /FI "WINDOWTITLE eq broker-2*" /F
   ```

   (Adjust to how your cluster scripts label windows.)

3. **Watch Grafana** (Lab 06):

   - `UnderReplicatedPartitions` may spike briefly
   - Leader election — new leaders on remaining brokers

4. Note time on clock — how long until producer recovers?

**Expected behavior:**

- With **2 of 3** brokers alive and `min.insync.replicas=2`, producer should resume after short errors.
- Sustained `NOT_ENOUGH_REPLICAS` means ISR too small — check `min.insync.replicas` and which brokers are in ISR.

---

## Step 4 — Observe producer during outage

Stay on perf test output.

| Observation | Good / investigate |
|-------------|-------------------|
| Brief pause then throughput resumes | **Good** |
| Continuous errors > 2 min | Check how many brokers actually up |
| Zero throughput but no errors | Bootstrap might point at dead broker only — use all three in `bootstrap.servers` |

---

## Step 5 — Restore broker-2

1. Start broker-2 the same way you started it initially (same config files).
2. Watch Grafana: URP should return to **0**.
3. Describe topic again:

   ```bat
   bin\windows\kafka-topics.bat --describe --topic drill-orders --bootstrap-server localhost:9092
   ```

**What success looks like:** ISR lists all replicas again for each partition.

---

## Step 6 — Verify message count (no data loss)

### 6.1 Note records sent

From perf test summary when it finishes (or note approximate count if still running).

### 6.2 Consume from beginning and count

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 ^
  --topic drill-orders --from-beginning --group drill-verify-once ^
  --timeout-ms 10000
```

For large topics, use offset tools instead:

```bat
bin\windows\kafka-run-class.bat kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic drill-orders --time -1
```

Compare end offsets sum to producer record count (within idempotent semantics).

**What success looks like:** No large gap between sent and available messages.

---

## Checkpoint — you are done when

- [ ] Topic has RF=3 and `min.insync.replicas=2`
- [ ] Producer survived single broker loss (brief pause OK)
- [ ] URP returned to 0 after broker rejoined
- [ ] Message counts consistent (no major loss)

---

## Failure modes reference (slide 27)

| Symptom | Likely cause |
|---------|----------------|
| URP > 0 | Broker down or slow follower |
| Offline partitions | No ISR quorum for leader election |
| Produce errors with `acks=all` | ISR size < `min.insync.replicas` |
| Only one broker ever in ISR | Other brokers not in cluster or misconfigured racks |

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| Cannot create RF=3 topic | Fewer than 3 brokers running |
| Immediate data loss | RF=1 or `acks=1` — redo topic and producer settings |
| Broker won't rejoin | Check `log.dirs`, disk full, same `cluster.id` |
| Perf test stops permanently | Restart with all bootstrap hosts |

---

## What’s next?

[Lab 08](../lab-08-chaos-runbook-drill/README.md) — network partition + formal runbook exercise.
