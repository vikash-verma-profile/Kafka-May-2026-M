"""
Lab 01-produce with SASL/SCRAM (PLAIN or SCRAM listener).

Requires client-scram.properties and SCRAM user on broker.

Run:
    python lab01_scram_producer.py
    python lab01_scram_producer.py localhost:9093 orders
"""

from __future__ import annotations

import sys

from kafka import KafkaProducer
from kafka.errors import KafkaError

from config import DEFAULT_BOOTSTRAP


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    topic = sys.argv[2] if len(sys.argv) > 2 else "orders"
    username = sys.argv[3] if len(sys.argv) > 3 else "alice"
    password = sys.argv[4] if len(sys.argv) > 4 else "secret"

    producer = KafkaProducer(
        bootstrap_servers=bootstrap,
        security_protocol="SASL_PLAINTEXT",
        sasl_mechanism="SCRAM-SHA-512",
        sasl_plain_username=username,
        sasl_plain_password=password,
        value_serializer=lambda v: v.encode("utf-8"),
    )

    try:
        future = producer.send(topic, value="hello-from-python-scram")
        meta = future.get(timeout=10)
        print(f"Sent to {meta.topic} partition={meta.partition} offset={meta.offset}")
    except KafkaError as exc:
        print(f"Failed: {exc}")
        sys.exit(1)
    finally:
        producer.close()


if __name__ == "__main__":
    main()
