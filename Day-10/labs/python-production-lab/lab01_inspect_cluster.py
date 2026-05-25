"""
Lab 01 — list topics and describe a topic / consumer groups.

Run: python lab01_inspect_cluster.py
"""

from __future__ import annotations

import sys

from kafka import KafkaAdminClient, KafkaConsumer, TopicPartition

from config import DEFAULT_BOOTSTRAP


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    topic = sys.argv[2] if len(sys.argv) > 2 else "orders"

    admin = KafkaAdminClient(bootstrap_servers=bootstrap)
    consumer = KafkaConsumer(bootstrap_servers=bootstrap)

    try:
        topics = sorted(consumer.topics())
        print("Topics:", topics)

        if topic in topics:
            parts = consumer.partitions_for_topic(topic)
            print(f"\nTopic {topic} partitions: {parts}")
            for p in sorted(parts or []):
                tp = TopicPartition(topic, p)
                end = consumer.end_offsets([tp])[tp]
                print(f"  partition {p} log-end-offset={end}")

        groups = admin.list_consumer_groups()
        print("\nConsumer groups:")
        for g in groups:
            print(f"  {g[0]} ({g[1]})")
    finally:
        consumer.close()
        admin.close()


if __name__ == "__main__":
    main()
