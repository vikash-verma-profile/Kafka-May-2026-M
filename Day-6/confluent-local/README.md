# Confluent Local (Docker)

Kafka, Schema Registry, and Control Center for **Day 6** labs 03–06.

> **`docker compose` only here.** Maven/Python: [labs/README.md](../labs/README.md). Registry URLs: [labs/SCHEMA-REGISTRY.md](../labs/SCHEMA-REGISTRY.md).

## Start

```powershell
cd C:\Users\om\Desktop\KafKa\Day-6\confluent-local
docker compose up -d
docker compose ps
```

From `labs`: `cd ..\confluent-local`

## Services

| Service | URL |
|---------|-----|
| Kafka | `localhost:9092` |
| Schema Registry | http://localhost:8081 |
| Control Center | http://localhost:9021 |

## Verify Registry

```powershell
Invoke-RestMethod http://localhost:8081/subjects
Invoke-RestMethod http://localhost:8081/config
```

| When | `/subjects` | `/subjects/employees-avro-value/versions` |
|------|-------------|-------------------------------------------|
| Lab 03 | `[]` | *(subject not created yet)* |
| Lab 04 | `["employees-avro-value"]` | `[1]` |
| Lab 06 + v2 | same | `[1, 2]` |

## Kafka CLI (PowerShell, single line)

```powershell
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list

docker compose exec kafka kafka-topics --create --topic employees-avro --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

Or: `labs\scripts\create-employees-avro-topic.bat`

## Stop

```powershell
docker compose down
```

## Troubleshooting

| Issue | Fix |
|-------|-----|
| `no configuration file provided` | Run commands in `confluent-local` |
| `mvn` no POM | Use `labs\java-serialization-lab` |
| Lab 05 consumer hangs on 2nd run | Offsets committed — normal |
| `employees-avro-value` in browser | Expected subject name — see SCHEMA-REGISTRY.md |
