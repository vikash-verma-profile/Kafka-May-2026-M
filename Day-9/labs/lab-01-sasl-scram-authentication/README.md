# Lab 01 — Configure SASL/SCRAM Authentication

**Objective:** Create a Kafka user with a password (SCRAM), turn on a SASL listener on the broker, and send your first message using those credentials.

**Source:** Kafka_Security_Monitoring.pptx — Slide 8  
**Time:** ~20 minutes  
**Next lab:** [Lab 02 — ACLs](../lab-02-kafka-acls/README.md)

---

## What you are learning (in plain terms)

- **SASL** = a standard way for clients to *log in* to Kafka.
- **SCRAM** = the login uses a username + password (stored securely on the broker).
- **SASL_PLAINTEXT** = login is encrypted at the protocol level, but message bytes on the wire are *not* TLS-encrypted (fine for this lab; Lab 03 adds TLS).

---

## Before you start — checklist

- [ ] Kafka **4.2.0** (or 3.x+) installed — example path `C:\kafka-bin\kafka_2.13-4.2.0`
- [ ] Cluster running from [my-config/README.md](../my-config/README.md) (controller + 3 brokers)
- [ ] You are **not** using a production cluster
- [ ] Client config ready: [configs/client-scram.properties](../configs/client-scram.properties) or [my-config/client-scram-oneshot.properties](../my-config/client-scram-oneshot.properties)

---

## Implementation tracks

| Track | What to run |
|-------|-------------|
| **Shell (recommended first time)** | [scripts/create-scram-user.bat](../scripts/create-scram-user.bat) + `kafka-console-producer` with [configs/client-scram.properties](../configs/client-scram.properties) |
| **Java** | `Lab01ScramProducer` — see [java-security-lab README](../java-security-lab/README.md) |
| **Python** | `python lab01_scram_producer.py` — see [python-security-lab README](../python-security-lab/README.md) |

**Docker:** not required for this lab.

---

## Step 0 — Open terminals and go to Kafka

1. Open **Command Prompt** or **PowerShell**.
2. Confirm Kafka home:

   ```bat
   echo %KAFKA_HOME%
   ```

   If empty, set it (example):

   ```bat
   set KAFKA_HOME=C:\kafka\kafka_2.13-3.6.0
   ```

3. Change to Kafka’s `bin\windows` folder (optional but keeps commands short):

   ```bat
   cd /d %KAFKA_HOME%\bin\windows
   ```

**Expected:** `cd` succeeds; no “path not found” error.

> **Windows tip:** If you see `More?` while pasting commands, you are in CMD line-continuation mode. Easiest fix: paste the command as a **single line**.

---

## Step 1 — Create SCRAM user `alice`

A SCRAM user is just a username + password stored in Kafka’s internal config (not in Windows users).

### Option A — Use the lab script (easiest)

From the `labs` folder:

```bat
cd /d c:\Users\om\Desktop\KafKa\Day-9\labs\scripts
create-scram-user.bat localhost:9092 alice secret
```

Arguments: `bootstrap-server` `username` `password` (defaults: `alice` / `secret`).

### Option B — Run the command yourself

```bat
cd /d %KAFKA_HOME%
bin\windows\kafka-configs.bat --bootstrap-server localhost:9092 ^
  --alter --add-config "SCRAM-SHA-512=[password=secret]" ^
  --entity-type users --entity-name alice
```

### Verify the user exists

```bat
bin\windows\kafka-configs.bat --bootstrap-server localhost:9092 ^
  --describe --entity-type users --entity-name alice
```

**What success looks like:** Output contains `SCRAM-SHA-512` for user `alice`.  
**If it fails:** `Connection refused` → start the broker first. `Unknown entity` → run the `--alter` command again.

---

## Step 2 — Enable SASL on the broker

You will add a **SASL listener** that requires SCRAM, while keeping PLAINTEXT for learning/troubleshooting.

### Already done in this repo?

If you use [my-config/](../my-config/) configs, **SASL is already enabled on all three brokers**. Skip to **Step 1** (create user) if the cluster is running, then **Step 4** (produce/consume).

Otherwise, configure manually:

This repo’s working 3-broker setup uses:

| Role | PLAINTEXT | SASL (SCRAM) |
|------|-----------|--------------|
| Broker-1 (node.id=2) | 9092 | **9096** |
| Broker-2 (node.id=3) | 9094 | **9097** |
| Broker-3 (node.id=4) | 9095 | **9098** |
| Controller | — | **9093** (not for clients) |

### 2.1 Edit broker configuration

1. Find your broker config file — usually `%KAFKA_HOME%\config\server.properties` or a per-broker file like `broker-1.properties`.
2. **Stop the broker** (Ctrl+C in the broker terminal).
3. Add or update these lines:

   ```properties
   listeners=SASL_PLAINTEXT://:9096,PLAINTEXT://:9092
   sasl.enabled.mechanisms=SCRAM-SHA-512
   listener.security.protocol.map=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,SASL_PLAINTEXT:SASL_PLAINTEXT
   ```

   Also set a correct advertised hostname (important on Windows):

   ```properties
   advertised.listeners=SASL_PLAINTEXT://localhost:9096,PLAINTEXT://localhost:9092
   ```

4. Save the file.

### 2.2 Create JAAS file for the broker itself

The broker SASL listener needs a JAAS file. This repo provides: [my-config/kafka_server_jaas.conf](../my-config/kafka_server_jaas.conf):

```text
KafkaServer {
  org.apache.kafka.common.security.scram.ScramLoginModule required
  username="alice"
  password="secret";
};
```

> For production labs with a separate broker `admin` user, you can change this to `admin` / `admin-secret` and create that SCRAM user too.

### 2.3 Set `KAFKA_OPTS` and start each broker

**Important:** `KAFKA_OPTS` must be set in the **same** terminal session **before** `kafka-server-start`.

**Broker-1 example (CMD):**

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
set KAFKA_OPTS=-Djava.security.auth.login.config=c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\kafka_server_jaas.conf
bin\windows\kafka-server-start.bat c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\broker-1.properties
```

Repeat for `broker-2.properties` and `broker-3.properties` in **separate** terminals. Full commands: [my-config/README.md](../my-config/README.md).

**What success looks like:** Logs show `Awaiting socket connections on ...9096` (SASL) and `...9092` (PLAINTEXT); no `LoginException` or JAAS errors.

> **Note:** `KAFKA_OPTS` only applies to that terminal window. If you close it and restart the broker, set `KAFKA_OPTS` again.

> **Production:** Use `SASL_SSL`, not `SASL_PLAINTEXT` — covered in [Lab 03](../lab-03-tls-sasl-end-to-end/README.md).

---

## Step 3 — Prepare client properties

The lab repo includes a ready-made file: [configs/client-scram.properties](../configs/client-scram.properties).

1. Copy it to a convenient place, e.g. `%KAFKA_HOME%\config\client-scram.properties`.
2. Open it and confirm username/password match what you created:

   ```properties
   security.protocol=SASL_PLAINTEXT
   sasl.mechanism=SCRAM-SHA-512
   sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required \
     username="alice" \
     password="secret";
   ```

**Important:** The password in this file must **exactly** match Step 1.

---

## Step 4 — Create topic and produce a message

All commands below must use your **SASL port** (in this setup: **9096**) and `--command-config`.

### 4.1 Create topic `orders`

```bat
cd /d %KAFKA_HOME%
bin\windows\kafka-topics.bat --create --topic orders --bootstrap-server localhost:9096 ^
  --command-config c:\Users\om\Desktop\KafKa\Day-9\labs\configs\client-scram.properties ^
  --partitions 3 --replication-factor 3
```

**Expected:** `Created topic orders.`  
**If topic exists:** That is OK — skip or use `--describe` to confirm.

### 4.2 Start console producer

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9096,localhost:9097,localhost:9098 ^
  --command-config c:\Users\om\Desktop\KafKa\Day-9\labs\configs\client-scram.properties ^
  --topic orders
```

Type a line, e.g. `hello-scram`, and press **Enter**.

**What success looks like:** No `Authentication failed` or `Connection refused`; cursor waits for more lines.

### 4.3 (Optional) Produce with Java or Python instead of console producer

**Java** ([java-security-lab](../java-security-lab/README.md)):

```bat
cd c:\Users\om\Desktop\KafKa\Day-9\labs\java-security-lab
mvn -q exec:java -Dexec.mainClass=day9.labs.Lab01ScramProducer -Dexec.args="localhost:9096 orders alice secret"
```

**Python** ([python-security-lab](../python-security-lab/README.md)):

```bat
cd c:\Users\om\Desktop\KafKa\Day-9\labs\python-security-lab
python lab01_scram_producer.py localhost:9096 orders alice secret
```

### 4.4 (Optional) Read the message back

Open a **new** terminal:

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9096,localhost:9097,localhost:9098 ^
  --command-config c:\Users\om\Desktop\KafKa\Day-9\labs\configs\client-scram.properties ^
  --topic orders --from-beginning --group test-read-once
```

**Expected:** You see `hello-scram`. Press Ctrl+C to stop.

---

## Checkpoint — you are done when

- [ ] User `alice` appears in `kafka-configs --describe` (via port **9092**)
- [ ] All three brokers listen on SASL ports **9096 / 9097 / 9098**
- [ ] Producer connects with `--command-config` and bootstrap `localhost:9096,localhost:9097,localhost:9098`
- [ ] Consumer reads messages with the same config

---

## Troubleshooting

See also: [TROUBLESHOOTING.md](../TROUBLESHOOTING.md)

| Symptom | Likely cause | What to do |
|---------|--------------|------------|
| `Authentication failed` | Wrong password or user not created | Re-run Step 1 on **9092**; fix client properties |
| `enabled mechanisms are []` on **9093** | Used controller port for SCRAM | Use **9096/9097/9098**, not 9093 |
| `Unexpected handshake` on **9092** | SCRAM config on PLAINTEXT port | Use SASL ports only with `--command-config` |
| `0 messages` / producer timeout | SASL only on one broker | Enable SASL on **all** brokers ([my-config](../my-config/)) |
| Broker exits: JAAS not set | `KAFKA_OPTS` missing | Set before start; see [my-config/README](../my-config/README.md) |
| CMD shows `More?` | Broken multi-line paste | Paste command as **one line** |
| `LoginModule not found` | Typo in `sasl.jaas.config` | Use `client-scram-oneshot.properties` for CLI |

---

## What’s next?

In [Lab 02](../lab-02-kafka-acls/README.md) you will turn on the **authorizer** so only users with ACLs can read or write topics — even if they have a valid password.
