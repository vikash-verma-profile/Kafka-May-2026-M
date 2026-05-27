# Lab 02 — Configure MySQL JDBC Source Connector

**Objective:** Stream new rows from MySQL into Kafka using the Confluent JDBC Source connector.

From **Kafka_Connect_API.pptx** — Slides 16–17.

**Tested with:** Java 17, Kafka 4.2, MySQL 8, Kafka Connect **standalone** on Windows.

---

## Prerequisites

- Kafka cluster (single broker or KRaft multi-broker) reachable on `localhost:9092` (and `9094`, `9095` if using a 3-broker lab cluster)
- **Kafka Connect** standalone on `http://localhost:8083`
- MySQL 8 with database `ordersdb`, table `orders` with monotonic `id`
- Confluent JDBC connector + **MySQL Connector/J** JAR in Connect `plugin.path`

---

## Lab layout (Windows paths)

| Item | Example path |
| ---- | ------------- |
| Kafka home | `C:\kafka-bin\kafka_2.13-4.2.0` |
| Connect config | `config\connect-standalone.properties` |
| Plugins root | `plugins\` (not `plugins\...\lib`) |
| JDBC plugin | `plugins\confluent-jdbc\` |
| Connector JARs | `plugins\confluent-jdbc\lib\` |
| Lab configs | `labs\configs\mysql-orders-source.json` |
| Deploy scripts | `labs\scripts\deploy-connector.bat` |

---

## Step 0 — Start Kafka (if not running)

If you use a local KRaft cluster script:

```bat
C:\kafka-bin\kafka_2.13-4.2.0\start-kafka-cluster.bat
```

Wait until each broker window shows **Kafka Server started**.

Verify brokers:

```bat
cd /d C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092,localhost:9094,localhost:9095 --list
```

---

## Step 1 — Configure `connect-standalone.properties`

Edit `C:\kafka-bin\kafka_2.13-4.2.0\config\connect-standalone.properties`.

**Use forward slashes (`/`) in paths** — backslashes (`\`) are treated as escape characters and break `plugin.path`.

```properties
bootstrap.servers=localhost:9092,localhost:9094,localhost:9095

key.converter=org.apache.kafka.connect.json.JsonConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
key.converter.schemas.enable=false
value.converter.schemas.enable=false

offset.storage.file.filename=C:/kafka-data/connect-offsets/connect.offsets
offset.flush.interval.ms=10000

plugin.path=C:/kafka-bin/kafka_2.13-4.2.0/plugins
listeners=http://localhost:8083
```

Create the offsets folder once:

```bat
mkdir C:\kafka-data\connect-offsets
```

### `plugin.path` rules (important)

| Correct | Wrong |
| ------- | ----- |
| `plugin.path=C:/kafka-bin/kafka_2.13-4.2.0/plugins` | `...\plugins\confluent-jdbc\lib` |
| Parent `plugins` folder | A single `.jar` file path |
| Forward slashes | `C:\...` backslashes only |

Connect discovers plugins under `plugins\confluent-jdbc\` automatically (`manifest.json` + `lib\*.jar`).

---

## Step 2 — Install JDBC plugin + MySQL driver

### 2a) Confluent JDBC connector (Hub)

1. Download [Kafka Connect JDBC](https://www.confluent.io/hub/confluentinc/kafka-connect-jdbc) (e.g. 10.9.3).
2. Extract so you have:

```
plugins\confluent-jdbc\
  manifest.json
  lib\
    kafka-connect-jdbc-10.9.3.jar
    (other dependency jars from the package)
```

### 2b) MySQL Connector/J (separate download)

1. Go to https://dev.mysql.com/downloads/connector/j/
2. **Select Operating System:** **Platform Independent**
3. Download **ZIP Archive** (not Linux/Windows OS packages).
4. Copy `mysql-connector-j-9.7.0.jar` (or 8.4.x) into:

```
C:\kafka-bin\kafka_2.13-4.2.0\plugins\confluent-jdbc\lib\
```

You need **both** JARs in `lib\`:

- `kafka-connect-jdbc-10.9.3.jar` — connector
- `mysql-connector-j-9.7.0.jar` — MySQL driver (Postgres/MSSQL JARs do **not** work for MySQL)

### 2c) Restart Connect

```bat
cd /d C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\connect-standalone.bat config\connect-standalone.properties
```

Verify JDBC plugin loaded:

```powershell
Invoke-RestMethod -Uri http://localhost:8083/connector-plugins |
  Where-Object { $_.class -like '*jdbc*' }
```

Expected: `io.confluent.connect.jdbc.JdbcSourceConnector`

---

## Step 3 — Prepare MySQL

Run init SQL (from `labs\sql`):

```bat
cd /d C:\Users\om\Desktop\KafKa\Day-8\labs\sql
mysql -u root -p < init-ordersdb.sql
```

Or in **MySQL Workbench** (localhost:3306, user `root`):

```sql
CREATE DATABASE IF NOT EXISTS ordersdb;
USE ordersdb;
CREATE TABLE IF NOT EXISTS orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL,
  order_total DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO orders (customer_id, order_total) VALUES (1, 99.50), (2, 250.00);
```

### Database credentials for Connect

Set in [configs/mysql-orders-source.json](../configs/mysql-orders-source.json) — **not** in `connect-standalone.properties`:

```json
"connection.url": "jdbc:mysql://localhost:3306/ordersdb?useSSL=false&allowPublicKeyRetrieval=true",
"connection.user": "root",
"connection.password": "YOUR_MYSQL_ROOT_PASSWORD"
```

Use the same password that works in MySQL Workbench.

Optional dedicated user:

```sql
CREATE USER 'kafka'@'localhost' IDENTIFIED BY 'kafka123';
GRANT SELECT ON ordersdb.* TO 'kafka'@'localhost';
FLUSH PRIVILEGES;
```

---

## Step 4 — Connector JSON (`mysql-orders-source.json`)

See [configs/mysql-orders-source.json](../configs/mysql-orders-source.json).

Topic name: **`mysql-orders`** (`topic.prefix` + table name `orders`).

---

## Step 5 — Deploy via REST API

From **PowerShell** (note `.\scripts\` — run from `labs` folder):

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs

# If connector already exists (409), delete first:
curl.exe -X DELETE http://localhost:8083/connectors/mysql-orders-source

.\scripts\deploy-connector.bat .\configs\mysql-orders-source.json http://localhost:8083
.\scripts\connect-status.bat mysql-orders-source http://localhost:8083
```

**cmd:**

```bat
cd /d C:\Users\om\Desktop\KafKa\Day-8\labs\scripts
deploy-connector.bat ..\configs\mysql-orders-source.json http://localhost:8083
connect-status.bat mysql-orders-source http://localhost:8083
```

**Browser checks:**

- `http://localhost:8083/connectors` → `["mysql-orders-source"]`
- `http://localhost:8083/connectors/mysql-orders-source/status` → `"state": "RUNNING"`

**Python** ([python-connect-lab](../../python-connect-lab/)):

```powershell
python deploy_connector.py ..\configs\mysql-orders-source.json
python connect_status.py http://localhost:8083 mysql-orders-source
```

---

## Step 6 — Insert rows and consume

```sql
INSERT INTO orders (customer_id, order_total) VALUES (3, 1200.00);
```

```bat
cd /d C:\kafka-bin\kafka_2.13-4.2.0
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic mysql-orders --from-beginning
```

**Expected:** JSON records for each row within `poll.interval.ms` (5000 ms).

---

## Checkpoint

- [ ] `connector-plugins` lists `JdbcSourceConnector`
- [ ] `mysql-connector-j-*.jar` in `plugins\confluent-jdbc\lib\`
- [ ] Connector **RUNNING** at `/connectors/mysql-orders-source/status`
- [ ] Topic `mysql-orders` has data
- [ ] New INSERTs appear in the consumer

---

## Standalone vs distributed (Lab 02)

| Mode | When to use |
| ---- | ----------- |
| **Standalone** | Local labs; one worker; REST on 8083 — **recommended for this course** |
| **Distributed** | Production; offsets in `connect-offsets` topic |

Connector JSON and deploy commands are the **same** for both modes.

---

## Limitations (slide 14)

JDBC **incrementing** mode does not capture **DELETEs**. For full CDC use Debezium (Lab 06).

---

## Troubleshooting

| Issue | Fix |
| ----- | --- |
| `Failed to find ... JdbcSourceConnector` (400) | JDBC plugin not loaded. Fix `plugin.path` to `C:/.../plugins` with **forward slashes**. Restart Connect. Check `/connector-plugins` for JDBC. |
| `Could not get listing for plugin path: C:kafka-bin...` | Backslashes in `plugin.path` ate `\k`, `\p`. Use `C:/kafka-bin/.../plugins`. |
| `No suitable driver found for jdbc:mysql://...` | Add `mysql-connector-j-*.jar` to `plugins\confluent-jdbc\lib\`. Restart Connect. Delete + redeploy connector. |
| `Failed to connect to and describe Kafka cluster` / `listNodes` timeout | Start Kafka first. Test: `kafka-topics.bat --bootstrap-server localhost:9092,localhost:9094,localhost:9095 --list`. Match `bootstrap.servers` in Connect config. |
| `Request timed out` (500) on deploy | Often MySQL unreachable or missing driver. Check Connect worker log window. |
| `Connector mysql-orders-source already exists` (409) | `curl -X DELETE http://localhost:8083/connectors/mysql-orders-source` then deploy again. |
| `Failed to transition connector ... to STARTED` (500) | Check `/status` trace. Usually wrong MySQL password or missing `ordersdb`. |
| Access denied (MySQL) | Update `connection.user` / `connection.password` in `mysql-orders-source.json`. Grant `SELECT ON ordersdb.*`. |
| `deploy-connector.bat` not recognized (PowerShell) | Use `.\scripts\deploy-connector.bat` from `labs` folder. |
| Connect REST `[]` but deploy fails | JDBC or MySQL issue — not REST. Follow rows above. |

### Useful commands

```powershell
# List connectors
Invoke-RestMethod -Uri http://localhost:8083/connectors

# JDBC plugins
Invoke-RestMethod -Uri http://localhost:8083/connector-plugins | Where-Object { $_.class -like '*jdbc*' }

# Verify MySQL JAR
dir C:\kafka-bin\kafka_2.13-4.2.0\plugins\confluent-jdbc\lib\mysql*

# Connect log (Windows)
type C:\kafka-bin\kafka_2.13-4.2.0\logs\connect.log | Select-Object -Last 50
```
