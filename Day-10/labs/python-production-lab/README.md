# Day 10 Python Production Lab

Python equivalent of production/capstone scripts.

## Setup

```powershell
pip install -r requirements.txt
```

## Bootstrap server

| Cluster | `--bootstrap-server` |
|---------|----------------------|
| Local Kafka | `localhost:9092` |
| Strimzi on K8s (Lab 02) | `localhost:19092` *(with port-forward running)* |

Port-forward for K8s cluster:

```bash
kubectl port-forward svc/my-cluster-kafka-bootstrap 19092:9092 -n kafka
```

## Scripts

| Script | Lab |
|--------|-----|
| `lab01_inspect_cluster.py` | 01 — topics & groups |
| `lab03_baseline_throughput.py` | 03 — baseline perf |
| `lab04_tuned_throughput.py` | 04 — tuned perf |
| `lab07_consumer_lag.py` | 07 — lag report |
| `lab10_capacity_plan.py` | 10 — sizing calculator |
| `capstone_producer.py` | capstone |
| `capstone_stream.py` | capstone |

For JVM perf tests use [../scripts](../scripts/) (`kafka-producer-perf-test.bat`).

Example with Strimzi cluster:

```powershell
python lab01_inspect_cluster.py --bootstrap-server localhost:19092
python lab07_consumer_lag.py localhost:19092 order-processor
```
