# Lab 04 — Elasticsearch & Kibana (Docker)

Runs Elasticsearch and Kibana for the Kafka Connect Elasticsearch sink lab.

| Service | URL |
| ------- | --- |
| Elasticsearch | http://localhost:9200 |
| Kibana | http://localhost:5601 |

Security is **disabled** for local labs (matches `connection.url": "http://localhost:9200"` in `es-orders-sink.json`).

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Windows) — **must be running** before `docker compose up`
- At least **4 GB RAM** free for Docker (ES + Kibana)

If compose fails with `dockerDesktopLinuxEngine` / pipe not found, open Docker Desktop, wait until it says **Running**, then retry.

## Start

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs\lab-04-elasticsearch-sink\docker
docker compose up -d
```

**First run:** Compose may download large images (`docker.elastic.co/elasticsearch/kibana` 8.15.3) — allow several minutes on a slow link.

If you see `Container lab04-elasticsearch Error dependency` and Kibana **Created** but not started, wait until ES is **healthy**, then run `docker compose up -d` again.

Wait until healthy (first start often **1–3 minutes**):

```powershell
docker compose ps
curl.exe http://localhost:9200
```

Kibana starts **after** Elasticsearch is healthy. Allow **2–5 minutes** more, then:

```powershell
curl.exe http://localhost:5601/api/status
```

Open http://127.0.0.1:5601 if `localhost` hangs. Welcome / “Explore on my own” = Kibana is ready.

## Stop

```powershell
docker compose down
```

Remove data volume (reset cluster):

```powershell
docker compose down -v
```

## Verify (Lab 04)

After deploying `es-orders-sink` and producing to `orders-topic`:

```powershell
curl.exe "http://localhost:9200/orders-topic/_search?pretty"
```

Kibana: open http://localhost:5601 → **Discover** → create data view `orders-topic*`.

## Troubleshooting

| Issue | Fix |
| ----- | --- |
| `dockerDesktopLinuxEngine` / cannot connect to Docker | Start **Docker Desktop**; wait until engine is running |
| `unable to get image` during `compose up` | Docker engine not running — start Desktop, retry |
| `elasticsearch` **unhealthy** | Wait 2–3 min; `docker compose logs elasticsearch`; `docker compose restart elasticsearch` |
| `kibana` failed — `elasticsearch is unhealthy` | Fix ES first (`docker compose ps` → healthy); then `docker compose up -d kibana` |
| Container exits / OOM | Increase Docker memory to 4 GB+ in Docker Desktop settings |
| Port 9200 in use | Stop other ES instances or change host port in `docker-compose.yml` |
| Kibana blank / curl empty reply | Still starting — wait 2–5 min; check `docker compose logs kibana` |
| Kibana “Explore on my own” | Ready — proceed to Lab 04 Step 5 (data view) |
| Connect cannot reach ES | Use `http://localhost:9200` (host) when Connect runs on Windows host, not in Docker |

## Observations

- Connect and Kafka run on the **host**; only ES/Kibana run in Docker — always use **localhost:9200** in connector config.
- Verified: `curl.exe http://localhost:9200` returns cluster info when ES is healthy.
- Kibana `curl.exe http://localhost:5601/api/status` may fail until startup completes; browser UI can still load later.

## Files

- `docker-compose.yml` — official Elastic images 8.15.3 (no custom Dockerfile required)
