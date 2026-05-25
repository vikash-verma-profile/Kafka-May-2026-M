# Day 10 — Production Deployment & Capstone Labs

Ten hands-on labs from **Kafka_cap.pptx** (as stated on slide 2).

| Lab | Folder | Topic | Time |
|-----|--------|-------|------|
| 01 | [lab-01-inspect-kafka-cluster](lab-01-inspect-kafka-cluster/README.md) | CLI cluster inspection | ~20 min |
| 02 | [lab-02-kubernetes-strimzi](lab-02-kubernetes-strimzi/README.md) | Strimzi on K8s | ~60 min |
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
| **Configs** | [configs](configs/) | Strimzi, MirrorMaker 2 |

## Capstone (slides 39–45)

After the labs, build the end-to-end system described in [capstone/README.md](capstone/README.md).

## Prerequisites

- `KAFKA_HOME` set; cluster reachable (local or K8s)
- Labs 2–4, 9: Kubernetes (`kubectl`, minikube/kind) optional but recommended
