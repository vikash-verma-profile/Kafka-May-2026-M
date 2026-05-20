# Lab 10- Python Kafka Producer

**Objective:** Publish JSON events using `kafka-python`.

## Code

`python-kafka-producer-lab/basic_producer.py`

## Step 1- Create virtual environment (recommended)

```powershell
cd Day-3\Labs\python-kafka-producer-lab
python -m venv .venv
.\.venv\Scripts\activate
```

## Step 2- Install dependency

```powershell
pip install -r requirements.txt
```

## Step 3- Run the producer

```powershell
python basic_producer.py
```

Or from `Labs`:

```powershell
run-python-basic-producer.bat
```

**Expected output:**

```text
Messages Sent
```

Each line shows partition and offset.

## Step 4- Verify with console consumer

```powershell
Labs\scripts\run-console-consumer.bat
```

Example message body:

```json
{"orderId": 1, "status": "CREATED"}
```

## Step 5- Custom parameters

```powershell
python basic_producer.py localhost:9092 orders-topic 10
```

## Compare with Java (Lab 03)

| Java | Python |
|------|--------|
| `KafkaProducer` | `KafkaProducer` (kafka-python) |
| `StringSerializer` | `value_serializer` lambda |
| `ProducerRecord` | `producer.send(topic, value=...)` |

## Next lab

→ [Lab 11- Structured JSON](../lab-11-structured-json/README.md)
