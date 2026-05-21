"""Lab 02 - named consumer for group demos."""
import sys
from kafka import KafkaConsumer
from config import BOOTSTRAP_SERVERS, DEMO_TOPIC


def main():
    group_id = sys.argv[1] if len(sys.argv) > 1 else "lab-group"
    name = sys.argv[2] if len(sys.argv) > 2 else "Consumer-1"
    consumer = KafkaConsumer(
        DEMO_TOPIC,
        bootstrap_servers=BOOTSTRAP_SERVERS,
        group_id=group_id,
        client_id=name,
        auto_offset_reset="earliest",
        enable_auto_commit=False,
        key_deserializer=lambda k: k.decode("utf-8") if k else None,
        value_deserializer=lambda v: v.decode("utf-8"),
    )
    print(f"[{name}] group={group_id} partitions={consumer.assignment()}")
    try:
        for msg in consumer:
            print(
                f"[{name}] P={msg.partition} offset={msg.offset} "
                f"key={msg.key} value={msg.value}"
            )
            consumer.commit()
    finally:
        consumer.close()


if __name__ == "__main__":
    main()
