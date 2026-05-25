"""
Lab 06 — produce record with evolved schema (email field).

Register employee_v2.avsc via REST or auto-register, then run Lab 05 consumer.

Run: python lab06_schema_evolution.py
"""

from __future__ import annotations

import sys
from pathlib import Path

from confluent_kafka import Producer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroSerializer
from confluent_kafka.serialization import SerializationContext, MessageField

from config import DEFAULT_BOOTSTRAP, DEFAULT_SCHEMA_REGISTRY, EMPLOYEES_AVRO_TOPIC

SCHEMA_V2 = Path(__file__).parent / "schemas" / "employee_v2.avsc"


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    registry_url = sys.argv[2] if len(sys.argv) > 2 else DEFAULT_SCHEMA_REGISTRY
    schema_str = SCHEMA_V2.read_text(encoding="utf-8")

    registry = SchemaRegistryClient({"url": registry_url})
    serializer = AvroSerializer(registry, schema_str)
    producer = Producer({"bootstrap.servers": bootstrap})

    record = {
        "id": 99,
        "name": "Evolved-Employee",
        "dept": "Sales",
        "salary": 60_000.0,
        "email": "evolved@example.com",
    }
    producer.produce(
        topic=EMPLOYEES_AVRO_TOPIC,
        key="99",
        value=serializer(record, SerializationContext(EMPLOYEES_AVRO_TOPIC, MessageField.VALUE)),
    )
    producer.flush()
    print(f"Sent evolved record. Versions: curl {registry_url}/subjects/employees-avro-value/versions")


if __name__ == "__main__":
    main()
