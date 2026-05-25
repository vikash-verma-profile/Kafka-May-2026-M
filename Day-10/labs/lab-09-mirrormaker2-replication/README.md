# Lab 09 — Replicate with MirrorMaker 2

**Objective:** Replicate topic `orders` from cluster A to cluster B using MirrorMaker 2.

From **Kafka_cap.pptx** — Slide 34.

---

## Prerequisites

- **Two** Kafka clusters (different ports or hosts)
  - Cluster A: `localhost:9092`
  - Cluster B: `localhost:9096` (second KRaft/broker install or Docker)
- `connect-mirror-maker.sh` / `.bat` available

---

## Step 1 — Create topic on cluster A

```bat
bin\windows\kafka-topics.bat --create --topic orders ^
  --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

Produce sample messages.

---

## Step 2 — MM2 properties (`mm2.properties`)

```properties
clusters = A, B
A.bootstrap.servers = localhost:9092
B.bootstrap.servers = localhost:9096

A->B.enabled = true
A->B.topics = orders

replication.factor = 1
```

---

## Step 3 — Start MirrorMaker 2

```bat
bin\windows\connect-mirror-maker.bat mm2.properties
```

Wait for internal topics and mirror consumer groups to stabilize.

---

## Step 4 — Consume on cluster B

MM2 typically creates **`A.orders`** on cluster B (naming depends on config):

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9096 ^
  --topic A.orders --from-beginning
```

**Expected:** Same records as cluster A; lag under a few seconds under load.

---

## Step 5 — Verify failover readiness

Document:

- How to point consumers to cluster B during DR
- Expected replication lag metric

---

## Checkpoint

- [ ] MM2 process running without ERROR in logs
- [ ] Mirrored topic visible on cluster B
- [ ] Record count matches source

---

## DR context (slide 33)

MirrorMaker 2, cluster linking, backup of critical topics, tested failover runbooks.
