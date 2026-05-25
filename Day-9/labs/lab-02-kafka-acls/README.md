# Lab 02 — kafka-acls: Grant & Revoke

**Objective:** Grant produce/consume ACLs to different principals, verify access, then revoke and confirm denial.

From **Kafka_Security_Monitoring.pptx** — Slide 11. **Time:** ~25 min.

---

## Prerequisites

- [Lab 01](../lab-01-sasl-scram-authentication/README.md) — SCRAM users `alice`, `bob`
- Broker with authorizer enabled:

```properties
authorizer.class.name=org.apache.kafka.metadata.authorizer.StandardAuthorizer
super.users=User:admin
allow.everyone.if.no.acl.found=false
```

Restart broker after enabling ACLs.

---

## Step 1 — Allow alice to write `orders`

```bat
bin\windows\kafka-acls.bat --bootstrap-server localhost:9093 ^
  --command-config client-scram.properties ^
  --add --allow-principal User:alice ^
  --operation Write --topic orders
```

---

## Step 2 — Allow bob to read `orders` with group `billing-svc`

```bat
bin\windows\kafka-acls.bat --bootstrap-server localhost:9093 ^
  --command-config client-scram.properties ^
  --add --allow-principal User:bob ^
  --operation Read --topic orders ^
  --group billing-svc
```

Bob also needs **Describe** on group (Kafka 2.0+):

```bat
--operation Describe --group billing-svc
```

---

## Step 3 — List ACLs

```bat
bin\windows\kafka-acls.bat --bootstrap-server localhost:9093 ^
  --command-config client-scram.properties ^
  --list --topic orders
```

---

## Step 4 — Verify access

| User | Action | Expected |
|------|--------|----------|
| alice | produce to `orders` | Success |
| bob | consume `orders` / `billing-svc` | Success |
| bob | produce to `orders` | **TopicAuthorizationException** |

---

## Step 5 — Revoke alice write

```bat
bin\windows\kafka-acls.bat --bootstrap-server localhost:9093 ^
  --command-config client-scram.properties ^
  --remove --allow-principal User:alice ^
  --operation Write --topic orders
```

Produce as alice again → **TopicAuthorizationException**.

---

## Checkpoint

- [ ] ACL list shows alice Write, bob Read
- [ ] Revoke blocks alice produce
- [ ] Understood default deny model (slide 9)

---

## Reference — common operations

Read, Write, Create, Delete, Describe, Alter (topics); Read, Describe (groups).
