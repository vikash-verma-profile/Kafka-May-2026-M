# Kafka on Windows — Lab guide (Kafka 4.2)

**Install path:** `C:\kafka-bin\kafka_2.13-4.2.0`  
**Config file:** `config\server.properties` (not `config\kraft\server.properties`)  
**Scripts folder:** `bin\windows\` (not `bin\`)

**Alternative — Kafka in Docker (same CLI lab):** [kafka-docker-lab-guide.md](./kafka-docker-lab-guide.md)

**Multi-broker local (3 brokers, CLI):** [kafka-local-multi-broker-cli-lab.md](./kafka-local-multi-broker-cli-lab.md)

**Full architecture (infra + diagrams):** [kafka-architecture-guide.md](./kafka-architecture-guide.md)

Use **3 terminals** for the full lab. Every command below is copy-paste ready.

---

## Before you start

| Rule | Detail |
|------|--------|
| Always `cd` first | `cd C:\kafka-bin\kafka_2.13-4.2.0` |
| Script path | `bin\windows\kafka-....bat` |
| Do not use from Labs | `bin\windows\` does not exist under `Day-2\Labs` |
| Log4j line | `Reconfiguration failed...` is a warning -ignore if commands work |

---

# Part 1 -One-time setup (skip if already done)

Run once on a new machine, or after deleting `C:\kafka-data\kraft-combined-logs`.

### Step 1 -Open Terminal A

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
```

### Step 2 -Create data folder

```bat
mkdir C:\kafka-data\kraft-combined-logs
```

### Step 3 -Generate cluster ID (copy the UUID from output)

```bat
bin\windows\kafka-storage.bat random-uuid
```

Example output:

```text
IrBZOkc6Siq9sMLnDA53Hg
```

### Step 4 -Format storage (Kafka 4.2 needs `--standalone`)

Replace `YOUR_UUID` with the ID from Step 3 (or use `IrBZOkc6Siq9sMLnDA53Hg` if that is yours):

```bat
bin\windows\kafka-storage.bat format -t YOUR_UUID -c config\server.properties --standalone
```

Expected output includes:

```text
Formatting dynamic metadata voter directory C:/kafka-data/kraft-combined-logs ...
```

### Step 5 -Confirm format worked

```bat
dir C:\kafka-data\kraft-combined-logs\meta.properties
```

You should see `meta.properties`. If missing, do not start Kafka -repeat Step 4.

---

# Part 2 -Start Kafka (every lab session)

### Terminal 1 -Broker (keep open)

**Command 1**

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
```

**Command 2**

```bat
bin\windows\kafka-server-start.bat config\server.properties
```

**Wait for:**

```text
Kafka Server started
```

Do not close this terminal.

---

# Part 3 -Verify broker (Terminal 2)

Open a **new** terminal.

**Command 1**

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
```

**Command 2**

```bat
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list
```

**Expected output (example):**

```text
demo-topic
lab-messages
```

If connection fails, go back to Terminal 1 and confirm `Kafka Server started`.

---

# Part 4 -Publisher / consumer lab

Topic name: **`lab-messages`**

## Terminal 2 -Create topic

**Command 1**

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
```

**Command 2** (skip if topic already exists)

```bat
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --create --topic lab-messages --partitions 1 --replication-factor 1
```

**Expected:**

```text
Created topic lab-messages.
```

**Command 3 -List topics**

```bat
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list
```

**Expected:**

```text
lab-messages
```

---

## Terminal 3 -Consumer (subscriber) -start first

Open a **new** terminal.

**Command 1**

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
```

**Command 2**

```bat
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic lab-messages --from-beginning
```

Terminal 3 will wait for messages (cursor blinks, no error).

---

## Terminal 2 -Producer (publisher)

In Terminal 2 (or another new terminal after topic create):

**Command 1** (if not already in Kafka folder)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
```

**Command 2**

```bat
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic lab-messages
```

**Type in Terminal 2 and press Enter after each line:**

```text
Hello Kafka
This is message 2
```

---

## What you should see

| Terminal | Role | You type / run | You see |
|----------|------|----------------|---------|
| **1** | Broker | `kafka-server-start.bat` | `Kafka Server started` |
| **2** | Producer | `Hello Kafka` + Enter | (no echo required; cursor ready for next line) |
| **3** | Consumer | (nothing, already running) | `Hello Kafka` then `This is message 2` |

**Terminal 3 example after you send from Terminal 2:**

```text
Hello Kafka
This is message 2
```

Send more lines in Terminal 2 → each line appears in Terminal 3.

---

# Part 5 -Command cheat sheet (in order)

### Terminal 1 -Kafka running

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-server-start.bat config\server.properties
```

### Terminal 2 -Topic + producer

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --create --topic lab-messages --partitions 1 --replication-factor 1
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic lab-messages
```

### Terminal 3 -Consumer

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic lab-messages --from-beginning
```

**Best order:** Terminal 1 → Terminal 3 (consumer) → Terminal 2 (producer) → type messages.

---

# Troubleshooting

| Problem | Fix |
|---------|-----|
| `config\kraft\server.properties` not found | Use `config\server.properties` |
| `No readable meta.properties` | Run Part 1 Step 4 with `--standalone` |
| `The system cannot find the path specified` | Run `cd C:\kafka-bin\kafka_2.13-4.2.0` first; use `bin\windows\` |
| `'kafka' is not recognized` in `C:\kafka-bin\...` | Do not use `kafka` helper here -use `bin\windows\...` directly |
| `'kafka-topics.bat' is not recognized` in `bin\` | Go up one level; scripts are in `bin\windows\` |
| Topic already exists | Skip `--create`; go to producer |
| Consumer shows nothing | Same topic name `lab-messages`; press Enter after each line in producer |
| Consumer started late | Use `--from-beginning` on consumer |

---

# Optional -run from Labs folder

Only if you are in `C:\Users\om\Desktop\KafKa\Day-2\Labs`:

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs
kafka kafka-topics.bat --bootstrap-server localhost:9092 --list
```

Do **not** use `kafka ...` when your prompt is already `C:\kafka-bin\kafka_2.13-4.2.0>`.

---

# Your machine -quick start (storage already formatted)

**Terminal 1**

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-server-start.bat config\server.properties
```

**Terminal 3** (consumer)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic lab-messages --from-beginning
```

**Terminal 2** (producer)

```bat
cd C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic lab-messages
```

Type a message → read it in Terminal 3.

---

# Part 6 — Java client lab

Maven project: **`kafka-java-lab/`** (must run Maven **inside** that folder, or use the helper scripts from Labs).

**Build once**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-java-lab
mvn -q compile
```

**Option A — from `kafka-java-lab` folder**

**Terminal A — consumer**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-java-lab
mvn -q exec:java -Dexec.mainClass=com.kafka.lab.SimpleConsumer
```

**Terminal B — producer**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-java-lab
mvn -q exec:java -Dexec.mainClass=com.kafka.lab.SimpleProducer
```

**Option B — from `Labs` folder (helper scripts)**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs
run-consumer.bat
```

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs
run-producer.bat
```

See **`kafka-java-lab-guide.md`** for full create/run/troubleshooting details.

---

# Part 7 — Python client lab

Maven-style guide: **`kafka-python-lab-guide.md`**

**Setup (once)**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-python-lab
python -m venv .venv
.venv\Scripts\pip install -r requirements.txt
```

**Consumer**

```bat
.venv\Scripts\python simple_consumer.py
```

**Producer**

```bat
.venv\Scripts\python simple_producer.py
```

Or from `Labs`: `run-python-consumer.bat` then `run-python-producer.bat`

---

# Part 8 — .NET client lab

Full guide: **`kafka-dotnet-lab-guide.md`**

**Build (once)**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-dotnet-lab\src\KafkaDotNetLab
dotnet build
```

**Consumer**

```bat
dotnet run -- consumer
```

**Producer**

```bat
dotnet run -- producer
```

Or from `Labs`: `run-dotnet-consumer.bat` then `run-dotnet-producer.bat`

---

# Part 9 — Kafka on Docker (alternative to local install)

Same CLI producer/consumer lab, broker in a container on `localhost:9092`.

**Full guide:** [kafka-docker-lab-guide.md](./kafka-docker-lab-guide.md)

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-docker
docker compose up -d
```

```bat
cd scripts
kafka-cli.bat topics --bootstrap-server localhost:9092 --list
kafka-cli.bat console-consumer --bootstrap-server localhost:9092 --topic lab-messages --from-beginning
kafka-cli.bat console-producer --bootstrap-server localhost:9092 --topic lab-messages
```

Stop local Kafka first if port 9092 is already in use.

---

# Part 10 — Multi-broker local cluster (3 brokers, CLI)

Same scenarios as Docker multi-broker lab, using `C:\kafka-bin\kafka_2.13-4.2.0`.

**Full guide:** [kafka-local-multi-broker-cli-lab.md](./kafka-local-multi-broker-cli-lab.md)

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-local-3brokers\scripts
setup-cluster.bat
```

Start `start-broker1.bat`, `start-broker2.bat`, `start-broker3.bat` in 3 terminals.

```bat
set BS=localhost:9092,localhost:9094,localhost:9095
bin\windows\kafka-console-producer.bat --bootstrap-server %BS% --topic scenario2-topic
```
