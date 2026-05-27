"""Consume up to N messages from a topic (Lab 06 CDC verification)."""

from __future__ import annotations

import sys

from kafka import KafkaConsumer

from config import DEFAULT_BOOTSTRAP


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    topic = sys.argv[2] if len(sys.argv) > 2 else "mysql-orders"
    max_messages = int(sys.argv[3]) if len(sys.argv) > 3 else 20

    consumer = KafkaConsumer(
        topic,
        bootstrap_servers=bootstrap,
        group_id="py-connect-verify",
        auto_offset_reset="earliest",
        consumer_timeout_ms=5000,
        value_deserializer=lambda v: v.decode("utf-8", errors="replace"),
    )

    count = 0
    for msg in consumer:
        print(f"[{msg.partition}@{msg.offset}] {msg.value[:200]}")
        count += 1
        if count >= max_messages:
            break

    consumer.close()
    print(f"Read {count} messages from {topic}")


if __name__ == "__main__":
    main()
