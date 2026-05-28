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

- [ ] `KAFKA_HOME` is set and Kafka CLI works (see [main README](../README.md#first-time-setup-do-this-once-before-lab-01))
- [ ] One broker is running (default PLAINTEXT port **9092**)
- [ ] You are **not** using a production cluster
- [ ] You have a text editor to edit `server.properties`

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

You will add a **second listener** on port **9093** that requires SCRAM, while keeping **9092** as PLAINTEXT for now (easier for learning).

### 2.1 Edit broker configuration

1. Find your broker config file — usually `%KAFKA_HOME%\config\server.properties` or `broker-1.properties`.
2. **Stop the broker** (Ctrl+C in the broker terminal).
3. Add or update these lines:

   ```properties
   listeners=SASL_PLAINTEXT://:9093,PLAINTEXT://:9092
   sasl.enabled.mechanisms=SCRAM-SHA-512
   listener.security.protocol.map=SASL_PLAINTEXT:SASL_PLAINTEXT,PLAINTEXT:PLAINTEXT
   ```

4. Save the file.

### 2.2 Create JAAS file for the broker itself

The broker also needs an identity to authenticate inter-broker traffic. Create `%KAFKA_HOME%\config\kafka_server_jaas.conf`:

```text
KafkaServer {
  org.apache.kafka.common.security.scram.ScramLoginModule required
  username="admin"
  password="admin-secret";
};
```

### 2.3 Create SCRAM user `admin` (must match JAAS)

While the broker is still **stopped**, on PLAINTEXT port 9092:

```bat
bin\windows\kafka-configs.bat --bootstrap-server localhost:9092 ^
  --alter --add-config "SCRAM-SHA-512=[password=admin-secret]" ^
  --entity-type users --entity-name admin
```

### 2.4 Set `KAFKA_OPTS` and start the broker

In the **same** terminal session where you start Kafka:

```bat
set KAFKA_OPTS=-Djava.security.auth.login.config=%KAFKA_HOME%\config\kafka_server_jaas.conf
bin\windows\kafka-server-start.bat %KAFKA_HOME%\config\server.properties
```

**What success looks like:** Log lines show listeners on **9092** and **9093**; no `LoginException` or JAAS file errors.

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

All commands below must use port **9093** and `--command-config` / `--producer.config`.

### 4.1 Create topic `orders`

```bat
cd /d %KAFKA_HOME%
bin\windows\kafka-topics.bat --create --topic orders --bootstrap-server localhost:9093 ^
  --command-config c:\Users\om\Desktop\KafKa\Day-9\labs\configs\client-scram.properties ^
  --partitions 3 --replication-factor 1
```

**Expected:** `Created topic orders.`  
**If topic exists:** That is OK — skip or use `--describe` to confirm.

### 4.2 Start console producer

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9093 ^
  --producer.config c:\Users\om\Desktop\KafKa\Day-9\labs\configs\client-scram.properties ^
  --topic orders
```

Type a line, e.g. `hello-scram`, and press **Enter**.

**What success looks like:** No `Authentication failed` or `Connection refused`; cursor waits for more lines.

### 4.3 (Optional) Produce with Java or Python instead of console producer

**Java** ([java-security-lab](../java-security-lab/README.md)):

```bat
cd c:\Users\om\Desktop\KafKa\Day-9\labs\java-security-lab
mvn -q exec:java -Dexec.mainClass=day9.labs.Lab01ScramProducer -Dexec.args="localhost:9093 orders alice secret"
```

**Python** ([python-security-lab](../python-security-lab/README.md)):

```bat
cd c:\Users\om\Desktop\KafKa\Day-9\labs\python-security-lab
python lab01_scram_producer.py localhost:9093 orders alice secret
```

### 4.4 (Optional) Read the message back

Open a **new** terminal:

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9093 ^
  --consumer.config c:\Users\om\Desktop\KafKa\Day-9\labs\configs\client-scram.properties ^
  --topic orders --from-beginning --group test-read-once
```

**Expected:** You see `hello-scram`. Press Ctrl+C to stop.

---

## Checkpoint — you are done when

- [ ] User `alice` appears in `kafka-configs --describe`
- [ ] Broker listens on port **9093** (SASL)
- [ ] Producer connects on **9093** with SCRAM config
- [ ] Message is visible to consumer with the same client config

---

## Troubleshooting

| Symptom | Likely cause | What to do |
|---------|--------------|------------|
| `Authentication failed` | Wrong password or user not created | Re-run Step 1; fix `client-scram.properties` |
| Broker exits on start | JAAS path wrong | Check `%KAFKA_HOME%\config\kafka_server_jaas.conf` exists; `KAFKA_OPTS` path uses forward slashes or escaped backslashes |
| `Connection refused` on 9093 | SASL listener not enabled | Re-check `listeners=` in `server.properties`; restart broker |
| Producer still works on **9092** without config | PLAINTEXT still open | Expected for this lab — do not disable until you understand Lab 02–03 |
| `LoginModule not found` | Typo in `sasl.jaas.config` | Copy JAAS line exactly from Step 3 |

---

## What’s next?

In [Lab 02](../lab-02-kafka-acls/README.md) you will turn on the **authorizer** so only users with ACLs can read or write topics — even if they have a valid password.
