# Lab 02 — Deploy Kafka on Kubernetes with Strimzi

**Objective:** Install the Strimzi operator and deploy a 3-node Kafka cluster (KRaft) on Kubernetes, then connect from your Windows host.

From **Kafka_cap.pptx** — Slide 12.

**Estimated time:** **45–90 minutes** on first run (mostly image downloads). Repeat runs: **~15–20 minutes**.

---

## Lab layout

```text
lab-02-kubernetes-strimzi/
├── README.md                          ← this file
└── create-kafka/
    ├── README.md                      ← manifest reference
    └── kafka-persistent.yaml          ← apply this to create the cluster
```

Helper script (repo root): [scripts/start-strimzi-port-forwards.bat](../scripts/start-strimzi-port-forwards.bat)

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
| 5 | Port-forwards + topic list test | **~2 min** |

**Total:** ~20–35 min if images are cached; **45–90 min** on a fresh Docker Desktop / kind cluster.

> **Tip:** `ContainerCreating` for several minutes while images download is normal. Do not apply the Kafka CR until the operator pod is `1/1 Running`.

---

## Prerequisites

| Requirement | Details |
|-------------|---------|
| Kubernetes | Docker Desktop Kubernetes, minikube, or kind |
| `kubectl` | Cluster-admin access |
| RAM | **≥ 6 GB** for Docker / cluster (3 brokers + operators) |
| Network | Pull images from `quay.io` |
| Kafka CLI (optional) | e.g. `C:\kafka-bin\kafka_2.13-4.2.0` — **not** in this lab folder |
| Docs | [Strimzi 1.0 deploying guide](https://strimzi.io/docs/operators/1.0.0/deploying.html) |

### Strimzi 1.0 (important)

`https://strimzi.io/install/latest` installs **Strimzi 1.0**, which:

| Old (slides / older labs) | Current (this lab) |
|---------------------------|-------------------|
| `kafka.strimzi.io/v1beta2` | `kafka.strimzi.io/v1` |
| ZooKeeper + `Kafka` only | KRaft + `KafkaNodePool` + `Kafka` |
| `replicas` + `storage` on `Kafka` | Storage on `KafkaNodePool` |

Applying old YAML gives:

```text
no matches for kind "Kafka" in version "kafka.strimzi.io/v1beta2"
ensure CRDs are installed first
```

**Fix:** Wait for the operator, then use `create-kafka/kafka-persistent.yaml`.

### Windows / Docker Desktop

| Port | Used by |
|------|---------|
| `9092` | Your **local** Kafka broker (Labs 01, 03–07) — do not use for Strimzi port-forward |
| `30094` | Strimzi **bootstrap** (after port-forwards) |
| `30095`–`30097` | Strimzi **brokers** 0–2 (after port-forwards) |

NodePorts are **not** reachable on `localhost` from Docker Desktop on Windows. Use [start-strimzi-port-forwards.bat](../scripts/start-strimzi-port-forwards.bat) (four `kubectl port-forward` sessions).

---

## Architecture

```text
  Your PC (Windows)
       │
       │  localhost:30094  (bootstrap)
       │  localhost:30095–30097  (brokers 0–2)
       │  via kubectl port-forward × 4
       ▼
  ┌─────────────────────────────────────────────┐
  │  namespace: kafka                           │
  │  ┌─────────────────────────────────────┐  │
  │  │ Strimzi Cluster Operator            │  │
  │  └─────────────────────────────────────┘  │
  │  ┌──────────┐ ┌──────────┐ ┌──────────┐   │
  │  │ dual-    │ │ dual-    │ │ dual-    │   │
  │  │ role-0   │ │ role-1   │ │ role-2   │   │
  │  │ KRaft    │ │ KRaft    │ │ KRaft    │   │
  │  │ ctrl+brk │ │ ctrl+brk │ │ ctrl+brk │   │
  │  └──────────┘ └──────────┘ └──────────┘   │
  │  ┌─────────────────────────────────────┐  │
  │  │ Entity Operator (topic + user)      │  │
  │  └─────────────────────────────────────┘  │
  └─────────────────────────────────────────────┘
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

Wait until **both** pass:

```bash
kubectl get pods -n kafka
kubectl api-resources --api-group=kafka.strimzi.io
```

**Expected:**

```text
strimzi-cluster-operator-xxxxxxxxxx-xxxxx   1/1   Running

kafkas               k   kafka.strimzi.io/v1   true   Kafka
kafkanodepools       knp kafka.strimzi.io/v1   true   KafkaNodePool
```

Brief `Unhealthy` probe warnings right after start are normal (~1 min).

---

## Step 3 — Apply Kafka cluster

```bash
cd C:\Users\om\Desktop\KafKa\Day-10\labs\lab-02-kubernetes-strimzi\create-kafka
kubectl apply -f kafka-persistent.yaml -n kafka
```

### What the manifest creates

| Resource | Name | Purpose |
|----------|------|---------|
| `KafkaNodePool` | `dual-role` | 3 nodes: controller + broker, 10 Gi PVC each |
| `Kafka` | `my-cluster` | Kafka 4.1.0 (KRaft), listeners, entity operator |

### Listeners

| Listener | Type | Port | Access from PC |
|----------|------|------|----------------|
| `plain` | internal | 9092 | In-cluster only (`kubectl exec`) |
| `external` | nodeport | 9094 | `localhost:30094` (bootstrap) via port-forward |

Broker advertised addresses (for clients): `localhost:30095`, `30096`, `30097`.

Full YAML: [create-kafka/kafka-persistent.yaml](create-kafka/kafka-persistent.yaml) and [create-kafka/README.md](create-kafka/README.md).

---

## Step 4 — Wait for readiness

```bash
kubectl get pods -n kafka -w
```

| Phase | What you see |
|-------|----------------|
| Image pull | `ContainerCreating` (~6–15 min first run) |
| KRaft | `Running` but `0/1 Ready` |
| Brokers | 3× `my-cluster-dual-role-*` at `1/1 Running` |
| Entity operator | `my-cluster-entity-operator-*` → `2/2 Running` |
| Cluster | `kubectl get kafka` → `READY: True` |

```bash
kubectl wait kafka/my-cluster --for=condition=Ready --timeout=900s -n kafka
kubectl get kafka,kafkanodepool -n kafka
```

**Expected pods:**

```text
my-cluster-dual-role-0                        1/1   Running
my-cluster-dual-role-1                        1/1   Running
my-cluster-dual-role-2                        1/1   Running
my-cluster-entity-operator-xxxxxxxxxx-xxxxx   2/2   Running
strimzi-cluster-operator-xxxxxxxxxx-xxxxx     1/1   Running
```

**Services (after external listener is applied):**

```text
my-cluster-kafka-external-bootstrap   NodePort   ...:30094
my-cluster-dual-role-0                NodePort   ...:30095
my-cluster-dual-role-1                NodePort   ...:30096
my-cluster-dual-role-2                NodePort   ...:30097
```

---

## Step 5 — Verify and connect

### Option A — Quick verify (no `KAFKA_HOME` needed)

```bash
kubectl exec my-cluster-dual-role-0 -n kafka -c kafka -- bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

Empty output = success (no topics yet).

### Option B — Kafka CLI from Windows

**Do not** run `bin\windows\kafka-topics.bat` from `create-kafka\` — that folder has **only YAML**, no Kafka binaries.

**Terminal 1** — start port-forwards (four windows stay open):

```bat
cd C:\Users\om\Desktop\KafKa\Day-10\labs\scripts
start-strimzi-port-forwards.bat
```

Manual equivalent:

```bash
kubectl port-forward svc/my-cluster-kafka-external-bootstrap 30094:9094 -n kafka
kubectl port-forward svc/my-cluster-dual-role-0 30095:9094 -n kafka
kubectl port-forward svc/my-cluster-dual-role-1 30096:9094 -n kafka
kubectl port-forward svc/my-cluster-dual-role-2 30097:9094 -n kafka
```

**Terminal 2** — list topics:

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-topics.bat --bootstrap-server localhost:30094 --list
```

Or:

```bat
cd %KAFKA_HOME%
bin\windows\kafka-topics.bat --bootstrap-server localhost:30094 --list
```

**Python:**

```bash
cd C:\Users\om\Desktop\KafKa\Day-10\labs\python-production-lab
python lab01_inspect_cluster.py --bootstrap-server localhost:30094
```

### Bootstrap server for later labs

| Cluster | `--bootstrap-server` | Before running commands |
|---------|----------------------|-------------------------|
| Local Kafka | `localhost:9092` | Start local broker |
| Strimzi (this lab) | `localhost:30094` | Run `start-strimzi-port-forwards.bat` |

---

## Checkpoint

- [ ] Operator `1/1 Running`; API shows `kafka.strimzi.io/v1`
- [ ] `kubectl apply -f kafka-persistent.yaml` succeeded
- [ ] 3× `my-cluster-dual-role-*` pods `1/1 Running`
- [ ] Entity operator `2/2 Running`
- [ ] `kubectl get kafka my-cluster -n kafka` → `READY: True`
- [ ] Option A or B: topic list works (empty list is OK)
- [ ] Know bootstrap for later labs: `localhost:30094` + port-forwards

---

## Troubleshooting

| Symptom | Cause | Fix |
|---------|--------|-----|
| `no matches for kind "Kafka" in version "kafka.strimzi.io/v1beta2"` | Old YAML or CRDs not ready | Wait for operator; use `create-kafka/kafka-persistent.yaml` |
| Operator `ContainerCreating` long time | Pulling ~230 MB image | Wait 3–6 min |
| Kafka pods `ContainerCreating` | Pulling ~400 MB image × 3 | Wait 6–15 min |
| `Running` but `0/1 Ready` | KRaft still forming | Wait 2–5 min |
| Entity operator `0/2`, restarts | Still starting | Wait; target `2/2 Running` |
| `kubectl wait` timeout | Slow first pull | `--timeout=900s`; check brokers `1/1` |
| `unable to listen on port 9092` | Local Kafka on 9092 | Use `30094` + port-forward script |
| `The system cannot find the path specified` | Ran CLI from `create-kafka\` | `cd` to `KAFKA_HOME` or `C:\kafka-bin\kafka_2.13-4.2.0` |
| `Timed out waiting for a node assignment` | Single forward to internal bootstrap (`19092:9092`) | Use Option B (4 forwards) or Option A (`kubectl exec`) |
| Port-forward `lost connection to pod` | Rolling update replaced broker | Restart `start-strimzi-port-forwards.bat` |
| NodePort test fails on `localhost:30094` | Docker Desktop limitation | Use port-forwards, not raw NodePort |

**Debug commands:**

```bash
kubectl get events -n kafka --sort-by='.lastTimestamp'
kubectl describe kafka my-cluster -n kafka
kubectl get svc -n kafka
kubectl logs -n kafka deployment/strimzi-cluster-operator --tail=50
kubectl exec my-cluster-dual-role-0 -n kafka -c kafka -- grep advertised.listeners /tmp/strimzi.properties
```

---

## Cleanup

```bash
kubectl delete kafka my-cluster -n kafka
kubectl delete kafkanodepool dual-role -n kafka
kubectl delete namespace kafka
```

Or delete the namespace only (removes everything in `kafka`).

Close the four port-forward windows if still open.

---

## Alternatives (slide 13)

Confluent Operator, Helm (Bitnami), custom StatefulSets.
