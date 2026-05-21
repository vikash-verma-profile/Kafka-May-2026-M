# Kafka on Docker -Lab Guide (CLI + Client Apps)

Run Kafka in **Docker** and use the **same CLI workflow** as the local Windows install (`kafka-topics`, console producer/consumer). Your Java, Python, and .NET labs still connect to **`localhost:9092`**.

**Local Windows install guide:** [kafka-kraft-setup-windows.md](./kafka-kraft-setup-windows.md)

**Multi-broker CLI lab (3 brokers, multiple producers/consumers):** [kafka-multi-broker-cli-lab.md](./kafka-multi-broker-cli-lab.md)

---

## 1. What you get

| Item | Location |
|------|----------|
| Docker Compose file | `kafka-docker/docker-compose.yml` |
| Start / stop scripts | `kafka-docker/scripts/start-kafka.bat`, `stop-kafka.bat` |
| CLI helper | `kafka-docker/scripts/kafka-cli.bat` |
| Broker address | `localhost:9092` |
| Container name | `kafka-broker` |
| Image | `apache/kafka:3.9.0` (KRaft, single node) |

No ZooKeeper. No manual `kafka-storage format` -the image configures KRaft on first start.

---

## 2. Prerequisites

| Requirement | Check |
|-------------|--------|
| **Docker Desktop** (Windows) | `docker --version` |
| **Docker Compose** | `docker compose version` |
| **Port 9092 free** | Stop local Kafka if it uses 9092 |

### Important: local Kafka vs Docker

If you already run Kafka from `C:\kafka-bin\...`, **stop it** before starting Docker Kafka. Both use port **9092**.

```bat
REM Close the terminal running kafka-server-start.bat, or stop the Java process using port 9092
```

---

## 3. Local install vs Docker -command mapping

| Task | Local Windows (your machine) | Docker (this lab) |
|------|------------------------------|-------------------|
| Start broker | `bin\windows\kafka-server-start.bat config\server.properties` | `docker compose up -d` |
| Stop broker | Close terminal / Ctrl+C | `docker compose down` |
| List topics | `bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list` | `docker exec kafka-broker /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list` |
| Create topic | `kafka-topics.bat ... --create --topic lab-messages ...` | Same args via `docker exec` or `kafka-cli.bat` |
| Producer | `kafka-console-producer.bat ...` | `docker exec -it kafka-broker /opt/kafka/bin/kafka-console-producer.sh ...` |
| Consumer | `kafka-console-consumer.bat ...` | `docker exec -it kafka-broker /opt/kafka/bin/kafka-console-consumer.sh ...` |
| Scripts path | `bin\windows\` | `/opt/kafka/bin/` **inside container** |
| Config | `config\server.properties` | Environment variables in `docker-compose.yml` |

**Bootstrap server** is always `localhost:9092` from your PC (host), and `localhost:9092` from inside the container.

---

## 4. One-time setup

### Step 4.1 -Verify Docker

```bat
docker --version
docker compose version
```

### Step 4.2 -Go to the Docker lab folder

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-docker
```

### Step 4.3 -Pull image (first time only)

```bat
docker compose pull
```

---

## 5. Start Kafka in Docker

### Option A -Helper script

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-docker\scripts
start-kafka.bat
```

### Option B -Manual

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-docker
docker compose up -d
```

**Check status**

```bat
docker compose ps
docker compose logs broker
```

**Expected:** container `kafka-broker` is `running`, logs show broker started.

**Test broker**

```bat
docker exec kafka-broker /opt/kafka/bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092
```

---

## 6. Stop Kafka in Docker

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-docker\scripts
stop-kafka.bat
```

Or:

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-docker
docker compose down
```

Data is removed when the container is removed (ephemeral `/tmp/kraft-combined-logs` inside container). For a fresh lab each time, `down` then `up` is enough.

---

# Part A -CLI lab (same as local machine)

Use **3 terminals** on your Windows host.

---

## Terminal 1 -Kafka running in Docker

Kafka runs in the background after `docker compose up -d`. You do **not** need a dedicated terminal unless you want logs:

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-docker
docker compose logs -f broker
```

Press Ctrl+C to stop following logs (broker keeps running).

---

## Terminal 2 -Create topic

**Using helper script**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-docker\scripts
kafka-cli.bat topics --bootstrap-server localhost:9092 --create --topic lab-messages --partitions 1 --replication-factor 1
```

**Using full docker exec (same as local CLI)**

```bat
docker exec kafka-broker /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic lab-messages --partitions 1 --replication-factor 1
```

**List topics**

```bat
kafka-cli.bat topics --bootstrap-server localhost:9092 --list
```

**Expected:**

```text
lab-messages
```

---

## Terminal 3 -Consumer (start before producer)

**Helper**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-docker\scripts
kafka-cli.bat console-consumer --bootstrap-server localhost:9092 --topic lab-messages --from-beginning
```

**Full command**

```bat
docker exec -it kafka-broker /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic lab-messages --from-beginning
```

Leave this terminal open. It waits for messages.

---

## Terminal 2 -Producer (send messages)

**Helper**

```bat
kafka-cli.bat console-producer --bootstrap-server localhost:9092 --topic lab-messages
```

**Full command**

```bat
docker exec -it kafka-broker /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic lab-messages
```

**Type and press Enter after each line:**

```text
Hello from Docker CLI
Message number 2
```

---

## Expected result (same as local lab)

| Terminal | You see |
|----------|---------|
| **Producer** | Cursor accepts lines (no echo required) |
| **Consumer** | `Hello from Docker CLI` then `Message number 2` |

---

# Part B -Run Java / Python / .NET labs against Docker Kafka

Kafka in Docker still exposes **`localhost:9092`** on your host. No code changes needed.

1. Start Docker Kafka (`docker compose up -d`)
2. Stop local Windows Kafka if it was using 9092
3. Run client labs as documented:

| Lab | Guide |
|-----|--------|
| Java | [kafka-java-lab-guide.md](./kafka-java-lab-guide.md) |
| Python | [kafka-python-lab-guide.md](./kafka-python-lab-guide.md) |
| .NET | [kafka-dotnet-lab-guide.md](./kafka-dotnet-lab-guide.md) |

Example: Java consumer + Python producer, all against Docker broker.

---

# Part C -Full command reference (copy-paste)

### Start / stop

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-docker
docker compose up -d
docker compose down
```

### Topics

```bat
docker exec kafka-broker /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list

docker exec kafka-broker /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic lab-messages --partitions 1 --replication-factor 1

docker exec kafka-broker /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic lab-messages
```

### Producer / consumer

```bat
docker exec -it kafka-broker /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic lab-messages

docker exec -it kafka-broker /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic lab-messages --from-beginning
```

### Helper script equivalents

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-docker\scripts
kafka-cli.bat topics --bootstrap-server localhost:9092 --list
kafka-cli.bat console-producer --bootstrap-server localhost:9092 --topic lab-messages
kafka-cli.bat console-consumer --bootstrap-server localhost:9092 --topic lab-messages --from-beginning
```

---

## 7. Side-by-side lab checklist

| Step | Local Windows | Docker |
|------|---------------|--------|
| 1. Start broker | `kafka-server-start.bat` | `docker compose up -d` |
| 2. Create topic | `kafka-topics.bat --create ...` | `kafka-cli.bat topics --create ...` |
| 3. Start consumer | `kafka-console-consumer.bat ...` | `kafka-cli.bat console-consumer ...` |
| 4. Start producer | `kafka-console-producer.bat ...` | `kafka-cli.bat console-producer ...` |
| 5. Type messages | Enter per line | Enter per line |
| 6. See messages | Consumer terminal | Consumer terminal |

---

## 8. Troubleshooting

| Problem | Cause | Fix |
|---------|-------|-----|
| `port is already allocated` | Local Kafka or other app on 9092 | Stop local broker; `docker compose down` |
| `Cannot connect to broker` | Container not ready | `docker compose logs broker`; wait 10–15 s after `up` |
| `kafka-broker` not found | Container not running | `docker compose up -d` from `kafka-docker` folder |
| Consumer shows nothing | Wrong topic or producer not sending | Same topic `lab-messages`; press Enter in producer |
| `docker` not recognized | Docker Desktop not installed/running | Start Docker Desktop |
| Java/Python connection refused | Docker not up or wrong port | `docker compose ps`; use `localhost:9092` |
| Topic already exists | Re-running create | Safe to ignore; use `--list` |
| Permission error on volume | Rare on Windows | Use Docker Desktop latest version |

**View logs**

```bat
docker compose logs -f broker
```

**Shell inside container**

```bat
docker exec -it kafka-broker bash
ls /opt/kafka/bin
```

---

## 9. Architecture diagram

```text
  Your Windows PC
  +------------------------------------------------------------------+
  |  Terminal: kafka-cli / Java / Python / .NET                      |
  |       |                                                          |
  |       |  bootstrap: localhost:9092                               |
  |       v                                                          |
  |  Docker Desktop                                                  |
  |  +------------------------+                                      |
  |  |  container: kafka-broker                                     |
  |  |  image: apache/kafka:3.9.0                                   |
  |  |  /opt/kafka/bin/*.sh  <-- CLI tools                           |
  |  |  KRaft broker + controller                                   |
  |  +------------------------+                                      |
  |       port map: 9092:9092                                        |
  +------------------------------------------------------------------+
```

---

## 10. Quick start (shortest path)

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-docker
docker compose up -d
```

**Consumer (Terminal A)**

```bat
cd C:\Users\om\Desktop\KafKa\Day-2\Labs\kafka-docker\scripts
kafka-cli.bat console-consumer --bootstrap-server localhost:9092 --topic lab-messages --from-beginning
```

**Producer (Terminal B)** -create topic first if needed:

```bat
kafka-cli.bat topics --bootstrap-server localhost:9092 --create --topic lab-messages --partitions 1 --replication-factor 1
kafka-cli.bat console-producer --bootstrap-server localhost:9092 --topic lab-messages
```

Type messages → read them in Terminal A.

---

## 11. Files in this lab

```text
kafka-docker/
  docker-compose.yml
  scripts/
    start-kafka.bat
    stop-kafka.bat
    kafka-cli.bat
kafka-docker-lab-guide.md    ← this document
```
