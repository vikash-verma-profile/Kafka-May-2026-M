"""Capstone Python producer — same role as java CapstoneProducer."""

from __future__ import annotations

import json
import random
import sys

from kafka import KafkaProducer

from config import DEFAULT_BOOTSTRAP


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    topic = sys.argv[2] if len(sys.argv) > 2 else "capstone-orders"
    count = int(sys.argv[3]) if len(sys.argv) > 3 else 100

    producer = KafkaProducer(
        bootstrap_servers=bootstrap,
        acks="all",
        compression_type="lz4",
        linger_ms=5,
        batch_size=65536,
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
        key_serializer=lambda k: k.encode("utf-8"),
    )

    for i in range(count):
        order = {
            "orderId": f"ord-{i}",
            "customerId": f"cust-{random.randint(0, 19)}",
            "region": f"region-{random.randint(0, 3)}",
            "amount": 500 + random.randint(0, 9500),
            "valid": True,
        }
        producer.send(topic, key=order["orderId"], value=order)

    producer.flush()
    producer.close()
    print(f"Produced {count} events to {topic}")


if __name__ == "__main__":
    main()
