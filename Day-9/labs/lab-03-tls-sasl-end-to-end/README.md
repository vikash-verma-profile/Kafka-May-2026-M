# Lab 03 — End-to-End TLS + SASL

**Objective:** Encrypt data on the network with **TLS** and still authenticate clients with **SCRAM** (`SASL_SSL`).

**Source:** Kafka_Security_Monitoring.pptx — Slide 14  
**Time:** ~30 minutes  
**Requires:** Labs [01](../lab-01-sasl-scram-authentication/README.md) and [02](../lab-02-kafka-acls/README.md)

---

## What you are learning

| Term | Meaning |
|------|---------|
| **TLS** | Encrypts bytes on the wire so others cannot read traffic |
| **Keystore** | Broker’s private key + certificate (`.jks` file) |
| **Truststore** | CA/certs the broker or client *trusts* |
| **SASL_SSL** | TLS encryption + SCRAM login together |

This lab uses a **self-signed** certificate — fine for training; production uses a real CA.

---

## Before you start — checklist

- [ ] `keytool` works: `keytool` (comes with JDK) — run in CMD and see help text
- [ ] Broker stopped (you will change listeners and SSL paths)
- [ ] A folder for certs, e.g. `C:\kafka-config\` (create it if missing)

---

## Step 0 — Create a working folder for certificates

```bat
mkdir C:\kafka-config
cd /d C:\kafka-config
```

All `keytool` commands below assume you are in this folder.

---

## Step 1 — Generate broker keystore

This creates a private key + self-signed certificate for `localhost`.

```bat
keytool -genkey -alias kafka -keyalg RSA -keystore kafka.keystore.jks ^
  -validity 365 -storepass changeit -keypass changeit ^
  -dname "CN=localhost, OU=Dev, O=Training, L=City, ST=State, C=US"
```

**What success looks like:** File `kafka.keystore.jks` appears in `C:\kafka-config`.

**First-time tip:** Passwords `changeit` are for labs only — never use in production.

---

## Step 2 — Build truststore (so clients trust the broker)

Export the cert from the keystore, then import it into a truststore:

```bat
cd /d C:\kafka-config
keytool -export -alias kafka -file kafka.crt -keystore kafka.keystore.jks -storepass changeit
keytool -import -alias ca -file kafka.crt -keystore kafka.truststore.jks -storepass changeit -noprompt
```

**What success looks like:** `kafka.truststore.jks` exists.

**Important:** Copy `kafka.truststore.jks` to every machine that runs clients (same PC in this lab).

---

## Step 3 — Configure broker `server.properties`

1. Stop the broker.
2. Set listener to **SASL_SSL** only on 9093 (lab simplification):

   ```properties
   listeners=SASL_SSL://:9093
   listener.security.protocol.map=SASL_SSL:SASL_SSL
   ssl.keystore.location=C:/kafka-config/kafka.keystore.jks
   ssl.keystore.password=changeit
   ssl.key.password=changeit
   ssl.truststore.location=C:/kafka-config/kafka.truststore.jks
   ssl.truststore.password=changeit
   security.inter.broker.protocol=SASL_SSL
   sasl.enabled.mechanisms=SCRAM-SHA-512
   ```

   Use **forward slashes** in paths or escaped backslashes — Kafka on Windows accepts `C:/kafka-config/...`.

3. Keep JAAS + SCRAM settings from Lab 01.
4. Start broker with `KAFKA_OPTS` set.

**What success looks like:** Logs show `SASL_SSL` listener on 9093; no `SslAuthenticationException` at startup.

Template in repo: [configs/broker-sasl-ssl.properties.snippet](../configs/broker-sasl-ssl.properties.snippet)

---

## Step 4 — Create client properties file

Create `C:\kafka-config\client.properties` (or copy [configs/client-sasl-ssl.properties.template](../configs/client-sasl-ssl.properties.template)):

```properties
security.protocol=SASL_SSL
ssl.truststore.location=C:/kafka-config/kafka.truststore.jks
ssl.truststore.password=changeit
sasl.mechanism=SCRAM-SHA-512
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="alice" password="secret";
```

**Checklist before testing:**

- [ ] `ssl.truststore.location` points to the **truststore**, not keystore
- [ ] Username/password match SCRAM user from Lab 01
- [ ] Port is **9093**

---

## Step 5 — Restart broker and test produce/consume

### 5.1 Produce a test message

```bat
cd /d %KAFKA_HOME%
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9093 ^
  --producer.config C:\kafka-config\client.properties --topic orders
```

Type `tls-test-message` and Enter.

**Expected:** Connected without SSL handshake errors.

### 5.2 Consume (optional)

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9093 ^
  --consumer.config C:\kafka-config\client.properties ^
  --topic orders --from-beginning --group tls-lab-group
```

---

## Step 6 — Verify TLS handshake (optional but educational)

If you have OpenSSL installed:

```bat
openssl s_client -connect localhost:9093
```

**What success looks like:** Certificate chain printed; handshake completes (you may see Kafka protocol noise after — that is OK).

**Without OpenSSL:** Success in Step 5 already proves TLS + SCRAM work.

---

## Checkpoint — you are done when

- [ ] Broker listens with `SASL_SSL` on 9093
- [ ] Produce/consume work only with truststore + SCRAM config
- [ ] You know the difference between keystore (broker identity) and truststore (who to trust)

---

## Security notes (from slides)

| Setting | Purpose |
|---------|---------|
| `ssl.endpoint.identification.algorithm=HTTPS` | Verify hostname matches cert (use in production) |
| `ssl.client.auth=required` | Mutual TLS — client presents cert too (advanced) |

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `SSL handshake failed` | Client missing truststore or wrong path |
| `CertificateException` | Exported wrong cert — repeat Step 2 |
| `Authentication failed` | SCRAM password wrong — TLS is fine, fix SASL |
| Broker won’t start | Keystore path wrong — use absolute path with forward slashes |
| Works without truststore | You might still be on PLAINTEXT port — use 9093 only |

---

## What’s next?

[Lab 04](../lab-04-jmx-prometheus/README.md) — expose broker metrics for monitoring.
