# Lab 04 - Monitor ISR and Leader Election

**Objective:** Watch ISR shrink when a broker fails, expand on recovery, and confirm no under-replicated partitions when healthy.

From **Kafka_Storage_Internals_Replication.pptx** — Slide 20.

---

## Prerequisites

- 3-broker cluster (same as [Lab 03](../lab-03-broker-failure-simulation/README.md))
- `set BS=localhost:9092,localhost:9094,localhost:9095`

---

## Concepts

| Setting | Typical value | Role |
|---------|---------------|------|
| `replica.lag.time.max.ms` | 30000 | Follower dropped from ISR if not fetching in time |
| `min.insync.replicas` | 2 (3-broker config) | Minimum ISR for `acks=all` |
| Under-replicated | URP | Follower not in ISR or behind leader |

---

## Step 1 - Create topic `isr-lab`

```bat
cd C:\Users\om\Desktop\KafKa\Day-5\labs
scripts\create-isr-lab-topic.bat
```

---

## Step 2 - Baseline ISR

```bat
scripts\describe-isr-lab.bat
```

Every partition should show `Isr: 1,2,3` and `Leader` assigned.

---

## Step 3 - Steady load producer

**Terminal A:**

```bat
cd %KAFKA_HOME%
set BS=localhost:9092,localhost:9094,localhost:9095
bin\windows\kafka-producer-perf-test.bat --topic isr-lab --num-records 100000 --record-size 50 --throughput 100 --producer-props bootstrap.servers=%BS%,acks=all
```

Leave running or let it finish — either works for ISR observation.

---

## Step 4 - Watch describe in a loop

**Terminal B:**

```bat
cd C:\Users\om\Desktop\KafKa\Day-5\labs
scripts\watch-isr-lab.bat
```

Runs `kafka-topics --describe` every 5 seconds.

---

## Step 5 - Simulate broker failure

Stop **broker 2** (port 9094):

- Close `start-broker2.bat` terminal, or end that Java process.

Within ~30 seconds you should see:

- `Isr` missing broker `2`
- `Replicas` still `1,2,3` but not all in sync

---

## Step 6 - Restart broker 2

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-local-3brokers\scripts
start-broker2.bat
```

Watch Terminal B until `Isr` returns to full set for all partitions.

---

## Step 7 - Check under-replicated partitions

```bat
cd %KAFKA_HOME%
set BS=localhost:9092,localhost:9094,localhost:9095
bin\windows\kafka-topics.bat --bootstrap-server %BS% --describe --under-replicated-partitions
```

**Healthy cluster:** empty output or no lines for `isr-lab`.

During failure you may see `isr-lab` partitions listed — note which replica is lagging.

---

## Step 8 - Optional — ISR troubleshooting scenario

From slide 19 (discussion):

- `replication.factor=3`, `min.insync.replicas=2`
- If ISR drops to `[1]` only, producers with `acks=all` get `NotEnoughReplicasException`
- Fix: restore brokers, fix network, reduce load on lagging follower

Check broker config:

```bat
findstr min.insync %KAFKA_HOME%\..\..\Day-2\Labs\kafka-local-3brokers\config\broker1.properties
```

(Or open `broker1.properties` in the Day-2 lab folder.)

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| ISR never shrinks | Broker process still running; wait `replica.lag.time.max.ms` |
| Perf test `NotEnoughReplicas` | Two brokers down; only one ISR member left |
| `watch` script errors | Set `KAFKA_HOME` |

---

## What you learned

- Live ISR changes during failure and recovery
- `--under-replicated-partitions` for ops checks
- Link between ISR size and `acks=all` success

---

## Next lab

→ [Lab 05 - Configure Retention](../lab-05-configure-retention/README.md) (single broker)
