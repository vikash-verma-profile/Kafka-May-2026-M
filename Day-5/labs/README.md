# Day 5 - Kafka Storage Internals & Replication Labs

Hands-on labs from **Kafka_Storage_Internals_Replication.pptx**. Each folder has a step-by-step guide for Windows (CMD or PowerShell).

## Prerequisites

| Software | Version |
|----------|---------|
| Java JDK | 17+ |
| Apache Kafka | 3.x / 4.x (KRaft) |

**Single-broker labs (01, 02, 05, 06):** `localhost:9092`  
**Multi-broker labs (03, 04, 07):** `localhost:9092,localhost:9094,localhost:9095`

## Quick start

1. Complete **[lab-00-initial-setup](./lab-00-initial-setup/README.md)** — broker(s), `KAFKA_HOME`, log directories.
2. Run labs in order, or jump to the topic you need (see index below).
3. Use helper scripts in `scripts/` after setting `KAFKA_HOME`.

## Lab index

| Lab | Topic | Cluster |
|-----|--------|---------|
| [lab-00](./lab-00-initial-setup/README.md) | Initial setup | 1 or 3 brokers |
| [lab-01](./lab-01-inspect-log-files/README.md) | Inspect segment files (.log, .index, .timeindex) | Single |
| [lab-02](./lab-02-index-offset-lookup/README.md) | Index files and sparse offset lookup | Single |
| [lab-03](./lab-03-broker-failure-simulation/README.md) | Leader failover when a broker dies | 3 brokers |
| [lab-04](./lab-04-monitor-isr-leader-election/README.md) | ISR shrink/expand and under-replication | 3 brokers |
| [lab-05](./lab-05-configure-retention/README.md) | Time- and size-based retention | Single |
| [lab-06](./lab-06-enable-log-compaction/README.md) | `cleanup.policy=compact` and tombstones | Single |
| [lab-07](./lab-07-replication-monitoring/README.md) | URP, ISR metrics, replication lag | 3 brokers |

## Project layout

```text
Day-5/labs/
  README.md
  scripts/
  lab-00-initial-setup/
  lab-01-inspect-log-files/
  lab-02-index-offset-lookup/
  lab-03-broker-failure-simulation/
  lab-04-monitor-isr-leader-election/
  lab-05-configure-retention/
  lab-06-enable-log-compaction/
  lab-07-replication-monitoring/
```

## Related (earlier days)

- [KRaft single broker (Windows)](../../Day-2/Labs/kafka-kraft-setup-windows.md)
- [3-broker local cluster](../../Day-2/Labs/kafka-local-multi-broker-cli-lab.md)
- [Day 4 Consumer labs](../../Day-4/labs/README.md)
