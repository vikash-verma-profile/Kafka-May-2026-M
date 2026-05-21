# Kafka Local 3-Broker Config -Explained & Changes

This folder (`kafka-local-3brokers/config/`) uses a **combined KRaft** design: each node is **both broker and controller**. There is **no** separate `controller.properties` file here.

Your **muti-broker / kafka-bin** lab uses a **different** design: one dedicated `controller.properties` plus three `broker-*.properties` files. Both are documented below.

---

## 1. Two ways to run 3 brokers on Windows

| | **This folder** (`kafka-local-3brokers`) | **kafka-bin `config\`** (muti-broker lab) |
|---|------------------------------------------|-------------------------------------------|
| Controller | Built into each `brokerN.properties` | Separate `controller.properties` |
| Broker role | `process.roles=broker,controller` | `process.roles=broker` only |
| Processes to start | **3** (broker1, broker2, broker3) | **4** (controller + 3 brokers) |
| Quorum config | `controller.quorum.voters=...` | Brokers use `controller.quorum.bootstrap.servers=localhost:9093` |
| Client ports | 9092, 9094, 9095 | 9092, 9094, 9095 |
| Format flag | `--initial-controllers` on all 3 | `--standalone` on controller, `--no-initial-controllers` on brokers |

---

## 2. `broker1.properties` -line-by-line

File: `kafka-local-3brokers/config/broker1.properties`

This file defines **node 1** of the cluster. It handles **client traffic** (port 9092) and **controller/quorum** work (port 9093) on the same JVM.

### Server basics

| Property | Value | Meaning |
|----------|--------|---------|
| `process.roles` | `broker,controller` | This JVM is a **combined** node: stores topic data **and** participates in KRaft metadata quorum. |
| `node.id` | `1` | Unique id for this node in the cluster (broker2=2, broker3=3). |
| `controller.quorum.voters` | `1@localhost:9093,2@localhost:9193,3@localhost:9293` | Static list of all 3 controller endpoints. Every combined node must use the **same** line. `1@localhost:9093` = node 1’s controller listener. |

**Why not `controller.quorum.bootstrap.servers`?**  
A 3-node static cluster uses **voters** (fixed membership). Bootstrap is typical for a single dynamic controller or adding nodes later.

### Network / listeners

| Property | Value | Meaning |
|----------|--------|---------|
| `listeners` | `PLAINTEXT://:9092,CONTROLLER://:9093` | **9092** = clients (producers/consumers). **9093** = KRaft controller protocol for this node. |
| `advertised.listeners` | `PLAINTEXT://localhost:9092` | Address clients receive in metadata (must be reachable from your PC). |
| `inter.broker.listener.name` | `PLAINTEXT` | Brokers replicate data over the PLAINTEXT listener. |
| `controller.listener.names` | `CONTROLLER` | Which listener name is used for KRaft. |
| `listener.security.protocol.map` | `CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT` | No TLS/SASL in this lab. |

**Controller role on broker1:** The `CONTROLLER://:9093` part is the “controller.properties equivalent” embedded in this file. You do **not** start a separate controller process for this lab.

### Storage

| Property | Value | Meaning |
|----------|--------|---------|
| `log.dirs` | `C:/kafka-data/multi-broker-1` | Topic data and local metadata for **node 1** only. Each broker has its own folder. |

**Changed from default:** Default `server.properties` used `/tmp/kraft-combined-logs` or a single path -we use a **dedicated Windows path per node**.

### Replication (multi-broker lab)

| Property | Value | Meaning |
|----------|--------|---------|
| `num.partitions` | `3` | Default partitions for new topics (lab uses 3). |
| `default.replication.factor` | `3` | Each topic partition has 3 copies (one per broker). |
| `min.insync.replicas` | `2` | At least 2 replicas must be in-sync before ack. |
| `offsets.topic.replication.factor` | `3` | Internal `__consumer_offsets` replicated across brokers. |
| `transaction.state.log.replication.factor` | `3` | Transaction log replicated. |
| `transaction.state.log.min.isr` | `2` | Min in-sync for transaction log. |

**Changed from default:** Single-broker `server.properties` uses replication factor **1**. For 3 brokers we raised RF to **3** so the lab can use `--replication-factor 3`.

### broker2 / broker3 (only differences)

| File | `node.id` | PLAINTEXT port | CONTROLLER port | `log.dirs` |
|------|-----------|----------------|-----------------|------------|
| `broker1.properties` | 1 | 9092 | 9093 | `multi-broker-1` |
| `broker2.properties` | 2 | 9094 | 9193 | `multi-broker-2` |
| `broker3.properties` | 3 | 9095 | 9293 | `multi-broker-3` |

Same `controller.quorum.voters` line on all three files.

**CLI bootstrap (clients):**

```text
localhost:9092,localhost:9094,localhost:9095
```

Do **not** use 9093, 9193, or 9293 in `--bootstrap-server` -those are controller ports.

---

## 3. `controller.properties` -separate-file model (kafka-bin)

There is **no** `controller.properties` under `kafka-local-3brokers/config/`.  
For the **muti-broker** lab, the controller lives here:

```text
C:\kafka-bin\kafka_2.13-4.2.0\config\controller.properties
```

### What that file does

| Property | Typical value | Meaning |
|----------|---------------|---------|
| `process.roles` | `controller` | **Only** metadata/quorum -no topic produce/consume on this JVM. |
| `node.id` | `1` | Controller node id (brokers use 2, 3, 4 in that lab). |
| `controller.quorum.bootstrap.servers` | `localhost:9093` | How this controller finds/joins the quorum (dynamic style for a single controller). |
| `listeners` | `CONTROLLER://:9093` | Listens **only** for KRaft -no PLAINTEXT client port. |
| `advertised.listeners` | `CONTROLLER://localhost:9093` | Address brokers use to reach the controller. |
| `log.dirs` | `C:/kafka-data/kraft-controller-logs` | Controller metadata only (not your topic messages). |

### broker-only file (e.g. `broker-1.properties` in kafka-bin)

| Property | Value | Meaning |
|----------|--------|---------|
| `process.roles` | `broker` | No controller work on this JVM. |
| `node.id` | `2` | Broker id (not 1 -id 1 is the controller). |
| `controller.quorum.bootstrap.servers` | `localhost:9093` | “Register with controller at 9093.” |
| `listeners` | `PLAINTEXT://localhost:9092` | Client port only. |
| `log.dirs` | `C:/kafka-data/kraft-broker-logs-1` | Topic data for broker 1. |

**Start order:** controller first → then brokers.

---

## 4. What we changed vs default Kafka `server.properties`

Default single-node file: `C:\kafka-bin\kafka_2.13-4.2.0\config\server.properties`

| Setting | Default (single node) | `kafka-local-3brokers` broker1 | Why |
|---------|----------------------|----------------------------------|-----|
| `process.roles` | `broker,controller` | Same | Still combined, but ×3 nodes |
| `node.id` | `1` | `1` (2 and 3 on other files) | One id per process |
| Quorum | `controller.quorum.bootstrap.servers=localhost:9093` | `controller.quorum.voters=1@...,2@...,3@...` | 3-node static cluster |
| `listeners` | `PLAINTEXT :9092`, `CONTROLLER :9093` | Same on broker1; **9094/9193**, **9095/9293** on others | Avoid port conflicts |
| `advertised.listeners` | `PLAINTEXT://localhost:9092` (+ controller) | PLAINTEXT only on advertised (per node) | Clients use broker ports only |
| `log.dirs` | `C:/kafka-data/kraft-combined-logs` (one dir) | `C:/kafka-data/multi-broker-1` (per node) | Isolated data per broker |
| `num.partitions` | `1` | `3` | Multi-partition lab |
| `default.replication.factor` | (not set / 1) | `3` | Replicate across 3 brokers |
| `offsets.topic.replication.factor` | `1` | `3` | Cluster-wide offsets |

### Other fixes required to run on your machine

| Issue | Fix |
|-------|-----|
| Empty `kafka-storage.bat` | Restored script under `bin\windows\` (was 0 bytes). |
| Kafka 4.2 format | Use `--initial-controllers` for 3 combined nodes (same voters line on all three). |
| Windows paths | `C:/kafka-data/...` instead of `/tmp/...`. |
| `kafka-storage.bat` for muti-broker split model | Controller: `--standalone`; brokers: `--no-initial-controllers`. |

---

## 5. How to format and start (`kafka-local-3brokers`)

### Format (once, from Kafka home)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
mkdir C:\kafka-data\multi-broker-1
mkdir C:\kafka-data\multi-broker-2
mkdir C:\kafka-data\multi-broker-3
bin\windows\kafka-storage.bat random-uuid
```

Use the UUID for all three (example uses `YOUR_CLUSTER_ID`):

```bat
bin\windows\kafka-storage.bat format -t YOUR_CLUSTER_ID -c C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-local-3brokers\config\broker1.properties --initial-controllers "1@localhost:9093:DIR1,2@localhost:9193:DIR2,3@localhost:9293:DIR3"
```

Generate `DIR1`, `DIR2`, `DIR3` with three `kafka-storage.bat random-uuid` calls. The **same** `--initial-controllers` string must be used for broker2 and broker3 format commands.

Or run: `kafka-local-3brokers\scripts\setup-cluster.bat` (generates IDs automatically).

### Start (3 terminals)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-server-start.bat C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-local-3brokers\config\broker1.properties
bin\windows\kafka-server-start.bat ...\config\broker2.properties
bin\windows\kafka-server-start.bat ...\config\broker3.properties
```

---

## 6. Diagram -combined mode (this folder)

```text
  Node 1 (broker1.properties)     Node 2 (broker2.properties)     Node 3 (broker3.properties)
  +------------------------+    +------------------------+    +------------------------+
  | broker + controller    |    | broker + controller    |    | broker + controller    |
  | clients :9092          |    | clients :9094          |    | clients :9095          |
  | KRaft   :9093          |    | KRaft   :9193          |    | KRaft   :9293          |
  +------------------------+    +------------------------+    +------------------------+
              \_________________________|_________________________/
                            one cluster (quorum voters)
```

## Diagram -split mode (kafka-bin `controller.properties` + brokers)

```text
  controller.properties          broker-1.properties    broker-2.properties    broker-3.properties
  +------------------+           +-------------+        +-------------+        +-------------+
  | controller only  |           | broker :9092|        | broker :9094|        | broker :9095|
  | KRaft :9093      |<----------| registers   |        | registers   |        | registers   |
  +------------------+           +-------------+        +-------------+        +-------------+
```

---

## 7. Which lab should you use?

| Use | When |
|-----|------|
| **`kafka-local-3brokers`** (this doc) | Learn 3 equal combined nodes; 3 terminals; `controller.quorum.voters`. |
| **`kafka-bin\config\` + muti-broker doc** | Matches official split broker/controller templates; 4 terminals; closer to production layout. |

CLI commands (topics, producer, consumer) are the same for both:

```bat
set BS=localhost:9092,localhost:9094,localhost:9095
```

---

## 8. Related docs

- Multi-broker CLI (kafka-bin): [../muti-broker/kafka-multi-broker-cli-lab.md](../muti-broker/kafka-multi-broker-cli-lab.md)
- Config verification (muti-broker): [../muti-broker/CONFIG-VERIFICATION.md](../muti-broker/CONFIG-VERIFICATION.md)
- Single broker setup: [../kafka-kraft-setup-windows.md](../kafka-kraft-setup-windows.md)
