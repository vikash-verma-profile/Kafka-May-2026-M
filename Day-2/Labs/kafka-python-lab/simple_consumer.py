"""
Subscribe to a topic and print messages as they arrive.

Run:
    python simple_consumer.py
    python simple_consumer.py localhost:9092 lab-messages my-group

Press Ctrl+C to stop.
"""

import sys

from kafka import KafkaConsumer

from config import DEFAULT_BOOTSTRAP, DEFAULT_GROUP_ID, DEFAULT_TOPIC


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    topic = sys.argv[2] if len(sys.argv) > 2 else DEFAULT_TOPIC
    group_id = sys.argv[3] if len(sys.argv) > 3 else DEFAULT_GROUP_ID

    print(
        f"Connecting to {bootstrap}, topic={topic}, group={group_id} (Ctrl+C to stop)"
    )

    consumer = KafkaConsumer(
        topic,
        bootstrap_servers=bootstrap,
        group_id=group_id,
        auto_offset_reset="earliest",
        enable_auto_commit=True,
        value_deserializer=lambda v: v.decode("utf-8"),
        client_id="python-lab-consumer",
    )

    try:
        for message in consumer:
            print(
                f'Received: "{message.value}" | '
                f"partition={message.partition} offset={message.offset} "
                f"key={message.key!r}"
            )
    except KeyboardInterrupt:
        print("\nConsumer stopped.")
    finally:
        consumer.close()


if __name__ == "__main__":
    main()
