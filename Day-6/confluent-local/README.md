# Confluent Local (Docker)

Kafka, Schema Registry, and Control Center for **Day 6** labs 03–06.

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) running on Windows

## Start

```powershell
cd Day-6\confluent-local
docker compose up -d
```

Wait until all containers are **Up** (about 30–60 seconds on first pull):

```powershell
docker compose ps
```

## Services

| Service | Host URL | Used in labs |
|---------|----------|--------------|
| Kafka | `localhost:9092` | 04–06 |
| Schema Registry | http://localhost:8081 | 03–06 |
| Control Center | http://localhost:9021 | Optional UI |
| Zookeeper | `localhost:2181` | Internal |

## Verify

From `Day-6\labs\scripts`:

```powershell
.\verify-schema-registry.bat
```

Or in PowerShell:

```powershell
Invoke-RestMethod http://localhost:8081/subjects   # expect empty list: []
Invoke-RestMethod http://localhost:8081/config     # compatibilityLevel: BACKWARD
```

List Kafka topics (via Docker):

```powershell
cd Day-6\confluent-local
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

The internal `_schemas` topic appears after the first schema is registered (Lab 04).

## Stop

```powershell
cd Day-6\confluent-local
docker compose down
```

Data is removed unless you add a volume in `docker-compose.yml`. For class work, `down` and `up -d` is usually enough.

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Port 9092 or 8081 in use | Stop other Kafka/Registry instances or change ports in `docker-compose.yml` |
| `Connection refused` from lab apps | Run `docker compose ps` — all services must be Up |
| Producer cannot reach broker | Use `localhost:9092`, not `kafka:29092` (host apps vs in-network) |
| Jenkins on 8080 conflicts | Jenkins uses 8080; Schema Registry uses **8081** — no conflict |
