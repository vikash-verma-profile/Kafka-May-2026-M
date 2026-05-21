"""Lab 04 - slow consumer to build lag."""
import sys
import time
from kafka import KafkaConsumer
from config import BOOTSTRAP_SERVERS, LAG_TOPIC


def main():
    group_id = sys.argv[1] if len(sys.argv) > 1 else "lag-group"
    sleep_ms = int(sys.argv[2]) if len(sys.argv) > 2 else 500
    name = sys.argv[3] if len(sys.argv) > 3 else "SlowConsumer"
    consumer = KafkaConsumer(
        LAG_TOPIC,
        bootstrap_servers=BOOTSTRAP_SERVERS,
        group_id=group_id,
        client_id=name,
        auto_offset_reset="earliest",
        enable_auto_commit=False,
        value_deserializer=lambda v: v.decode("utf-8"),
    )
    print(f"[{name}] group={group_id} sleep={sleep_ms}ms")
    try:
        for msg in consumer:
            time.sleep(sleep_ms / 1000.0)
            print(f"[{name}] P={msg.partition} offset={msg.offset} value={msg.value}")
            consumer.commit()
    finally:
        consumer.close()


if __name__ == "__main__":
    main()
