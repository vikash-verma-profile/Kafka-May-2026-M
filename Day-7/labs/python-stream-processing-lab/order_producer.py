"""Sample order producer for Day 7 Python labs."""

from __future__ import annotations

import json
import random
import sys

from kafka import KafkaProducer

from config import DEFAULT_BOOTSTRAP

REGIONS = ["north", "south", "east", "west"]


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    topic = sys.argv[2] if len(sys.argv) > 2 else "orders"
    count = int(sys.argv[3]) if len(sys.argv) > 3 else 50

    producer = KafkaProducer(
        bootstrap_servers=bootstrap,
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
        key_serializer=lambda k: k.encode("utf-8"),
    )

    for i in range(count):
        valid = random.random() > 0.1
        order = {
            "orderId": f"o-{i}",
            "customerId": f"c-{random.randint(0, 4)}",
            "region": random.choice(REGIONS),
            "amount": random.randint(1000, 9000) if valid else -1,
            "valid": valid,
        }
        producer.send(topic, key=order["orderId"], value=order)

    producer.flush()
    producer.close()
    print(f"Produced {count} orders to {topic}")


if __name__ == "__main__":
    main()
