"""
Publish sample messages to Kafka.

Run:
    python simple_producer.py
    python simple_producer.py localhost:9092 lab-messages 3
"""

import sys
import time

from kafka import KafkaProducer
from kafka.errors import KafkaError

from config import DEFAULT_BOOTSTRAP, DEFAULT_MESSAGE_COUNT, DEFAULT_TOPIC


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    topic = sys.argv[2] if len(sys.argv) > 2 else DEFAULT_TOPIC
    message_count = int(sys.argv[3]) if len(sys.argv) > 3 else DEFAULT_MESSAGE_COUNT

    print(f"Connecting to {bootstrap}, topic={topic}, messages={message_count}")

    producer = KafkaProducer(
        bootstrap_servers=bootstrap,
        value_serializer=lambda v: v.encode("utf-8"),
        client_id="python-lab-producer",
        acks="all",
    )

    try:
        for i in range(1, message_count + 1):
            value = f"Hello from Python producer - message {i}"
            future = producer.send(topic, value=value)

            try:
                metadata = future.get(timeout=10)
                print(
                    f'Sent: "{value}" -> '
                    f"partition={metadata.partition} offset={metadata.offset}"
                )
            except KafkaError as exc:
                print(f"Failed to send message {i}: {exc}", file=sys.stderr)
                sys.exit(1)

        producer.flush()
    finally:
        producer.close()

    print("Producer finished.")


if __name__ == "__main__":
    main()
