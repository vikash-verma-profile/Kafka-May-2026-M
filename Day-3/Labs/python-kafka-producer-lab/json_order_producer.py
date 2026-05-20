"""
Lab 11- Structured JSON order events (Python).

Run:
    python json_order_producer.py
"""

import json
import sys

from kafka import KafkaProducer
from kafka.errors import KafkaError

from config import DEFAULT_BOOTSTRAP, DEFAULT_TOPIC


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    topic = sys.argv[2] if len(sys.argv) > 2 else DEFAULT_TOPIC
    count = int(sys.argv[3]) if len(sys.argv) > 3 else 5

    producer = KafkaProducer(
        bootstrap_servers=bootstrap,
        key_serializer=lambda k: k.encode("utf-8") if k else None,
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
        acks="all",
    )

    customers = ["Vikash", "John", "Priya", "Amit"]

    try:
        for i in range(1, count + 1):
            order_id = 1000 + i
            event = {
                "orderId": order_id,
                "customer": customers[i % len(customers)],
                "amount": 1000 + i * 500,
                "payment": "UPI",
            }
            key = str(order_id)
            future = producer.send(topic, key=key, value=event)
            try:
                metadata = future.get(timeout=10)
                print(
                    f"Sent key={key} {event} -> "
                    f"partition={metadata.partition} offset={metadata.offset}"
                )
            except KafkaError as exc:
                print(f"Failed: {exc}", file=sys.stderr)
                sys.exit(1)

        producer.flush()
    finally:
        producer.close()

    print("JSON order events sent.")


if __name__ == "__main__":
    main()
