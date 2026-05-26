# Day 6 Python Serialization Lab

Python equivalent of [java-serialization-lab](../java-serialization-lab/).

Registry URLs: [SCHEMA-REGISTRY.md](../SCHEMA-REGISTRY.md)

## Infrastructure

```powershell
cd C:\Users\om\Desktop\KafKa\Day-6\confluent-local
docker compose up -d
```

## Setup & run

```powershell
cd C:\Users\om\Desktop\KafKa\Day-6\labs\python-serialization-lab
pip install -r requirements.txt

python lab01_four_formats.py
python lab02_format_benchmark.py

cd ..\scripts
.\create-employees-avro-topic.bat

cd ..\python-serialization-lab
python lab04_avro_producer.py
# http://localhost:8081/subjects/employees-avro-value/versions → [1]

python lab05_avro_consumer.py
# Second run may idle — Ctrl+C Y

python lab06_schema_evolution.py
cd ..\scripts
.\register-schema-v2.bat
# /versions → [1, 2]
```

## Scripts

| Script | Lab |
|--------|-----|
| `lab01_four_formats.py` | 01 |
| `lab02_format_benchmark.py` | 02 |
| `lab04_avro_producer.py` | 04 |
| `lab05_avro_consumer.py` | 05 |
| `lab06_schema_evolution.py` | 06 |

Deliverable: [evolution-notes.md](../lab-06-schema-evolution/evolution-notes.md)
