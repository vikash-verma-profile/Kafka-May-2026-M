# Lab 02 — Deploy Kafka on Kubernetes with Strimzi

**Objective:** Install the Strimzi operator and deploy a 3-node Kafka cluster (KRaft) on Kubernetes.

From **Kafka_cap.pptx** — Slide 12.

**Estimated time:** **45–90 minutes** on first run (mostly image downloads). Repeat runs: **~15–20 minutes**.

---

## Time breakdown (first run)

| Step | What happens | Typical time |
|------|----------------|--------------|
| 1 | Create namespace | < 1 min |
| 2 | Install Strimzi operator + CRDs | 1–2 min to apply |
| 2 | Pull operator image (`quay.io/strimzi/operator:1.0.0`) | **3–6 min** |
| 2 | Operator becomes Ready | **~1 min** after image pull |
| 3 | Apply Kafka manifest | < 1 min |
| 4 | Provision 3 PVCs (10 Gi each) | **~1 min** |
| 4 | Pull Kafka image × 3 (`quay.io/strimzi/kafka:1.0.0-kafka-4.1.0`) | **6–15 min** |
| 4 | Kafka pods start + KRaft quorum forms | **2–5 min** |
| 4 | Entity operator starts | **2–5 min** |
| 5 | Port-forward + topic list test | **~2 min** |

**Total:** ~20–35 min if images are cached; **45–90 min** on a fresh Docker Desktop / kind cluster with no cached Strimzi images.

> **Tip:** Steps 2 and 4 look “stuck” in `ContainerCreating` while images download. That is normal — wait for the pull to finish before assuming something is wrong.

---

## Prerequisites

- `kubectl` with cluster-admin (Docker Desktop Kubernetes, minikube, kind, or cloud cluster)
- **≥ 6 GB RAM** allocated to Docker / your cluster (3 Kafka nodes + operator + entity operator)
- Stable internet for pulling images from `quay.io`
- [Strimzi 1.0 docs](https://strimzi.io/docs/operators/1.0.0/deploying.html)

### Strimzi 1.0 changes (important)

The install URL `https://strimzi.io/install/latest` currently deploys **Strimzi 1.0**, which:

- Uses API **`kafka.strimzi.io/v1`** — older lab YAML using `v1beta2` will fail with *“no matches for kind Kafka”*
- Runs Kafka in **KRaft mode only** (no ZooKeeper)
- Requires a **`KafkaNodePool`** resource in addition to the **`Kafka`** resource

This lab’s manifest uses one combined node pool (3 nodes, each acting as controller + broker).

### Windows note — port 9092 conflict

If you also run a **local Kafka broker** (Labs 01, 03–07), it already listens on `localhost:9092`. You cannot port-forward the K8s cluster to the same port.

**Use `19092` as the local port** when connecting to the Strimzi cluster (see Step 5). Later labs that say `localhost:9092` should use `localhost:19092` while the K8s port-forward is active.

Check what is using port 9092:

```powershell
netstat -ano | findstr ":9092"
Get-Process -Id <PID>
```

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

Wait until **both** checks pass before continuing:

```bash
kubectl get pods -n kafka
kubectl api-resources --api-group=kafka.strimzi.io
```

**Expected:**

```text
NAME                                       READY   STATUS    RESTARTS   AGE
strimzi-cluster-operator-xxxxxxxxxx-xxxxx  1/1     Running   0          Xm

NAME                 SHORTNAMES   APIVERSION            NAMESPACED   KIND
kafkas               k            kafka.strimzi.io/v1   true         Kafka
kafkanodepools       knp          kafka.strimzi.io/v1   true         KafkaNodePool
...
```

**Do not proceed to Step 3 until the operator is `1/1 Running`.** If you apply the Kafka cluster too early, you will see:

```text
no matches for kind "Kafka" in version "kafka.strimzi.io/v1beta2"
ensure CRDs are installed first
```

Fix: wait for Step 2 to complete, then use `create-kafka/kafka-persistent.yaml` (uses `v1` API).

Brief `Unhealthy` liveness/readiness warnings right after the operator starts are normal while it initializes.

---

## Step 3 — Apply Kafka cluster

```bash
cd lab-02-kubernetes-strimzi/create-kafka
kubectl apply -f kafka-persistent.yaml -n kafka
```

The manifest (`create-kafka/kafka-persistent.yaml`) defines:

- **`KafkaNodePool` `dual-role`** — 3 replicas, combined controller + broker roles, 10 Gi persistent storage each
- **`Kafka` `my-cluster`** — Kafka 4.1.0 (KRaft), plain listener on port 9092, topic/user operators enabled

Full manifest:

```yaml
apiVersion: kafka.strimzi.io/v1
kind: KafkaNodePool
metadata:
  name: dual-role
  labels:
    strimzi.io/cluster: my-cluster
spec:
  replicas: 3
  roles:
    - controller
    - broker
  storage:
    type: jbod
    volumes:
      - id: 0
        type: persistent-claim
        size: 10Gi
        deleteClaim: false
        kraftMetadata: shared
---
apiVersion: kafka.strimzi.io/v1
kind: Kafka
metadata:
  name: my-cluster
  namespace: kafka
spec:
  kafka:
    version: 4.1.0
    metadataVersion: 4.1-IV0
    listeners:
      - name: plain
        port: 9092
        type: internal
        tls: false
    config:
      offsets.topic.replication.factor: 3
      transaction.state.log.replication.factor: 3
      transaction.state.log.min.isr: 2
      default.replication.factor: 3
      min.insync.replicas: 2
  entityOperator:
    topicOperator: {}
    userOperator: {}
```

A copy also lives at [configs/kafka-persistent.yaml](../configs/kafka-persistent.yaml).

---

## Step 4 — Wait for readiness

Watch pod progress:

```bash
kubectl get pods -n kafka -w
```

**Normal startup sequence:**

| Phase | Pod status | Meaning |
|-------|------------|---------|
| Image pull | `ContainerCreating` | Downloading ~400 MB Kafka image per node |
| KRaft forming | `Running` but `0/1 Ready` | Broker started, readiness probe not passing yet |
| Brokers up | `Running` `1/1 Ready` | All 3 `my-cluster-dual-role-*` pods healthy |
| Entity operator | `Running` `0/2` → `2/2` | Topic/user operators starting (may restart once) |
| Cluster Ready | `kubectl get kafka` shows `READY: True` | Full stack healthy |

Wait for the cluster Ready condition (allow up to **15 minutes** on first run):

```bash
kubectl wait kafka/my-cluster --for=condition=Ready --timeout=900s -n kafka
```

**Expected:**

```text
kafka.kafka.strimzi.io/my-cluster condition met
```

**Expected pods when healthy:**

```text
NAME                                          READY   STATUS    RESTARTS   AGE
my-cluster-dual-role-0                        1/1     Running   0          Xm
my-cluster-dual-role-1                        1/1     Running   0          Xm
my-cluster-dual-role-2                        1/1     Running   0          Xm
my-cluster-entity-operator-xxxxxxxxxx-xxxxx   2/2     Running   0          Xm
strimzi-cluster-operator-xxxxxxxxxx-xxxxx     1/1     Running   0          Xm
```

Check cluster status:

```bash
kubectl get kafka -n kafka
kubectl get kafkanodepool -n kafka
```

You can test connectivity once all three broker pods are `1/1 Ready`, even if the entity operator is still catching up.

---

## Step 5 — Verify and connect

### Port-forward (leave this terminal open)

**Recommended on Windows** (avoids conflict with local Kafka on 9092):

```bash
kubectl port-forward svc/my-cluster-kafka-bootstrap 19092:9092 -n kafka
```

If port 9092 is free on your machine:

```bash
kubectl port-forward svc/my-cluster-kafka-bootstrap 9092:9092 -n kafka
```

If port-forward fails with *“bind: access permissions”* or *“unable to listen”*, another process owns that port — use `19092` instead.

### Test connectivity

In another terminal, list topics (requires `KAFKA_HOME`):

```bat
bin\windows\kafka-topics.bat --bootstrap-server localhost:19092 --list
```

Or with Python from this repo:

```bash
cd python-production-lab
python lab01_inspect_cluster.py --bootstrap-server localhost:19092
```

### Bootstrap server for later labs

| Cluster | Bootstrap server |
|---------|------------------|
| Local Kafka (default labs) | `localhost:9092` |
| Strimzi on K8s (this lab) | `localhost:19092` *(with port-forward running)* |

Keep the port-forward terminal open while running Labs 01, 03–07 against the K8s cluster.

---

## Checkpoint

- [ ] Strimzi operator pod `1/1 Running`
- [ ] `kubectl api-resources --api-group=kafka.strimzi.io` shows `Kafka` at `kafka.strimzi.io/v1`
- [ ] 3 `my-cluster-dual-role-*` pods `1/1 Running`
- [ ] Entity operator pod `2/2 Running`
- [ ] `kubectl get kafka my-cluster -n kafka` shows `READY: True`
- [ ] Port-forward active on `19092` (or `9092`)
- [ ] Can list topics via `--bootstrap-server localhost:19092`

---

## Troubleshooting

| Symptom | Cause | Fix |
|---------|--------|-----|
| `no matches for kind "Kafka" in version "kafka.strimzi.io/v1beta2"` | Old YAML or operator/CRDs not ready | Wait for operator Running; use `create-kafka/kafka-persistent.yaml` (`v1` API) |
| Operator stuck in `ContainerCreating` | Pulling ~230 MB operator image | Wait 3–6 min; check `kubectl describe pod -n kafka -l name=strimzi-cluster-operator` |
| Kafka pods stuck in `ContainerCreating` | Pulling Kafka image (~400 MB per node) | Wait 6–15 min; events show `Pulling image "quay.io/strimzi/kafka:..."` |
| Pods `Running` but `0/1 Ready` for several minutes | KRaft quorum still forming | Wait 2–5 min; check `kubectl logs my-cluster-dual-role-0 -n kafka` |
| Entity operator `0/2` with probe restarts | Operators still starting | Wait 2–5 min; should reach `2/2 Running` |
| `kubectl wait` times out | Image pulls or entity operator slow | Re-run with `--timeout=900s`; confirm all broker pods are `1/1 Ready` |
| Port-forward: `unable to listen on port 9092` | Local Kafka already on 9092 | Use `19092:9092` mapping; see Windows note above |
| PVC `Pending` | Storage class / provisioner missing | Ensure default StorageClass exists: `kubectl get sc` |

Useful debug commands:

```bash
kubectl get events -n kafka --sort-by='.lastTimestamp'
kubectl describe kafka my-cluster -n kafka
kubectl logs -n kafka deployment/strimzi-cluster-operator --tail=50
kubectl describe pod my-cluster-dual-role-0 -n kafka
```

---

## Cleanup

```bash
kubectl delete kafka my-cluster -n kafka
kubectl delete kafkanodepool dual-role -n kafka
kubectl delete namespace kafka
```

Deleting the namespace alone also removes all resources inside it.

---

## Alternatives (slide 13)

Confluent Operator, Helm (Bitnami), custom StatefulSets.
