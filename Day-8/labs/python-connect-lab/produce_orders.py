"""Produce JSON orders to Kafka for Connect sink/ES labs."""

from __future__ import annotations

import json
import sys

from kafka import KafkaProducer

from config import DEFAULT_BOOTSTRAP, KAFKA_API_VERSION, parse_bootstrap


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    topic = sys.argv[2] if len(sys.argv) > 2 else "orders-topic"
    count = int(sys.argv[3]) if len(sys.argv) > 3 else 10

    producer = KafkaProducer(
        bootstrap_servers=parse_bootstrap(bootstrap),
        api_version=KAFKA_API_VERSION,
        # kafka-python + multi-bootstrap can hit 60s metadata timeout on Windows; 9092 alone is enough
        request_timeout_ms=120_000,
        max_block_ms=120_000,
        key_serializer=lambda k: k.encode("utf-8"),
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
    )

    for i in range(count):
        order = {"orderId": f"o-{i}", "customer": f"user-{i}", "total": 1000 + i * 100}
        producer.send(topic, key=order["orderId"], value=order)

    producer.flush()
    producer.close()
    print(f"Produced {count} records to {topic}")


if __name__ == "__main__":
    main()
