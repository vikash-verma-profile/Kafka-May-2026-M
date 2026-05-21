"""Lab 03 - commit after each message (kafka-python uses sync commit)."""
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
        enable_auto_commit=False,
        value_deserializer=lambda v: v.decode("utf-8"),
    )
    print("manual_async_consumer (explicit commit per message)")
    for msg in consumer:
        print(f"ASYNC P={msg.partition} offset={msg.offset} value={msg.value}")
        consumer.commit_async()


if __name__ == "__main__":
    main()
