# Lab 03 — End-to-End TLS + SASL

**Objective:** Generate keystores, configure `SASL_SSL` listener, and produce/consume over encrypted SCRAM.

From **Kafka_Security_Monitoring.pptx** — Slide 14. **Time:** ~30 min.

---

## Prerequisites

- Labs 01–02
- `keytool` (JDK) on PATH

---

## Step 1 — Generate broker keystore

```bat
keytool -genkey -alias kafka -keyalg RSA -keystore kafka.keystore.jks ^
  -validity 365 -storepass changeit -keypass changeit ^
  -dname "CN=localhost, OU=Dev, O=Training, L=City, ST=State, C=US"
```

---

## Step 2 — Create CA and truststore (lab self-signed)

```bat
keytool -export -alias kafka -file kafka.crt -keystore kafka.keystore.jks -storepass changeit
keytool -import -alias ca -file kafka.crt -keystore kafka.truststore.jks -storepass changeit -noprompt
```

Copy `kafka.truststore.jks` to clients.

---

## Step 3 — Broker `server.properties`

```properties
listeners=SASL_SSL://:9093
ssl.keystore.location=/etc/kafka/kafka.keystore.jks
ssl.keystore.password=changeit
ssl.key.password=changeit
ssl.truststore.location=/etc/kafka/kafka.truststore.jks
ssl.truststore.password=changeit
security.inter.broker.protocol=SASL_SSL
sasl.enabled.mechanisms=SCRAM-SHA-512
```

Adjust paths for Windows: `C:\kafka-config\kafka.keystore.jks`.

---

## Step 4 — Client properties

```properties
security.protocol=SASL_SSL
ssl.truststore.location=C:\kafka-config\kafka.truststore.jks
ssl.truststore.password=changeit
sasl.mechanism=SCRAM-SHA-512
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="alice" password="secret";
```

---

## Step 5 — Restart broker and test

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9093 ^
  --producer.config client.properties --topic orders
```

---

## Step 6 — Verify TLS handshake

```bat
openssl s_client -connect localhost:9093
```

**Expected:** Certificate chain presented; handshake completes.

---

## Checkpoint

- [ ] Traffic encrypted (wireshark shows TLS on 9093)
- [ ] SCRAM auth still required
- [ ] Produce/consume succeed with client truststore

---

## Security notes (slide 12)

- Use `ssl.endpoint.identification.algorithm=HTTPS` in production (hostname verification)
- `ssl.client.auth=required` for mTLS (slide 6)
