# Lab 01-Configure SASL/SCRAM Authentication

**Objective:** Create a SCRAM user, enable a SASL listener on the broker, and produce a message with SCRAM credentials.

From **Kafka_Security_Monitoring.pptx**-Slide 8. **Time:** ~20 min.

---

## Implementation

| Track | Command |
|-------|---------|
| **Shell** | [scripts/create-scram-user.bat](../scripts/create-scram-user.bat) + `kafka-console-producer` with [configs/client-scram.properties](../configs/client-scram.properties) |
| **Python** | `python lab01_scram_producer.py localhost:9093 orders alice secret` in [python-security-lab](../../python-security-lab/) |

---

## Prerequisites

- Kafka broker (KRaft) with `kafka-configs` access
- **Use a lab cluster**-not production

---

## Step 1-Create SCRAM user

```bat
cd %KAFKA_HOME%
bin\windows\kafka-configs.bat --bootstrap-server localhost:9092 ^
  --alter --add-config "SCRAM-SHA-512=[password=secret]" ^
  --entity-type users --entity-name alice
```

Verify:

```bat
bin\windows\kafka-configs.bat --bootstrap-server localhost:9092 ^
  --describe --entity-type users --entity-name alice
```

---

## Step 2-Enable SASL on broker

Edit `server.properties` (or `broker-1.properties`):

```properties
listeners=SASL_PLAINTEXT://:9093,PLAINTEXT://:9092
sasl.enabled.mechanisms=SCRAM-SHA-512
listener.security.protocol.map=SASL_PLAINTEXT:SASL_PLAINTEXT,PLAINTEXT:PLAINTEXT
```

Create `kafka_server_jaas.conf`:

```text
KafkaServer {
  org.apache.kafka.common.security.scram.ScramLoginModule required
  username="admin"
  password="admin-secret";
};
```

Set before start:

```bat
set KAFKA_OPTS=-Djava.security.auth.login.config=%KAFKA_HOME%\config\kafka_server_jaas.conf
```

Create admin SCRAM user matching JAAS. Restart broker.

> For production use **SASL_SSL** (Lab 03), not SASL_PLAINTEXT.

---

## Step 3-Client properties (`client-scram.properties`)

```properties
security.protocol=SASL_PLAINTEXT
sasl.mechanism=SCRAM-SHA-512
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required \
  username="alice" \
  password="secret";
```

---

## Step 4-Create topic and produce

```bat
bin\windows\kafka-topics.bat --create --topic orders --bootstrap-server localhost:9093 ^
  --command-config client-scram.properties --partitions 3 --replication-factor 1

bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9093 ^
  --producer.config client-scram.properties --topic orders
```

Type a test message.

---

## Checkpoint

- [ ] User `alice` exists in broker config
- [ ] Producer connects on SASL port 9093
- [ ] Message visible via consumer with same client config

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| `Authentication failed` | Password mismatch; user not created |
| Broker won't start | JAAS path wrong; check `KAFKA_OPTS` |
| Still works on 9092 PLAINTEXT | Expected until PLAINTEXT disabled |
