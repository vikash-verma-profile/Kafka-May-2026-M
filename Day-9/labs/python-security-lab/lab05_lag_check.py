"""
Lab 05 — describe consumer group lag via kafka-python (admin-style check).

Run: python lab05_lag_check.py order-processor
"""

from __future__ import annotations

import sys

from kafka import KafkaAdminClient
from kafka.structs import TopicPartition


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else "localhost:9092"
    group = sys.argv[2] if len(sys.argv) > 2 else "billing-svc"

    admin = KafkaAdminClient(bootstrap_servers=bootstrap)
    consumer = __import__("kafka").KafkaConsumer(bootstrap_servers=bootstrap)
    try:
        offsets = admin.list_consumer_group_offsets(group)
        for tp, offset_meta in offsets.items():
            end = consumer.end_offsets([tp])[tp]
            lag = end - offset_meta.offset
            print(
                f"{group} {tp.topic} p{tp.partition} "
                f"committed={offset_meta.offset} end={end} lag={lag}"
            )
    finally:
        consumer.close()
        admin.close()


if __name__ == "__main__":
    main()
