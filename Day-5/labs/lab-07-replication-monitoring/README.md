# Lab 07 - Replication Monitoring

**Objective:** Use CLI and broker metrics to monitor under-replicated partitions, ISR shrink/expand rates, and replication lag during a controlled failure.

From **Kafka_Storage_Internals_Replication.pptx** — Slide 28.

---

## Prerequisites

- 3-broker cluster running
- `set BS=localhost:9092,localhost:9094,localhost:9095`
- [Lab 04](../lab-04-monitor-isr-leader-election/README.md) — similar failure steps

---

## Concepts

| Metric / command | What it tells you |
|------------------|-------------------|
| `--under-replicated-partitions` | Partitions with out-of-sync replicas |
| `UnderReplicatedPartitions` (JMX) | Count of URP across broker |
| `IsrShrinksPerSec` / `IsrExpandsPerSec` | ISR membership changes |
| `FetcherLagMetrics` | Bytes behind leader per follower |
| Consumer lag | Separate from replication lag (`kafka-consumer-groups`) |

---

## Step 1 - Baseline cluster health

```bat
cd C:\Users\om\Desktop\KafKa\Day-5\labs
set BS=localhost:9092,localhost:9094,localhost:9095
scripts\check-under-replicated.bat
```

Empty output = good (no URP).

---

## Step 2 - Ensure replicated workload topic

Reuse `isr-lab` or create:

```bat
scripts\create-isr-lab-topic.bat
scripts\describe-isr-lab.bat
```

---

## Step 3 - CLI monitoring loop

**Terminal A** — refresh every 10s:

```bat
scripts\watch-isr-lab.bat
```

**Terminal B** — URP only:

```bat
scripts\check-under-replicated.bat
```

---

## Step 4 - Generate load

```bat
cd %KAFKA_HOME%
set BS=localhost:9092,localhost:9094,localhost:9095
bin\windows\kafka-producer-perf-test.bat --topic isr-lab --num-records 50000 --record-size 100 --throughput 200 --producer-props bootstrap.servers=%BS%,acks=all
```

---

## Step 5 - Induce replication stress

Stop broker 3 (`start-broker3.bat` terminal):

- `check-under-replicated.bat` should list `isr-lab` partitions
- `describe-isr-lab.bat` shows shrunk ISR

Note timestamps — how long until URP clears after restart?

---

## Step 6 - Restart and confirm recovery

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-local-3brokers\scripts
start-broker3.bat
```

When healthy:

- `check-under-replicated.bat` → no `isr-lab` lines
- All partitions `Isr: 1,2,3`

---

## Step 7 - JMX metrics (optional)

Enable JMX on broker start (example for broker 1 — adjust port per broker):

```bat
set KAFKA_JMX_OPTS=-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
```

Restart broker with those opts, then use **JConsole** or **Prometheus JMX exporter** to watch:

| MBean pattern | Name |
|---------------|------|
| `kafka.server:type=ReplicaManager,name=UnderReplicatedPartitions` | URP count |
| `kafka.server:type=ReplicaManager,name=IsrShrinksPerSec` | ISR shrinks |
| `kafka.server:type=ReplicaManager,name=IsrExpandsPerSec` | ISR expands |

Fetcher lag: `kafka.server:type=FetcherLagMetrics,name=ConsumerLag,clientId=*,topic=*,partition=*`

---

## Step 8 - Consumer lag side-by-side

Replication lag ≠ consumer lag. Demo consumer group:

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server %BS% --topic isr-lab --group monitor-demo --from-beginning
```

In another window:

```bat
bin\windows\kafka-consumer-groups.bat --bootstrap-server %BS% --describe --group monitor-demo
```

| Column | Meaning |
|--------|---------|
| `LAG` | Consumer behind log end |
| URP | Broker replica behind leader |

Alerting rule of thumb from slides:

- URP > 0 for **5+ minutes** → investigate broker/network/disk
- ISR shrinks faster than expands → unstable cluster

---

## Challenge

Write three alert rules for a production checklist:

1. **URP** threshold and duration  
2. **ISR shrink rate** vs expand rate  
3. **Leader election** spike (many elections per minute)

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| JMX connection refused | Windows firewall; JMX port not set on running JVM |
| URP stuck after recovery | Broker logs; disk full; inter-broker port blocked |
| No metrics in JConsole | Connect to `localhost:9999` with SSL disabled |

---

## What you learned

- Ops commands for replication health
- Difference between replica lag and consumer lag
- JMX starting point for Prometheus/Grafana

---

## Course wrap-up

Review slide 30 (**Production challenge**) — design `orders` topic with `replication.factor=3`, `min.insync.replicas=2`, `acks=all`, retention for 7 days, and monitoring from this lab.
