"""
Lab 04-mini order pipeline: invalid branch, high-value alerts, regional totals.

Run: python lab04_order_pipeline.py
"""

from __future__ import annotations

import json
import sys
from collections import defaultdict

from kafka import KafkaConsumer, KafkaProducer

from config import DEFAULT_BOOTSTRAP

INPUT_TOPIC = "orders"


def valid_order(order: dict) -> bool:
    return bool(order.get("valid", True)) and order.get("amount", 0) > 0 and order.get("region")


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    region_totals: dict[str, float] = defaultdict(float)

    consumer = KafkaConsumer(
        INPUT_TOPIC,
        bootstrap_servers=bootstrap,
        group_id="py-order-pipeline-app",
        auto_offset_reset="earliest",
        enable_auto_commit=True,
        value_deserializer=lambda v: json.loads(v.decode("utf-8")),
    )
    producer = KafkaProducer(
        bootstrap_servers=bootstrap,
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
        key_serializer=lambda k: k.encode("utf-8") if k else None,
    )

    print(f"Order pipeline on {INPUT_TOPIC} (Ctrl+C to stop)")
    try:
        for msg in consumer:
            order = msg.value
            key = msg.key.decode("utf-8") if msg.key else order.get("orderId", "na")

            if not valid_order(order):
                producer.send("invalid-orders", key=key, value=order)
                continue

            amount = float(order["amount"])
            if amount >= 5000:
                producer.send("high-value-orders", key=key, value=order)
                print(f"ALERT high-value order key={key} amount={amount}")

            region = order["region"]
            region_totals[region] += amount
            analytics = {
                "region": region,
                "totalSales": round(region_totals[region], 2),
            }
            producer.send("order-analytics", key=region, value=analytics)
            producer.flush()
    except KeyboardInterrupt:
        pass
    finally:
        consumer.close()
        producer.close()


if __name__ == "__main__":
    main()
