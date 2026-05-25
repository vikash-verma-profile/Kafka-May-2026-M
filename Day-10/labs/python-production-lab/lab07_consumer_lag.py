"""
Lab 07 — show consumer group lag per partition.

Run: python lab07_consumer_lag.py localhost:9092 order-processor
"""

from __future__ import annotations

import sys

from kafka import KafkaAdminClient, KafkaConsumer
from kafka.structs import TopicPartition

from config import DEFAULT_BOOTSTRAP


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    group = sys.argv[2] if len(sys.argv) > 2 else "order-processor"

    admin = KafkaAdminClient(bootstrap_servers=bootstrap)
    consumer = KafkaConsumer(bootstrap_servers=bootstrap)
    try:
        committed = admin.list_consumer_group_offsets(group)
        if not committed:
            print(f"No offsets for group {group}")
            return
        print(f"GROUP={group}")
        for tp, meta in sorted(committed.items(), key=lambda x: (x[0].topic, x[0].partition)):
            end = consumer.end_offsets([tp])[tp]
            lag = end - meta.offset
            print(
                f"  TOPIC={tp.topic} PARTITION={tp.partition} "
                f"CURRENT={meta.offset} LOG-END={end} LAG={lag}"
            )
    finally:
        admin.close()
        consumer.close()


if __name__ == "__main__":
    main()
