# Docker map — Day 9 labs

**Docker is never required for Labs 01–03 or 07–08.** Kafka itself runs on the JVM from your `%KAFKA_HOME%` install.

Docker is an **optional shortcut** for the monitoring stack (Labs 04–06). You can always use native Windows/Linux installs instead.

---

## Quick reference

| Lab | Component | Docker required? | Image / compose service | Native alternative |
|-----|-----------|------------------|-------------------------|-------------------|
| 01–03 | Kafka broker + SCRAM/TLS | **No** | — | [my-config/](../my-config/) on host |
| 01, 05 | Java / Python clients | **No** | — | [java-security-lab](../java-security-lab/), [python-security-lab](../python-security-lab/) |
| **04** | **Prometheus** | **Optional** | `prometheus` in [docker-compose.yml](docker-compose.yml) | [prometheus.io download](https://prometheus.io/download/) |
| **04** | jmx_exporter | **No** | — | Java agent JAR on broker ([Lab 04](../lab-04-jmx-prometheus/README.md)) |
| **05** | **kafka-lag-exporter** | **Optional** | `kafka-lag-exporter` in compose | Run exporter JAR + `application.conf` |
| **06** | **Grafana** | **Optional** | `grafana` in compose | [grafana.com download](https://grafana.com/grafana/download/) |
| 07–08 | Chaos / broker drill | **No** | — | `taskkill`, firewall, or WSL `iptables` |

---

## One-command monitoring stack (Labs 04–06)

From this folder:

```bat
cd c:\Users\om\Desktop\KafKa\Day-9\labs\docker
docker compose up -d
```

| URL | Service |
|-----|---------|
| http://localhost:9090 | Prometheus |
| http://localhost:3000 | Grafana (login `admin` / `admin`) |
| http://localhost:9999/metrics | kafka-lag-exporter |

Stop:

```bat
docker compose down
```

---

## Prerequisites for Docker track

1. **Docker Desktop** installed and running (Windows).
2. **Kafka broker on the host** at `localhost:9092` (not inside compose — labs assume your course cluster).
3. **jmx_exporter on the broker** at host `localhost:7071` (Lab 04 — still attach agent to JVM; not in compose).

Verify Docker:

```bat
docker version
docker compose version
```

---

## Windows: reach host Kafka from containers

Containers cannot use `localhost:9092` to mean *your PC’s* Kafka — that points inside the container.

This repo’s compose uses:

```text
host.docker.internal:9092
```

for kafka-lag-exporter. If lag metrics are empty:

- Confirm Kafka listens on `0.0.0.0:9092` or `localhost:9092` on the host.
- In Docker Desktop → Settings → enable **Expose daemon on tcp://localhost:2375** if needed, and ensure `host.docker.internal` resolves (Docker Desktop 4+ on Windows usually does).

Prometheus scrapes **host** ports via `host.docker.internal`:

| Target | Host port |
|--------|-----------|
| jmx_exporter | 7071 |
| kafka-lag-exporter | 9999 |

---

## Per-lab: Docker vs no Docker

### Lab 04 — JMX + Prometheus

| Step | Docker? | What to do |
|------|---------|------------|
| Attach jmx_exporter to broker | No | `KAFKA_OPTS=-javaagent:...=7071:...` |
| Run Prometheus | **Optional** | `docker compose up prometheus` **or** install `prometheus.exe` |
| Scrape config | No | [monitoring/prometheus.yml](../monitoring/prometheus.yml) |

### Lab 05 — Consumer lag alerting

| Step | Docker? | What to do |
|------|---------|------------|
| kafka-lag-exporter | **Optional** | Compose service **or** `docker run` **or** JAR |
| Prometheus alert rules | No | [monitoring/alert-rules.yml](../monitoring/alert-rules.yml) |
| Lag check script | No | Java `Lab05LagCheck` or Python `lab05_lag_check.py` |

**Single-container lag exporter (without compose):**

```bat
docker run -d --name kafka-lag-exporter -p 9999:9999 ^
  -e KAFKA_LAG_EXPORTER_CLUSTERS_0_NAME=local ^
  -e KAFKA_LAG_EXPORTER_CLUSTERS_0_BOOTSTRAP_BROKERS=host.docker.internal:9092 ^
  seglo/kafka-lag-exporter:latest
```

### Lab 06 — Grafana

| Step | Docker? | What to do |
|------|---------|------------|
| Grafana UI | **Optional** | Compose **or** native install |
| Prometheus datasource | No | URL `http://localhost:9090` (or `http://prometheus:9090` only from inside Docker network) |

**Grafana in Docker + Prometheus in Docker:** use datasource URL `http://prometheus:9090`.

**Grafana native + Prometheus in Docker:** use `http://localhost:9090`.

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `Cannot connect to Docker daemon` | Start Docker Desktop |
| Prometheus target DOWN for 7071 | jmx agent not attached; broker not running |
| Lag exporter no metrics | Use `host.docker.internal:9092`; broker must be on host |
| Port already in use | Stop other Prometheus/Grafana or change ports in compose |
| Grafana login fails | Default `admin` / `admin`; change password on first login |

---

## File layout

```text
labs/docker/
  README.md           ← this map
  docker-compose.yml  ← optional Prometheus + Grafana + lag-exporter
```
