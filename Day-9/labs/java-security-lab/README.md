# Day 9 — Java Security Lab

**Java track** for the same client exercises as [python-security-lab](../python-security-lab/). Broker setup (JAAS, listeners, ACLs, TLS) stays in each lab README and uses Kafka CLI / `.bat` scripts.

| Script | Lab | Purpose |
|--------|-----|---------|
| `Lab01ScramProducer` | 01 | Send one message with SASL/SCRAM |
| `Lab05LagCheck` | 05 | Print consumer group lag by partition |

---

## Before you start — checklist

- [ ] **JDK 11+** and **Maven 3.8+** installed
- [ ] Lab 01 broker steps done for Java Lab 01 (SASL port **9093**, user `alice` / `secret`)
- [ ] `KAFKA_HOME` set if you use [../scripts](../scripts/) batch files

Verify:

```bat
java -version
mvn -version
```

---

## Step 1 — Open this folder

```powershell
cd c:\Users\om\Desktop\KafKa\Day-9\labs\java-security-lab
```

---

## Step 2 — Build once

```bat
mvn -q compile
```

**What success looks like:** `BUILD SUCCESS` and `target/classes` exists.

---

## Step 3 — Create SCRAM user (if not done)

```bat
cd c:\Users\om\Desktop\KafKa\Day-9\labs\scripts
create-scram-user.bat localhost:9092 alice secret
```

Match [../configs/client-scram.properties](../configs/client-scram.properties).

---

## Lab 01 — SCRAM producer

**When:** After cluster is running with SASL on **9096** — [Lab 01 README](../lab-01-sasl-scram-authentication/README.md) and [my-config/README](../my-config/README.md).

```bat
cd c:\Users\om\Desktop\KafKa\Day-9\labs\java-security-lab
mvn -q exec:java -Dexec.mainClass=day9.labs.Lab01ScramProducer -Dexec.args="localhost:9096 orders alice secret"
```

| Argument | Default | Meaning |
|----------|---------|---------|
| bootstrap | `localhost:9093` (env: `KAFKA_BOOTSTRAP`) | Use **9096** for this lab cluster |
| topic | `orders` | Topic name |
| username | `alice` | SCRAM user |
| password | `secret` | SCRAM password |

Optional env: `set KAFKA_BOOTSTRAP=localhost:9093`

**Expected:** `Sent to orders partition=... offset=...`

---

## Lab 05 — Consumer lag check

**When:** Topic has data and group `billing-svc` exists — [Lab 05 README](../lab-05-consumer-lag-alerting/README.md).

```bat
mvn -q exec:java -Dexec.mainClass=day9.labs.Lab05LagCheck -Dexec.args="localhost:9092 billing-svc"
```

**Expected:** Lines with `committed=`, `end=`, `lag=` per partition.

---

## Pick Java vs Python vs Shell

| You prefer | Use |
|------------|-----|
| Kafka CLI + `.bat` | [scripts/](../scripts/) + lab README shell steps |
| **Java** (kafka-clients) | This folder |
| **Python** (kafka-python) | [python-security-lab](../python-security-lab/) |

All three hit the same broker; only the client library changes.

---

## Troubleshooting

| Error | Fix |
|-------|-----|
| `mvn` not found | Install Maven; add to PATH |
| `Authentication failed` | Run `create-scram-user.bat`; check password |
| `NoBrokersAvailable` | Broker down or wrong port |
| `UnknownTopicOrPartition` | Create topic (Lab 01 shell Step 4) |
| Lag shows no partitions | Start consumer once with `--group billing-svc` |

---

## Next steps

- Lab 01 Java done → [Lab 02 ACLs](../lab-02-kafka-acls/README.md) (shell)
- Lab 05 Java done → [Lab 06 Grafana](../lab-06-grafana-dashboard/README.md)
