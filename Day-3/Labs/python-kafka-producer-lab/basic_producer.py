"""
Lab 10- Python Kafka producer (JSON values).

Run:
    pip install -r requirements.txt
    python basic_producer.py
    python basic_producer.py localhost:9092 orders-topic 5
"""

import json
import sys

from kafka import KafkaProducer
from kafka.errors import KafkaError

from config import DEFAULT_BOOTSTRAP, DEFAULT_MESSAGE_COUNT, DEFAULT_TOPIC


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    topic = sys.argv[2] if len(sys.argv) > 2 else DEFAULT_TOPIC
    count = int(sys.argv[3]) if len(sys.argv) > 3 else DEFAULT_MESSAGE_COUNT

    print(f"Connecting to {bootstrap}, topic={topic}, messages={count}")

    producer = KafkaProducer(
        bootstrap_servers=bootstrap,
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
        acks="all",
        client_id="day3-python-producer",
    )

    try:
        for i in range(1, count + 1):
            event = {"orderId": i, "status": "CREATED"}
            future = producer.send(topic, value=event)
            try:
                metadata = future.get(timeout=10)
                print(
                    f"Sent {event} -> partition={metadata.partition} offset={metadata.offset}"
                )
            except KafkaError as exc:
                print(f"Failed: {exc}", file=sys.stderr)
                sys.exit(1)

        producer.flush()
    finally:
        producer.close()

    print("Messages Sent")


if __name__ == "__main__":
    main()
