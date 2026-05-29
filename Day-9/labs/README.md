# Day 9 — Kafka Security & Monitoring Labs

Hands-on labs from **Kafka_Security_Monitoring.pptx**. Each lab builds on the previous one. **Use a dedicated test cluster only** — never run these exercises on production.

---

## Lab order (do them in sequence)

| # | Folder | What you will learn | Approx. time |
|---|--------|---------------------|--------------|
| 01 | [lab-01-sasl-scram-authentication](lab-01-sasl-scram-authentication/README.md) | Create SCRAM users and connect with username/password | ~20 min |
| 02 | [lab-02-kafka-acls](lab-02-kafka-acls/README.md) | Grant and revoke who can read/write topics | ~25 min |
| 03 | [lab-03-tls-sasl-end-to-end](lab-03-tls-sasl-end-to-end/README.md) | Encrypt traffic with TLS + keep SCRAM auth | ~30 min |
| 04 | [lab-04-jmx-prometheus](lab-04-jmx-prometheus/README.md) | Export broker metrics to Prometheus | ~25 min |
| 05 | [lab-05-consumer-lag-alerting](lab-05-consumer-lag-alerting/README.md) | Alert when consumers fall behind | ~20 min |
| 06 | [lab-06-grafana-dashboard](lab-06-grafana-dashboard/README.md) | Build a cluster health dashboard | ~30 min |
| 07 | [lab-07-broker-failure-drill](lab-07-broker-failure-drill/README.md) | Kill a broker and confirm no data loss | ~30 min |
| 08 | [lab-08-chaos-runbook-drill](lab-08-chaos-runbook-drill/README.md) | Simulate outages and follow a runbook | ~40 min |

---

## First-time setup (do this once before Lab 01)

### 1. Install and verify software

| Tool | Why you need it | How to check |
|------|-----------------|--------------|
| **Apache Kafka** (3.x–4.x, KRaft mode) | Runs the brokers and CLI tools | Example: `C:\kafka-bin\kafka_2.13-4.2.0` |
| **Java JDK 11+** | Kafka runs on the JVM | `java -version` |
| **Maven 3.8+** (optional) | Java lab track | `mvn -version` |
| **Python 3.9+** (optional) | Python lab track | `python --version` |
| **Docker Desktop** (optional, Labs 04–06) | Prometheus, Grafana, lag-exporter | `docker version` — see [docker/README.md](docker/README.md) |
| **Prometheus + Grafana** (Labs 04–06) | Monitoring stack | Native install **or** Docker compose |
| **Git Bash or PowerShell** | Run commands | You are reading this — good |

### 2. Set environment variables (Windows)

Open **PowerShell** or **Command Prompt** and run (adjust the path to where you unpacked Kafka):

```bat
set KAFKA_HOME=C:\kafka-bin\kafka_2.13-4.2.0
set PATH=%KAFKA_HOME%\bin\windows;%PATH%
```

PowerShell equivalent:

```powershell
$env:KAFKA_HOME = "C:\kafka-bin\kafka_2.13-4.2.0"
$env:PATH = "$env:KAFKA_HOME\bin\windows;$env:PATH"
```

To make this permanent (optional): Windows **Settings → System → About → Advanced system settings → Environment Variables** → add `KAFKA_HOME` under User variables.

**What success looks like:** In a new terminal, `echo %KAFKA_HOME%` (CMD) or `echo $env:KAFKA_HOME` (PowerShell) prints your Kafka path.

### 3. Clone or open this repo

Your working folder should be:

```text
c:\Users\om\Desktop\KafKa\Day-9\labs\
```

All lab READMEs assume commands are run from `%KAFKA_HOME%` for Kafka CLI, or from `Day-9\labs\` for scripts, Java, and Python.

### 4. Start the lab cluster (recommended: use `my-config/`)

**Full step-by-step:** [my-config/README.md](my-config/README.md)

| Labs | Cluster needed |
|------|----------------|
| **01–06** (security + monitoring) | Controller + **3 brokers** (this repo’s default) |
| **07–08** (failure + chaos drills) | Same 3-broker cluster |

**Start order (4 terminals):**

1. Controller → `my-config/controller.properties` (port **9093**)
2. Broker-1 → `my-config/broker-1.properties` + `KAFKA_OPTS` + `kafka_server_jaas.conf`
3. Broker-2 → `my-config/broker-2.properties` + `KAFKA_OPTS`
4. Broker-3 → `my-config/broker-3.properties` + `KAFKA_OPTS`

**Verify PLAINTEXT:**

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-broker-api-versions.bat --bootstrap-server localhost:9092
```

**Verify SCRAM:**

```bat
bin\windows\kafka-broker-api-versions.bat --bootstrap-server localhost:9096 --command-config c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\client-scram-oneshot.properties
```

**Stuck?** See [TROUBLESHOOTING.md](TROUBLESHOOTING.md).

---

## Repo layout (where files live)

| Path | Purpose |
|------|---------|
| [configs/](configs/) | Client SCRAM/TLS templates — see [configs/README.md](configs/README.md) |
| [monitoring/](monitoring/) | Prometheus scrape config and alert rules |
| [scripts/](scripts/) | Helper `.bat` files (create user, grant ACLs) |
| [my-config/](my-config/) | **Your working KRaft configs** (controller + 3 brokers + JAAS) |
| [java-security-lab/](java-security-lab/) | Java (`kafka-clients`) producer and lag checker |
| [python-security-lab/](python-security-lab/) | Python (`kafka-python`) producer and lag checker |
| [docker/](docker/) | **Docker map** + optional compose for Labs 04–06 |
| [TROUBLESHOOTING.md](TROUBLESHOOTING.md) | Errors we fixed (ports, SASL, JAAS, Kafka 4.x) |

---

## Your exact lab cluster (the setup we fixed)

This repo includes a ready-to-run **KRaft controller + 3 brokers** setup in `my-config/`.

### Ports used

| Component | Listener | Port | Notes |
|----------|----------|------|------|
| Controller | `CONTROLLER` | **9093** | **Not for producers/consumers** |
| Broker-1 (node.id=2) | `PLAINTEXT` | **9092** | No auth |
| Broker-1 (node.id=2) | `SASL_PLAINTEXT` | **9096** | SCRAM user/pass |
| Broker-2 (node.id=3) | `PLAINTEXT` | **9094** | No auth |
| Broker-2 (node.id=3) | `SASL_PLAINTEXT` | **9097** | SCRAM user/pass |
| Broker-3 (node.id=4) | `PLAINTEXT` | **9095** | No auth |
| Broker-3 (node.id=4) | `SASL_PLAINTEXT` | **9098** | SCRAM user/pass |

### Why you must enable SASL on *all* brokers in a 3-broker topic

Your topics use `replication-factor=3`. A partition leader can be on **any** broker.  
If only one broker had SASL enabled, a SCRAM client would authenticate to the bootstrap broker but fail when it is redirected to the partition leader (timeouts / “unexpected handshake” / “0 messages”).

We fixed this by enabling `SASL_PLAINTEXT` on **broker-2** and **broker-3** as well.

---

## Kafka 4.x note (important for commands)

Kafka 4.x prints warnings if you use old flags like `--producer.config` or `--consumer.config`.

- Use **`--command-config <properties-file>`** instead.
- On Windows **CMD**, if you split a command incorrectly you will see `More?` prompts. Easiest fix: copy/paste commands as **single lines**.

---

## Docker — when you need it

| Labs | Docker required? |
|------|------------------|
| 01–03, 07–08 | **No** — Kafka on host only |
| 04–06 | **Optional** — Prometheus, Grafana, kafka-lag-exporter |

Full table, `host.docker.internal` notes, and one-command stack: **[docker/README.md](docker/README.md)**.

```bat
cd c:\Users\om\Desktop\KafKa\Day-9\labs\docker
docker compose up -d
```

---

## Three ways to complete client labs (01 & 05)

| Track | Folder | Best for |
|-------|--------|----------|
| **Shell** | [scripts/](scripts/) + lab README | First time — see exactly what Kafka CLI does |
| **Java** | [java-security-lab/](java-security-lab/) | `kafka-clients`, Maven, same APIs as production apps |
| **Python** | [python-security-lab/](python-security-lab/) | Quick scripts with `kafka-python` |

Labs 02–03 and 07–08 are shell-only for broker/ACL/TLS/chaos. Labs 04–06 use shell + optional Docker for monitoring.

You can mix tracks (e.g. shell for Lab 01 broker setup, Java for produce).

---

## Tips for first-time learners

1. **Read the whole lab README once** before typing commands — you will know what “done” looks like.
2. **One terminal per role** — e.g. Terminal A = broker, Terminal B = producer, Terminal C = consumer.
3. **Copy paths carefully** — Windows uses backslashes; Kafka config paths must match where files actually are.
4. **Restart the broker** after changing `server.properties` or JAAS files.
5. **Check the Checkpoint section** at the end of each lab — tick every box before moving on.
6. If stuck, use [TROUBLESHOOTING.md](TROUBLESHOOTING.md) and each lab’s troubleshooting table.

---

## After you finish all labs

You should be able to:

- Authenticate clients with SASL/SCRAM
- Restrict topic access with ACLs
- Terminate TLS on the broker and clients
- Scrape metrics and build Grafana dashboards
- Alert on consumer lag
- Reason about broker failure and controlled chaos drills

Good luck — start with [Lab 01](lab-01-sasl-scram-authentication/README.md).
