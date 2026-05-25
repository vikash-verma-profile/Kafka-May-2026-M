# Lab 07-Broker Failure Drill

**Objective:** Kill a broker during continuous produce with `acks=all` and confirm no data loss.

From **Kafka_Security_Monitoring.pptx**-Slide 28. **Time:** ~30 min.

---

## Prerequisites

- **3-broker** cluster (`start-kafka-cluster.bat`)
- Topic with `replication-factor=3`, `min.insync.replicas=2`

---

## Step 1-Create resilient topic

```bat
bin\windows\kafka-topics.bat --create --topic drill-orders ^
  --bootstrap-server localhost:9092 ^
  --partitions 6 --replication-factor 3 ^
  --config min.insync.replicas=2
```

Verify:

```bat
bin\windows\kafka-topics.bat --describe --topic drill-orders --bootstrap-server localhost:9092
```

**Expected:** Each partition Leader + Replicas + ISR all three brokers.

---

## Step 2-Continuous producer (`acks=all`)

```properties
acks=all
enable.idempotence=true
bootstrap.servers=localhost:9092,localhost:9094,localhost:9095
```

Run perf test or custom producer loop for 5+ minutes.

```bat
bin\windows\kafka-producer-perf-test.bat --topic drill-orders ^
  --num-records 1000000 --record-size 256 --throughput 1000 ^
  --producer-props acks=all enable.idempotence=true bootstrap.servers=localhost:9092
```

---

## Step 3-Kill broker-2

Stop the broker-2 process (close its CMD window or `taskkill`).

Watch Grafana (Lab 06):

- `UnderReplicatedPartitions` may spike briefly
- Leader election on affected partitions

---

## Step 4-Verify producer

**Expected:**

- No sustained `NOT_ENOUGH_REPLICAS` if 2 of 3 brokers alive
- Perf test continues or briefly pauses then resumes

---

## Step 5-Restore broker

Restart broker-2. Monitor URP return to **0**.

```bat
bin\windows\kafka-topics.bat --describe --topic drill-orders --bootstrap-server localhost:9092
```

ISR should include all replicas again.

---

## Step 6-Consumer verification

Consume from beginning; count records matches producer send count (within idempotent semantics).

---

## Checkpoint

- [ ] RF=3, min.insync.replicas=2 configured
- [ ] Producer survived broker loss
- [ ] URP drained after broker return
- [ ] No message count gap

---

## Failure modes reference (slide 27)

| Symptom | Likely cause |
|---------|----------------|
| URP > 0 | Broker down or slow replica |
| Offline partitions | No ISR quorum |
| Produce errors with acks=all | ISR < min.insync.replicas |
