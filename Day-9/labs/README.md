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
| **Apache Kafka** (3.x, KRaft mode) | Runs the brokers and CLI tools | `dir %KAFKA_HOME%\bin\windows` shows `.bat` files |
| **Java JDK 11+** | Kafka runs on the JVM | `java -version` |
| **Maven 3.8+** (optional) | Java lab track | `mvn -version` |
| **Python 3.9+** (optional) | Python lab track | `python --version` |
| **Docker Desktop** (optional, Labs 04–06) | Prometheus, Grafana, lag-exporter | `docker version` — see [docker/README.md](docker/README.md) |
| **Prometheus + Grafana** (Labs 04–06) | Monitoring stack | Native install **or** Docker compose |
| **Git Bash or PowerShell** | Run commands | You are reading this — good |

### 2. Set environment variables (Windows)

Open **PowerShell** or **Command Prompt** and run (adjust the path to where you unpacked Kafka):

```powershell
# Example — change to YOUR Kafka folder
$env:KAFKA_HOME = "C:\kafka\kafka_2.13-3.6.0"
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

### 4. Start a test Kafka cluster

- **Labs 01–06:** A **single broker** on `localhost:9092` is enough.
- **Labs 07–08:** You need **three brokers** (e.g. ports 9092, 9094, 9095). Use your course `start-kafka-cluster.bat` if provided.

**What success looks like:** No errors when you run:

```bat
%KAFKA_HOME%\bin\windows\kafka-broker-api-versions.bat --bootstrap-server localhost:9092
```

You should see API version output, not `Connection refused`.

---

## Repo layout (where files live)

| Path | Purpose |
|------|---------|
| [configs/](configs/) | Ready-made client properties (SCRAM, TLS templates) |
| [monitoring/](monitoring/) | Prometheus scrape config and alert rules |
| [scripts/](scripts/) | Helper `.bat` files (create user, grant ACLs) |
| [java-security-lab/](java-security-lab/) | Java (`kafka-clients`) producer and lag checker |
| [python-security-lab/](python-security-lab/) | Python (`kafka-python`) producer and lag checker |
| [docker/](docker/) | **Docker map** + optional compose for Labs 04–06 |

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
6. If stuck, use the **Troubleshooting** table in each lab README.

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
