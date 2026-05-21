"""Lab 03 - manual commit after each message."""
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
    print("manual_sync_consumer")
    for msg in consumer:
        print(f"SYNC P={msg.partition} offset={msg.offset} value={msg.value}")
        consumer.commit()


if __name__ == "__main__":
    main()
