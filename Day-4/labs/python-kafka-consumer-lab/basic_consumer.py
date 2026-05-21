"""Lab 01 - basic Kafka consumer."""
import sys
from kafka import KafkaConsumer
from config import BOOTSTRAP_SERVERS, DEMO_TOPIC


def main():
    group_id = sys.argv[1] if len(sys.argv) > 1 else "lab1-group"
    consumer = KafkaConsumer(
        DEMO_TOPIC,
        bootstrap_servers=BOOTSTRAP_SERVERS,
        group_id=group_id,
        auto_offset_reset="earliest",
        enable_auto_commit=False,
        key_deserializer=lambda k: k.decode("utf-8") if k else None,
        value_deserializer=lambda v: v.decode("utf-8"),
    )
    print(f"Consuming {DEMO_TOPIC} group={group_id} (Ctrl+C to stop)")
    try:
        for msg in consumer:
            print(
                f"Partition={msg.partition}, Offset={msg.offset}, "
                f"Key={msg.key}, Value={msg.value}"
            )
            consumer.commit()
    except KeyboardInterrupt:
        consumer.commit()
    finally:
        consumer.close()


if __name__ == "__main__":
    main()
