# Day 10 — Production Deployment & Capstone Labs

Ten hands-on labs from **Kafka_cap.pptx** (as stated on slide 2).

| Lab | Folder | Topic | Time |
|-----|--------|-------|------|
| 01 | [lab-01-inspect-kafka-cluster](lab-01-inspect-kafka-cluster/README.md) | CLI cluster inspection | ~20 min |
| 02 | [lab-02-kubernetes-strimzi](lab-02-kubernetes-strimzi/README.md) | Strimzi on K8s | ~45–90 min (first run) |
| 03 | [lab-03-baseline-throughput](lab-03-baseline-throughput/README.md) | Perf baseline | ~30 min |
| 04 | [lab-04-tune-producer-throughput](lab-04-tune-producer-throughput/README.md) | Producer tuning | ~30 min |
| 05 | [lab-05-tune-consumer-fetch](lab-05-tune-consumer-fetch/README.md) | Consumer tuning | ~30 min |
| 06 | [lab-06-log-segment-retention](lab-06-log-segment-retention/README.md) | Segment & retention | ~25 min |
| 07 | [lab-07-diagnose-consumer-lag](lab-07-diagnose-consumer-lag/README.md) | Lag diagnosis | ~30 min |
| 08 | [lab-08-jmx-prometheus](lab-08-jmx-prometheus/README.md) | JMX + Prometheus | ~30 min |
| 09 | [lab-09-mirrormaker2-replication](lab-09-mirrormaker2-replication/README.md) | MirrorMaker 2 | ~45 min |
| 10 | [lab-10-capacity-planning](lab-10-capacity-planning/README.md) | Cluster sizing | ~30 min |

## Code

| Track | Path | Purpose |
|-------|------|---------|
| **Java** | [capstone/java-capstone](capstone/java-capstone/) | Capstone starter |
| **Python** | [python-production-lab](python-production-lab/) | Inspect, perf, lag, capstone |
| **Shell** | [scripts](scripts/) | CLI perf tests, retention, offsets |
| **Configs** | [configs](configs/) | Strimzi 1.0 KRaft manifest, MirrorMaker 2 |

## Kafka cluster options

Most labs assume a **local Kafka broker** on `localhost:9092`. Lab 02 deploys Kafka on **Kubernetes with Strimzi**.

| Setup | Bootstrap server | When to use |
|-------|------------------|-------------|
| Local broker | `localhost:9092` | Labs 01, 03–10 (default) |
| Strimzi on K8s (Lab 02) | `localhost:19092` | After port-forward (see below) |

### Connecting to the Strimzi cluster (Lab 02)

After Lab 02 completes, keep this running in a terminal:

```bash
kubectl port-forward svc/my-cluster-kafka-bootstrap 19092:9092 -n kafka
```

Use `localhost:19092` instead of `localhost:9092` in all later lab commands while the port-forward is active.

> **Why 19092?** On Windows, a local Kafka broker often already occupies port 9092. Port `19092` avoids the conflict. See [lab-02 README](lab-02-kubernetes-strimzi/README.md) for details.

### Strimzi manifest (Lab 02)

The Kafka cluster YAML uses **Strimzi 1.0** (`kafka.strimzi.io/v1`, KRaft, `KafkaNodePool`):

- Lab copy: [lab-02-kubernetes-strimzi/create-kafka/kafka-persistent.yaml](lab-02-kubernetes-strimzi/create-kafka/kafka-persistent.yaml)
- Shared copy: [configs/kafka-persistent.yaml](configs/kafka-persistent.yaml)

Do **not** use older `v1beta2` manifests — Strimzi 1.0 no longer supports them.

## Capstone (slides 39–45)

After the labs, build the end-to-end system described in [capstone/README.md](capstone/README.md).

## Prerequisites

- `KAFKA_HOME` set; cluster reachable (local or K8s)
- **Lab 02:** Docker Desktop Kubernetes (or minikube/kind), `kubectl`, ≥ 6 GB RAM for Docker
- **Labs 2–4, 9:** Kubernetes optional but recommended for production-style deployment
