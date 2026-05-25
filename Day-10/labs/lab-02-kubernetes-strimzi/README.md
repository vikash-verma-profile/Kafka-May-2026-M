# Lab 02 — Deploy Kafka on Kubernetes with Strimzi

**Objective:** Install the Strimzi operator and deploy a 3-broker Kafka cluster on Kubernetes.

From **Kafka_cap.pptx** — Slide 12.

---

## Prerequisites

- `kubectl` with cluster-admin (minikube, kind, or cloud cluster)
- Nodes with **≥ 4 GB RAM** free for brokers + KRaft/ZooKeeper
- [Strimzi docs](https://strimzi.io/docs/operators/latest/deploying.html)

---

## Step 1 — Create namespace

```bash
kubectl create namespace kafka
```

---

## Step 2 — Install Strimzi operator

```bash
kubectl apply -f https://strimzi.io/install/latest?namespace=kafka -n kafka
```

Wait for operator pod Running:

```bash
kubectl get pods -n kafka
```

---

## Step 3 — Kafka cluster CR

Create `kafka-persistent.yaml`:

```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: Kafka
metadata:
  name: my-cluster
  namespace: kafka
spec:
  kafka:
    version: 3.7.0
    replicas: 3
    listeners:
      - name: plain
        port: 9092
        type: internal
        tls: false
    storage:
      type: persistent-claim
      size: 10Gi
      deleteClaim: false
  entityOperator:
    topicOperator: {}
    userOperator: {}
```

Apply:

```bash
kubectl apply -f kafka-persistent.yaml -n kafka
```

---

## Step 4 — Wait for readiness

```bash
kubectl wait kafka/my-cluster --for=condition=Ready --timeout=300s -n kafka
```

**Expected:**

```text
kafka.kafka.strimzi.io/my-cluster condition met
```

---

## Step 5 — Verify pods and bootstrap

```bash
kubectl get pods -n kafka
kubectl get kafka -n kafka
```

Port-forward for local clients:

```bash
kubectl port-forward svc/my-cluster-kafka-bootstrap 9092:9092 -n kafka
```

Test:

```bat
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list
```

---

## Checkpoint

- [ ] Strimzi operator Running
- [ ] 3 Kafka pods Ready
- [ ] `my-cluster` condition Ready
- [ ] Can list topics via port-forward

---

## Cleanup

```bash
kubectl delete kafka my-cluster -n kafka
kubectl delete namespace kafka
```

---

## Alternatives (slide 13)

Confluent Operator, Helm (Bitnami), custom StatefulSets.
