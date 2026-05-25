# Day 9 — Kafka Security & Monitoring Labs

Hands-on labs from **Kafka_Security_Monitoring.pptx**. Labs build on each other; use a **dedicated test cluster**, not production.

| Lab | Folder | Topic | Time |
|-----|--------|-------|------|
| 01 | [lab-01-sasl-scram-authentication](lab-01-sasl-scram-authentication/README.md) | SASL/SCRAM | ~20 min |
| 02 | [lab-02-kafka-acls](lab-02-kafka-acls/README.md) | ACL grant & revoke | ~25 min |
| 03 | [lab-03-tls-sasl-end-to-end](lab-03-tls-sasl-end-to-end/README.md) | TLS + SASL | ~30 min |
| 04 | [lab-04-jmx-prometheus](lab-04-jmx-prometheus/README.md) | JMX exporter | ~25 min |
| 05 | [lab-05-consumer-lag-alerting](lab-05-consumer-lag-alerting/README.md) | Lag alerts | ~20 min |
| 06 | [lab-06-grafana-dashboard](lab-06-grafana-dashboard/README.md) | Grafana overview | ~30 min |
| 07 | [lab-07-broker-failure-drill](lab-07-broker-failure-drill/README.md) | Broker failure | ~30 min |
| 08 | [lab-08-chaos-runbook-drill](lab-08-chaos-runbook-drill/README.md) | Chaos & runbooks | ~40 min |

## Code

| Track | Path | Purpose |
|-------|------|---------|
| **Configs** | [configs](configs/) | SCRAM/TLS client + broker snippets |
| **Monitoring** | [monitoring](monitoring/) | Prometheus, alerts, JMX |
| **Shell** | [scripts](scripts/) | SCRAM user, ACLs, drill topic |
| **Python** | [python-security-lab](python-security-lab/) | SCRAM producer, lag check |

## Prerequisites

- Multi-broker Kafka (3 brokers recommended for Lab 07)
- Admin access to broker configs and `kafka-configs`, `kafka-acls`
- Optional: Prometheus, Grafana, kafka-lag-exporter
