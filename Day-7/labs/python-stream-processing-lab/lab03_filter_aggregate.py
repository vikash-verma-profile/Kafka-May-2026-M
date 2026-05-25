"""
Lab 03-filter valid orders and aggregate totals per customer.

Run: python lab03_filter_aggregate.py
"""

from __future__ import annotations

import json
import sys
from collections import defaultdict

from kafka import KafkaConsumer, KafkaProducer

from config import DEFAULT_BOOTSTRAP

INPUT_TOPIC = "orders-raw"
METRICS_TOPIC = "orders-metrics"
CRITICAL_TOPIC = "orders-critical"


def parse_amount(payload: dict) -> float:
    return float(payload.get("amount", 0))


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    totals: dict[str, float] = defaultdict(float)

    consumer = KafkaConsumer(
        INPUT_TOPIC,
        bootstrap_servers=bootstrap,
        group_id="py-order-filter-aggregate",
        auto_offset_reset="earliest",
        enable_auto_commit=True,
        value_deserializer=lambda v: json.loads(v.decode("utf-8")),
    )
    producer = KafkaProducer(
        bootstrap_servers=bootstrap,
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
        key_serializer=lambda k: k.encode("utf-8"),
    )

    print(f"Filtering {INPUT_TOPIC} (Ctrl+C to stop)")
    try:
        for msg in consumer:
            order = msg.value
            if not order.get("valid", True) or parse_amount(order) <= 0:
                continue
            if parse_amount(order) >= 5000:
                producer.send(CRITICAL_TOPIC, key=msg.key, value=order)
            customer = order.get("customerId", "unknown")
            totals[customer] += parse_amount(order)
            metric = {"customerId": customer, "total": round(totals[customer], 2)}
            producer.send(METRICS_TOPIC, key=customer, value=metric)
            producer.flush()
            print(metric)
    except KeyboardInterrupt:
        pass
    finally:
        consumer.close()
        producer.close()


if __name__ == "__main__":
    main()
