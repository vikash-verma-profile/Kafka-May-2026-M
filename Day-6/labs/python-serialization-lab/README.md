# Day 6 Python Serialization Lab

Python equivalent of [java-serialization-lab](../java-serialization-lab/).

## Setup

```powershell
cd Day-6\labs\python-serialization-lab
pip install -r requirements.txt
```

Schema Registry on `http://localhost:8081` required for Labs 04–06.

## Scripts

| Script | Lab |
|--------|-----|
| `lab01_four_formats.py` | 01 — JSON, XML, Avro, Protobuf |
| `lab02_format_benchmark.py` | 02 — size & time benchmark |
| `lab04_avro_producer.py` | 04 — Avro producer + Registry |
| `lab05_avro_consumer.py` | 05 — Avro consumer |
| `lab06_schema_evolution.py` | 06 — schema v2 |

## Run

```powershell
python lab01_four_formats.py
..\scripts\create-employees-avro-topic.bat
python lab04_avro_producer.py
python lab05_avro_consumer.py
```
