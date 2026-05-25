# Day 7 Python Stream Processing Lab

Python equivalent of [java-kafka-streams-lab](../java-kafka-streams-lab/).

> **Note:** Apache Kafka Streams is a **Java library**. These Python scripts implement the same **read → process → write** patterns using `kafka-python` consumer/producer loops.

## Setup

```powershell
pip install -r requirements.txt
..\scripts\create-streams-topics.bat
```

## Scripts

| Script | Lab |
|--------|-----|
| `lab01_uppercase_stream.py` | 01-uppercase transform |
| `lab02_word_count.py` | 02-word count |
| `lab03_filter_aggregate.py` | 03-filter + aggregate |
| `lab04_order_pipeline.py` | 04-mini project |
| `order_producer.py` | sample data |

## Example

```powershell
# Terminal 1
python lab01_uppercase_stream.py

# Terminal 2
kafka-console-producer --topic streams-input --bootstrap-server localhost:9092
```
