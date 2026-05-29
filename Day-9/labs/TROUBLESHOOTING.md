# Day 9 Labs — Troubleshooting Guide

Common errors seen during these labs and how to fix them. Based on a **KRaft controller + 3 brokers** setup on Windows with Kafka **4.2.0**.

---

## Quick reference: which port for what?

| Port | Use for |
|------|---------|
| **9093** | Controller only — **never** for producer/consumer/SCRAM client |
| **9092, 9094, 9095** | PLAINTEXT (no SCRAM config) |
| **9096, 9097, 9098** | SASL_PLAINTEXT + SCRAM (`--command-config`) |

**SCRAM bootstrap (always use all three SASL ports):**

```text
localhost:9096,localhost:9097,localhost:9098
```

---

## Error: `Unexpected handshake request with client mechanism SCRAM-SHA-512, enabled mechanisms are []`

**Cause:** You connected with SCRAM config to a **PLAINTEXT** port (9092) or to the **controller** port (9093).

**Fix:**

- Use SASL ports **9096/9097/9098** with `--command-config`.
- Do **not** use `localhost:9092,localhost:9096` in one command — pick one security mode per bootstrap list.

---

## Error: `Authentication failed` / `invalid credentials with SASL mechanism SCRAM-SHA-512`

**Cause:** Wrong username/password, or SCRAM user not created on the cluster.

**Fix:**

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-configs.bat --bootstrap-server localhost:9092 --alter --add-config "SCRAM-SHA-512=[password=secret]" --entity-type users --entity-name alice
```

Match [configs/client-scram.properties](configs/client-scram.properties) or [my-config/client-scram-oneshot.properties](my-config/client-scram-oneshot.properties).

---

## Error: `Processed a total of 0 messages` / producer `TimeoutException`

**Cause:** Only **one** broker had SASL enabled, but topic has `replication-factor=3`. Client authenticates to bootstrap broker, then fails talking to partition leader on another broker (PLAINTEXT only).

**Fix:** Enable `SASL_PLAINTEXT` on **all three brokers** — see [my-config/broker-1.properties](my-config/broker-1.properties), [broker-2](my-config/broker-2.properties), [broker-3](my-config/broker-3.properties). Full steps: [my-config/README.md](my-config/README.md).

---

## Error: `Could not find a 'KafkaServer' ... System property 'java.security.auth.login.config' is not set`

**Cause:** Broker has SASL listener but `KAFKA_OPTS` was not set before `kafka-server-start`, or `set KAFKA_OPTS=...` did not apply (common in PowerShell vs CMD).

**Fix (CMD — same window as start):**

```bat
set KAFKA_OPTS=-Djava.security.auth.login.config=c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\kafka_server_jaas.conf
bin\windows\kafka-server-start.bat c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\broker-1.properties
```

**Fix (one-liner):**

```bat
cmd /c "set KAFKA_OPTS=-Djava.security.auth.login.config=c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\kafka_server_jaas.conf && bin\windows\kafka-server-start.bat c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\broker-1.properties"
```

---

## Error: CMD shows `More?` when pasting a command

**Cause:** Line continuation character `^` at end of line without a following line, or broken paste.

**Fix:** Paste the **entire command on one line**, or use [my-config/README.md](my-config/README.md) copy/paste commands.

---

## Warning: `Option --producer.config is deprecated` (Kafka 4.x)

**Fix:** Use `--command-config path\to\file.properties` instead of `--producer.config` or `--consumer.config`.

---

## Warning: `Reconfiguration failed: No configuration found for '...'`

Usually a **log4j2** warning on the CLI — safe to ignore if the command completes (`Completed updating config`, messages produced/consumed).

---

## Error: `advertised.listeners=SASL_PLAINTEXT://:9096` (empty host)

**Cause:** Clients receive bad broker addresses from metadata.

**Fix:** Always include hostname:

```properties
advertised.listeners=SASL_PLAINTEXT://localhost:9096,PLAINTEXT://localhost:9092
```

---

## Verify cluster is healthy

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-broker-api-versions.bat --bootstrap-server localhost:9092
bin\windows\kafka-broker-api-versions.bat --bootstrap-server localhost:9096 --command-config c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\client-scram-oneshot.properties
bin\windows\kafka-topics.bat --describe --topic orders --bootstrap-server localhost:9096 --command-config c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\client-scram-oneshot.properties
```

---

## Still stuck?

1. Read [my-config/README.md](my-config/README.md) — start order and verified commands.
2. Restart: controller → broker-1 → broker-2 → broker-3 (four terminals).
3. Re-run SCRAM user create on **9092** (PLAINTEXT).
4. Produce/consume with **all SASL ports** in bootstrap.
