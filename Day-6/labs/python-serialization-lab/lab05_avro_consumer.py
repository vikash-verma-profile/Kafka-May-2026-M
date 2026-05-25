"""
Lab 05 — consume Avro records from employees-avro.

Run: python lab05_avro_consumer.py
"""

from __future__ import annotations

import sys
from pathlib import Path

from confluent_kafka import Consumer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroDeserializer
from confluent_kafka.serialization import SerializationContext, MessageField

from config import (
    AVRO_CONSUMER_GROUP,
    DEFAULT_BOOTSTRAP,
    DEFAULT_SCHEMA_REGISTRY,
    EMPLOYEES_AVRO_TOPIC,
)

SCHEMA_PATH = Path(__file__).parent / "schemas" / "employee.avsc"


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    registry_url = sys.argv[2] if len(sys.argv) > 2 else DEFAULT_SCHEMA_REGISTRY
    schema_str = SCHEMA_PATH.read_text(encoding="utf-8")

    registry = SchemaRegistryClient({"url": registry_url})
    deserializer = AvroDeserializer(registry, schema_str)

    consumer = Consumer(
        {
            "bootstrap.servers": bootstrap,
            "group.id": AVRO_CONSUMER_GROUP,
            "auto.offset.reset": "earliest",
            "enable.auto.commit": False,
        }
    )
    consumer.subscribe([EMPLOYEES_AVRO_TOPIC])

    count = 0
    try:
        while count < 10:
            msg = consumer.poll(2.0)
            if msg is None:
                continue
            if msg.error():
                print(msg.error())
                continue
            record = deserializer(
                msg.value(),
                SerializationContext(EMPLOYEES_AVRO_TOPIC, MessageField.VALUE),
            )
            print(
                f"partition={msg.partition()} offset={msg.offset()} "
                f"id={record['id']} name={record['name']} dept={record['dept']} salary={record['salary']}"
            )
            count += 1
        consumer.commit()
    finally:
        consumer.close()

    print(f"Read {count} records.")


if __name__ == "__main__":
    main()
