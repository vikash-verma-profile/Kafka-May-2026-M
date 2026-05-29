# Lab 02 — kafka-acls: Grant & Revoke

**Objective:** Control *who* can read or write *which* topics — even if they have a valid SCRAM password.

**Source:** Kafka_Security_Monitoring.pptx — Slide 11  
**Time:** ~25 minutes  
**Requires:** [Lab 01](../lab-01-sasl-scram-authentication/README.md) completed (SCRAM users + SASL port **9096/9097/9098**)

---

## What you are learning

- **ACL** = Access Control List — rules like “user `alice` may **Write** to topic `orders`”.
- **Default deny:** Once the authorizer is on, *nobody* can do anything unless an ACL allows it (except **super users**).
- **Principal** = Kafka’s name for a user, written as `User:alice`.

---

## Before you start — checklist

- [ ] Lab 01 done: SCRAM users `alice` and `bob` exist (create `bob` the same way as `alice` if needed)
- [ ] Broker SASL works on **9096/9097/9098**
- [ ] [configs/client-scram.properties](../configs/client-scram.properties) points at `alice` (admin commands often use an admin user — for this lab, `alice` is fine if she has super user OR you use admin credentials)

---

## Step 0 — Enable the authorizer on the broker

ACLs only work when the broker enforces them.

1. **Stop** the broker.
2. Edit `server.properties` and add:

   ```properties
   authorizer.class.name=org.apache.kafka.metadata.authorizer.StandardAuthorizer
   super.users=User:admin
   allow.everyone.if.no.acl.found=false
   ```

   - `super.users` — `admin` can do everything (matches Lab 01 JAAS user).
   - `allow.everyone.if.no.acl.found=false` — **deny by default** (important).

3. Set `KAFKA_OPTS` for JAAS (same as Lab 01) and **start** the broker.

**What success looks like:** Broker starts; logs mention authorizer / StandardAuthorizer.

> **Note for your `my-config/` cluster:** Your brokers already run with both PLAINTEXT and SASL listeners. Use the **SASL ports** (9096/9097/9098) for ACL commands with `--command-config`, and use the PLAINTEXT ports only for emergency troubleshooting.

**First-time tip:** After this change, even `alice` cannot produce until you grant ACLs in Step 1 — that is expected.

---

## Step 1 — Allow `alice` to write to `orders`

### Option A — Lab script

```bat
cd /d c:\Users\om\Desktop\KafKa\Day-9\labs\scripts
grant-acls.bat localhost:9096
```

(Only runs the grant part — read the script to see exact ACLs.)

### Option B — Manual command

```bat
cd /d %KAFKA_HOME%
bin\windows\kafka-acls.bat --bootstrap-server localhost:9096 ^
  --command-config c:\Users\om\Desktop\KafKa\Day-9\labs\configs\client-scram.properties ^
  --add --allow-principal User:alice ^
  --operation Write --topic orders
```

**Expected output:** `Adding ACLs for resource ...` and success message.

**What this means:** User `alice` may publish messages to topic `orders` only.

---

## Step 2 — Allow `bob` to read `orders` (consumer group `billing-svc`)

Consumers need permission on **both** the topic and the **consumer group**.

### 2.1 Read permission on topic

```bat
bin\windows\kafka-acls.bat --bootstrap-server localhost:9096 ^
  --command-config c:\Users\om\Desktop\KafKa\Day-9\labs\configs\client-scram.properties ^
  --add --allow-principal User:bob ^
  --operation Read --topic orders ^
  --group billing-svc
```

### 2.2 Describe permission on group (required Kafka 2.0+)

```bat
bin\windows\kafka-acls.bat --bootstrap-server localhost:9096 ^
  --command-config c:\Users\om\Desktop\KafKa\Day-9\labs\configs\client-scram.properties ^
  --add --allow-principal User:bob ^
  --operation Describe --group billing-svc
```

**Create `bob` if you have not yet:**

```bat
cd /d c:\Users\om\Desktop\KafKa\Day-9\labs\scripts
create-scram-user.bat localhost:9092 bob bob-secret
```

For bob’s client tests, use a copy of `client-scram.properties` with `username="bob"` and `password="bob-secret"`.

---

## Step 3 — List ACLs (verify they were stored)

```bat
bin\windows\kafka-acls.bat --bootstrap-server localhost:9096 ^
  --command-config c:\Users\om\Desktop\KafKa\Day-9\labs\configs\client-scram.properties ^
  --list --topic orders
```

**What success looks like:** Lines showing `User:alice` **Write** and `User:bob` **Read** on `orders`.

---

## Step 4 — Verify access (hands-on test)

Use separate config files or edit JAAS username/password per user.

| User | Test | How | Expected result |
|------|------|-----|-----------------|
| **alice** | Produce | `kafka-console-producer` on `orders` with alice config | **Success** |
| **bob** | Consume | `kafka-console-consumer --group billing-svc` with bob config | **Success** |
| **bob** | Produce | Same producer as alice but bob config | **TopicAuthorizationException** |

### Example — alice produces

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9096,localhost:9097,localhost:9098 --command-config c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\client-scram-oneshot.properties --topic orders
```

### Example — bob consumes

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9096,localhost:9097,localhost:9098 --command-config c:\path\to\client-bob.properties --topic orders --group billing-svc
```

**If bob gets `GroupAuthorizationException`:** You forgot **Describe** on group (Step 2.2).

---

## Step 5 — Revoke alice’s write permission

```bat
bin\windows\kafka-acls.bat --bootstrap-server localhost:9096 --command-config c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\client-scram-oneshot.properties --remove --allow-principal User:alice --operation Write --topic orders
```

Confirm removal:

```bat
bin\windows\kafka-acls.bat --bootstrap-server localhost:9096 --command-config c:\Users\om\Desktop\KafKa\Day-9\labs\my-config\client-scram-oneshot.properties --list --topic orders
```

Try producing as **alice** again.

**Expected:** `TopicAuthorizationException` — password is valid, but ACL denies write.

---

## Checkpoint — you are done when

- [ ] `kafka-acls --list` shows bob Read (+ Describe on group)
- [ ] alice **cannot** produce after revoke
- [ ] bob **cannot** produce (never granted)
- [ ] You understand **default deny** when `allow.everyone.if.no.acl.found=false`

---

## Reference — common ACL operations

| Resource | Operations you will use |
|----------|-------------------------|
| **Topic** | Read, Write, Create, Delete, Describe, Alter |
| **Group** | Read, Describe |

---

## Troubleshooting

See [TROUBLESHOOTING.md](../TROUBLESHOOTING.md)

| Symptom | Fix |
|---------|-----|
| All users denied | Grant ACLs as `admin` super user; check `super.users=User:admin` |
| `kafka-acls` auth fails | Use SASL port **9096** + `--command-config` |
| Used port 9093 | Controller port — use **9096/9097/9098** |
| Consumer works without group ACL | Ensure authorizer enabled and broker restarted |
| `--remove` says no ACL | Principal or operation typo — run `--list` first |

---

## What’s next?

[Lab 03](../lab-03-tls-sasl-end-to-end/README.md) — encrypt traffic with **TLS** while keeping SCRAM login.
