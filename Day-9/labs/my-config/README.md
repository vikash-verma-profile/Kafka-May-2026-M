# My Kafka configs (KRaft controller + 3 brokers)

This folder contains the **exact working configs** used in Day 9 labs on Windows, using Kafka **4.2.0** in **KRaft** mode.

---

## Files

| File | Purpose |
|------|---------|
| `controller.properties` | KRaft controller (node.id=1) |
| `broker-1.properties` | Broker (node.id=2) |
| `broker-2.properties` | Broker (node.id=3) |
| `broker-3.properties` | Broker (node.id=4) |
| `kafka_server_jaas.conf` | JAAS file needed for SASL listeners |
| `client-scram-oneshot.properties` | Single-line client SCRAM config for Kafka CLI tools |

---

## Port map

| Component | Listener | Port |
|----------|----------|------|
| Controller | `CONTROLLER` | **9093** |
| Broker-1 | `PLAINTEXT` | **9092** |
| Broker-1 | `SASL_PLAINTEXT` | **9096** |
| Broker-2 | `PLAINTEXT` | **9094** |
| Broker-2 | `SASL_PLAINTEXT` | **9097** |
| Broker-3 | `PLAINTEXT` | **9095** |
| Broker-3 | `SASL_PLAINTEXT` | **9098** |

**Important:** `9093` is the **controller** port, not for producers/consumers.

---

## Why SASL is enabled on all brokers

Topics in these labs commonly use `replication-factor=3`. Any broker can become a partition leader.

If only one broker exposes SASL, a SCRAM client will:

1. authenticate to the bootstrap broker, then
2. get redirected to the leader on a different broker, and
3. fail with timeouts / “unexpected handshake” / “0 messages”.

So we enable `SASL_PLAINTEXT` on **broker-1**, **broker-2**, and **broker-3**.

---

## Start order (one terminal per process)

### 1) Start controller

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-server-start.bat c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\controller.properties
```

### 2) Start brokers (repeat in 3 new terminals)

**Broker-1**

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set KAFKA_OPTS=-Djava.security.auth.login.config=c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\kafka_server_jaas.conf
bin\windows\kafka-server-start.bat c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\broker-1.properties
```

**Broker-2**

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set KAFKA_OPTS=-Djava.security.auth.login.config=c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\kafka_server_jaas.conf
bin\windows\kafka-server-start.bat c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\broker-2.properties
```

**Broker-3**

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set KAFKA_OPTS=-Djava.security.auth.login.config=c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\kafka_server_jaas.conf
bin\windows\kafka-server-start.bat c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\broker-3.properties
```

> If you use PowerShell, use `$env:KAFKA_OPTS=...` instead of `set ...`.

---

## One-shot SCRAM commands (copy/paste safe)

### Set/reset SCRAM password (PLAINTEXT port)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-configs.bat --bootstrap-server localhost:9092 --alter --add-config "SCRAM-SHA-512=[password=secret]" --entity-type users --entity-name alice
```

### Create topic (SCRAM)

```bat
bin\windows\kafka-topics.bat --create --topic orders --partitions 3 --replication-factor 3 --bootstrap-server localhost:9096 --command-config c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\client-scram-oneshot.properties
```

### Produce (SCRAM)

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9096,localhost:9097,localhost:9098 --command-config c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\client-scram-oneshot.properties --topic orders
```

### Consume (SCRAM)

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9096,localhost:9097,localhost:9098 --command-config c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\client-scram-oneshot.properties --topic orders --from-beginning --group test-read-once --max-messages 10 --timeout-ms 10000
```

---

## Kafka 4.x flags note

Kafka 4.x warns that these are deprecated:

- `--producer.config`
- `--consumer.config`

Use **`--command-config`** instead.

---

## What was fixed in these configs

| Issue | Fix applied |
|-------|-------------|
| Only broker-1 had SASL | Added `SASL_PLAINTEXT` on broker-2 (**9097**) and broker-3 (**9098**) |
| `advertised.listeners` missing host | Set to `localhost:9096` etc. (not `://:9096`) |
| Clients could not read messages (RF=3) | All brokers now accept SCRAM so any partition leader works |
| Kafka 4.x CLI warnings | Use `client-scram-oneshot.properties` + `--command-config` |
| JAAS not applied on start | Documented `KAFKA_OPTS` before each `kafka-server-start` |

More errors: [TROUBLESHOOTING.md](../TROUBLESHOOTING.md)

---

## Node IDs (do not confuse with broker numbers)

| File | `node.id` | SASL port |
|------|-----------|-----------|
| `controller.properties` | 1 | — (9093 controller) |
| `broker-1.properties` | 2 | 9096 |
| `broker-2.properties` | 3 | 9097 |
| `broker-3.properties` | 4 | 9098 |
