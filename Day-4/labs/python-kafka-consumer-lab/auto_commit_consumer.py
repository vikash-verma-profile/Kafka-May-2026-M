"""Lab 03 - auto commit enabled."""
import sys
from kafka import KafkaConsumer
from config import BOOTSTRAP_SERVERS, DEMO_TOPIC


def main():
    group_id = sys.argv[1] if len(sys.argv) > 1 else "commit-lab-group"
    consumer = KafkaConsumer(
        DEMO_TOPIC,
        bootstrap_servers=BOOTSTRAP_SERVERS,
        group_id=group_id,
        auto_offset_reset="earliest",
        enable_auto_commit=True,
        auto_commit_interval_ms=5000,
        value_deserializer=lambda v: v.decode("utf-8"),
    )
    print("auto_commit_consumer: interval=5000ms")
    for msg in consumer:
        print(f"AUTO P={msg.partition} offset={msg.offset} value={msg.value}")


if __name__ == "__main__":
    main()
