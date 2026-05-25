"""
Lab 04 — produce Avro Employee records with Confluent Schema Registry.

Run:
    python lab04_avro_producer.py
    python lab04_avro_producer.py localhost:9092 http://localhost:8081
"""

from __future__ import annotations

import sys
from pathlib import Path

from confluent_kafka import Producer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroSerializer
from confluent_kafka.serialization import SerializationContext, MessageField

from config import DEFAULT_BOOTSTRAP, DEFAULT_SCHEMA_REGISTRY, EMPLOYEES_AVRO_TOPIC

SCHEMA_PATH = Path(__file__).parent / "schemas" / "employee.avsc"


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    registry_url = sys.argv[2] if len(sys.argv) > 2 else DEFAULT_SCHEMA_REGISTRY
    schema_str = SCHEMA_PATH.read_text(encoding="utf-8")

    registry = SchemaRegistryClient({"url": registry_url})
    serializer = AvroSerializer(registry, schema_str)

    producer = Producer({"bootstrap.servers": bootstrap})

    def on_delivery(err, msg):
        if err:
            print(f"Delivery failed: {err}")
        else:
            print(f"Sent to {msg.topic()} [{msg.partition()}] @ {msg.offset()}")

    for i in range(1, 11):
        record = {
            "id": i,
            "name": f"Employee-{i}",
            "dept": "Engineering",
            "salary": 50_000.0 + i * 1_000,
        }
        producer.produce(
            topic=EMPLOYEES_AVRO_TOPIC,
            key=str(i),
            value=serializer(record, SerializationContext(EMPLOYEES_AVRO_TOPIC, MessageField.VALUE)),
            on_delivery=on_delivery,
        )

    producer.flush()
    print(f"Done. Subjects: curl {registry_url}/subjects")


if __name__ == "__main__":
    main()
