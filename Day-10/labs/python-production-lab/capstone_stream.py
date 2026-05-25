"""Capstone Python stream — filter valid orders to capstone-processed."""

from __future__ import annotations

import json
import sys

from kafka import KafkaConsumer, KafkaProducer

from config import DEFAULT_BOOTSTRAP


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP

    consumer = KafkaConsumer(
        "capstone-orders",
        bootstrap_servers=bootstrap,
        group_id="py-capstone-streams",
        auto_offset_reset="earliest",
        value_deserializer=lambda v: json.loads(v.decode("utf-8")),
    )
    producer = KafkaProducer(
        bootstrap_servers=bootstrap,
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
        key_serializer=lambda k: k.encode("utf-8"),
    )

    print("capstone-orders -> capstone-processed (Ctrl+C to stop)")
    try:
        for msg in consumer:
            order = msg.value
            if order.get("valid") and order.get("amount", 0) > 0:
                producer.send("capstone-processed", key=msg.key, value=order)
                producer.flush()
                print(f"processed {order['orderId']}")
    except KeyboardInterrupt:
        pass
    finally:
        consumer.close()
        producer.close()


if __name__ == "__main__":
    main()
