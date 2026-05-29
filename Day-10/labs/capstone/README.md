# Final Capstone — Real-Time Streaming System

Build an enterprise-style pipeline applying concepts from Days 6–10.

From **Kafka_cap.pptx** — Slides 39–45.

---

## Architecture

```text
Producer App  →  Kafka Cluster  →  Kafka Streams  →  Sink (DB/ES)
                        ↓
                 Monitoring (Grafana)
```

---

## Components

### 1. Producer application (slide 41)

- Generate real-time events (orders, payments, or sensor data)
- Optimized producer config: `acks=all`, `compression.type=lz4`, `enable.idempotence=true`
- Avro + Schema Registry (Day 6)
- Retry and error handling

### 2. Kafka cluster (slide 40)

- Multi-broker (RF=3, `min.insync.replicas=2`)
- ACLs + TLS (Day 9) if demonstrating security
- Topics with appropriate partition counts (Lab 10 sizing)
- **Kubernetes option:** deploy with Strimzi ([Lab 02](../lab-02-kubernetes-strimzi/README.md)); connect via `kubectl port-forward svc/my-cluster-kafka-bootstrap 19092:9092 -n kafka`

### 3. Kafka Streams processing (slide 42)

- Filter invalid records
- Aggregations (region, product, time window)
- Windowed analytics (1-minute tumbling)
- Output to `processed-orders` topic

### 4. Database sink (slide 43)

- JDBC Sink or Elasticsearch Sink (Day 8)
- Upsert on primary key
- DLQ for poison messages

### 5. Monitoring dashboard (slide 44)

- JMX → Prometheus → Grafana
- Panels: throughput, consumer lag, URP, offline partitions
- Alerts on lag SLO breach

---

## Starter code

| Track | Location |
|-------|----------|
| **Java** | [java-capstone](java-capstone/) — `CapstoneProducer`, `CapstoneStreamsApp` |
| **Python** | [python-production-lab](../python-production-lab/) — `capstone_producer.py`, `capstone_stream.py` |

**Java:**

```bat
cd capstone\java-capstone
mvn -q compile
mvn -q exec:java -Dexec.mainClass=com.training.kafka.capstone.CapstoneProducer
mvn -q exec:java -Dexec.mainClass=com.training.kafka.capstone.CapstoneStreamsApp
```

**Python:**

```powershell
cd ..\python-production-lab
pip install -r requirements.txt
python capstone_producer.py
python capstone_stream.py
```

Create topics `capstone-orders` and `capstone-processed` before running.

---

## Deliverables checklist

| Deliverable | Status |
|-------------|--------|
| Working producer | ☐ |
| Running Kafka cluster | ☐ |
| Streams processing logic | ☐ |
| Database / ES integration | ☐ |
| Performance report (baseline vs tuned) | ☐ |
| Monitoring screenshots | ☐ |

---

## Recommended enhancements (slide 46)

- Schema Registry with BACKWARD compatibility
- Deploy on Kubernetes (Strimzi) — Lab 02
- CI/CD for connector and topic configs
- Broker failure drill — Day 9 Lab 07

---

## Suggested timeline

| Phase | Duration |
|-------|----------|
| Setup cluster + topics | 2 h |
| Producer + Schema Registry | 2 h |
| Streams + sink | 4 h |
| Monitoring + tuning | 2 h |
| Documentation + demo | 2 h |

---

## Evaluation criteria

1. End-to-end data flows without manual intervention
2. Invalid events quarantined or dropped with metrics
3. Aggregations correct under load
4. Dashboard reflects real cluster state
5. Written summary of design decisions (format choice, RF, partitions)
