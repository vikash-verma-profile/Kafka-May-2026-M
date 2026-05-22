# Lab 03 - Broker Failure Simulation

**Objective:** Run a 3-broker cluster, create a replicated topic, kill the leader broker, and observe automatic leader election and ISR changes.

From **Kafka_Storage_Internals_Replication.pptx** — Slide 15.

---

## Prerequisites

- [Lab 00](../lab-00-initial-setup/README.md) — **3-broker cluster** running
- Bootstrap: `localhost:9092,localhost:9094,localhost:9095`
- [Day-2 3-broker guide](../../Day-2/Labs/kafka-local-multi-broker-cli-lab.md)

---

## Concepts

| Term | Meaning |
|------|---------|
| Leader | Broker serving reads/writes for a partition |
| Follower | Replica catching up via fetch from leader |
| ISR | In-sync replicas (eligible for clean leader election) |
| `acks=all` | Producer waits for ISR acknowledgment |

---

## Step 1 - Start 3 brokers

Three terminals:

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-local-3brokers\scripts
start-broker1.bat
start-broker2.bat
start-broker3.bat
```

Verify:

```bat
set BS=localhost:9092,localhost:9094,localhost:9095
cd %KAFKA_HOME%
bin\windows\kafka-broker-api-versions.bat --bootstrap-server %BS%
```

---

## Step 2 - Create replicated topic

```bat
cd C:\Users\om\Desktop\KafKa\Day-5\labs
scripts\create-failover-lab-topic.bat
```

Manual:

```bat
set BS=localhost:9092,localhost:9094,localhost:9095
bin\windows\kafka-topics.bat --create --topic failover-lab --bootstrap-server %BS% --partitions 3 --replication-factor 3
```

---

## Step 3 - Note current leaders

```bat
scripts\describe-failover-lab.bat
```

Example columns:

```text
Topic: failover-lab  Partition: 0  Leader: 2  Replicas: 1,2,3  Isr: 1,2,3
```

Write down which **broker id** is leader for partition 0 (Leader column = `node.id`).

| Client port | Broker `node.id` |
|-------------|------------------|
| 9092 | 1 |
| 9094 | 2 |
| 9095 | 3 |

---

## Step 4 - Continuous producer with acks=all

**Terminal A** — keep running:

```bat
cd %KAFKA_HOME%
set BS=localhost:9092,localhost:9094,localhost:9095
bin\windows\kafka-console-producer.bat --bootstrap-server %BS% --topic failover-lab --producer-property acks=all
```

Type messages every second (`evt-1`, `evt-2`, …).

Watch for brief errors if you kill a broker — clients retry after metadata refresh.

---

## Step 5 - Kill the leader broker

Identify the terminal running the leader JVM (from Step 3).

- **Clean test:** close that terminal or `Ctrl+C` once.
- **Hard failure (slide scenario):** Task Manager → End `java.exe` for that broker only.

Example: if partition 0 leader is broker 2, stop the terminal running `start-broker2.bat`.

---

## Step 6 - Re-describe topic

**Terminal B** (wait 5–15 seconds):

```bat
cd C:\Users\om\Desktop\KafKa\Day-5\labs
scripts\describe-failover-lab.bat
```

Observe:

- **New Leader** on surviving broker
- **ISR** shrunk (failed broker id missing)
- Possible **Under-replicated** until followers catch up

---

## Step 7 - Restart failed broker

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-local-3brokers\scripts
start-broker2.bat
```

(Use the script matching the broker you stopped.)

Run `describe-failover-lab.bat` every 10 seconds. ISR should grow back to `1,2,3` when the follower catches up.

---

## Step 8 - Producer recovery check

Producer terminal should resume without manual restart (may log `NotLeaderForPartition` briefly). If it stopped, restart console producer and confirm messages still land.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `InvalidReplicationFactor` | Not all 3 brokers up |
| No leader change | Wait longer; check controller logs |
| Producer stuck | Restart producer; verify `%BS%` lists all live brokers |
| ISR never full | Restart remaining brokers one at a time |

---

## What you learned

- Replication survives single broker loss
- Controller elects new leader from ISR
- ISR and preferred leader rebalance after recovery

---

## Next lab

→ [Lab 04 - Monitor ISR and Leader Election](../lab-04-monitor-isr-leader-election/README.md)
