# Lab 05 — Consume Avro Messages

**Objective:** Deserialize records from Lab 4 into typed `Employee` objects and verify offset resume after restart.

From **Seralization.pptx** — Slides 30–31.

---

## Run this lab (copy-paste)

**Prerequisite:** [Lab 04](../lab-04-produce-avro-messages/README.md) completed (10 messages on `employees-avro`).

```powershell
cd C:\Users\om\Desktop\KafKa\Day-6\labs\java-serialization-lab
mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab05.AvroConsumer"
```

| Track | Directory | Command |
|-------|-----------|---------|
| **Java** | `labs\java-serialization-lab` | see above |
| **Python** | `labs\python-serialization-lab` | `python lab05_avro_consumer.py` |

Do **not** run from `confluent-local`.

**Run once** for the lab. When you see `Read 10 records. Restart to verify offset resume.`, you are done — do not leave a second run waiting (see below).

---

## Expected output (first run)

```text
partition=2 offset=0 id=2 name=Employee-2 dept=Engineering salary=52000
...
Read 10 records. Restart to verify offset resume.
```

Order may differ (multiple partitions). You need **10 lines** with employee data plus the final summary line.

**SLF4J warnings** (`StaticLoggerBinder`) are harmless — ignore them.

---

## Second run (offset resume test)

Run the same command again **without** producing new messages:

```powershell
mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab05.AvroConsumer"
```

**Expected:** Only SLF4J lines, then **nothing** — the consumer waits for 10 *new* records because offsets were already committed (`avro-consumer-grp`).

That idle behavior **means success**: Kafka did not resend the original 10 messages.

**Stop it:** `Ctrl+C` → answer **`Y`** to `Terminate batch job (Y/N)?` ( **`n`** keeps the process running).

Do **not** confuse a hanging second run with failure. Your first run already completed the lab.

---

## Prerequisites

- Lab 04 producer ran successfully
- [confluent-local](../../confluent-local/) stack running
- Schema Registry: `http://localhost:8081`
- Kafka: `localhost:9092`

---

## Step 1 — Consumer properties

Already in lab code: `group.id=avro-consumer-grp`, `specific.avro.reader=true`, `auto.offset.reset=earliest`.

---

## Step 2 — Poll and print

Run the consumer (implementation table above).

**Expected:** All 10 employees print (`id`, `name`, `dept`, `salary`).

---

## Step 3 — Test offset commit and resume

1. **First run** — consumer prints 10 records, commits, prints `Read 10 records...`, then **exits**.
2. **Second run** — same command, same `group.id=avro-consumer-grp`.
3. **Expected:** No employee lines (no duplicates); process polls until you `Ctrl+C` with **`Y`**.

To see the consumer print again, produce more data first:

```powershell
mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab04.AvroProducer"
mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab05.AvroConsumer"
```

---

## Step 4 — Compare GenericRecord mode (optional)

Set `specific.avro.reader=false` and print fields via `GenericRecord.get("name")`.

---

## Checkpoint

- [ ] All 10 records deserialize as `Employee` POJOs
- [ ] Restart does not re-read entire topic (same group id)

---

## Next

[Lab 06 — Schema evolution](../lab-06-schema-evolution/README.md) → then [SCHEMA-REGISTRY.md](../SCHEMA-REGISTRY.md) to confirm `[1, 2]` versions.

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| No output on **first** run | Run Lab 04 producer first; check `docker compose ps` |
| Only SLF4J lines on **second** run | Normal — offsets committed; `Ctrl+C` then **`Y`** |
| `Terminate batch job (Y/N)?` | **`Y`** = stop; **`n`** = keep polling |
| `no POM in this directory` | `cd labs\java-serialization-lab` before `mvn` |
| `ClassCastException` / GenericRecord | Set `specific.avro.reader=true` |
| `SerializationException` | Registry down or wrong URL |
| Reads all 10 again every time | New `group.id` each run — lab uses `avro-consumer-grp` |
