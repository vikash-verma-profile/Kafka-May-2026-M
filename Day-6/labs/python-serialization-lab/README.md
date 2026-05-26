# Day 6 Python Serialization Lab

Python equivalent of [java-serialization-lab](../java-serialization-lab/).

## Infrastructure (Labs 04–06)

```powershell
cd Day-6\confluent-local
docker compose up -d
```

See [confluent-local/README.md](../../confluent-local/README.md). Registry: `http://localhost:8081`, broker: `localhost:9092`.

## Setup

```powershell
cd Day-6\labs\python-serialization-lab
pip install -r requirements.txt
```

## Scripts

| Script | Lab |
|--------|-----|
| `lab01_four_formats.py` | 01 — JSON, XML, Avro, Protobuf |
| `lab02_format_benchmark.py` | 02 — size & time benchmark |
| `lab04_avro_producer.py` | 04 — Avro producer + Registry |
| `lab05_avro_consumer.py` | 05 — Avro consumer |
| `lab06_schema_evolution.py` | 06 — schema v2 |

Labs 01–02 need no broker. Labs 04–06 require the Docker stack running.

## Run

```powershell
python lab01_four_formats.py
python lab02_format_benchmark.py

cd ..\scripts
.\create-employees-avro-topic.bat
cd ..\python-serialization-lab

python lab04_avro_producer.py
python lab05_avro_consumer.py
python lab06_schema_evolution.py
```
