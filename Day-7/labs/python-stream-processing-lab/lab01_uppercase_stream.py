"""
Lab 01-Python stream processor: uppercase values (streams-input -> streams-output).

Kafka Streams is JVM-only; this uses a consumer/producer loop with the same topology idea.

Run: python lab01_uppercase_stream.py
"""

from __future__ import annotations

import sys

from kafka import KafkaConsumer, KafkaProducer

from config import DEFAULT_BOOTSTRAP

INPUT_TOPIC = "streams-input"
OUTPUT_TOPIC = "streams-output"


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP

    consumer = KafkaConsumer(
        INPUT_TOPIC,
        bootstrap_servers=bootstrap,
        group_id="py-first-streams-app",
        auto_offset_reset="earliest",
        enable_auto_commit=True,
        value_deserializer=lambda v: v.decode("utf-8"),
    )
    producer = KafkaProducer(
        bootstrap_servers=bootstrap,
        value_serializer=lambda v: v.encode("utf-8"),
    )

    print(f"Processing {INPUT_TOPIC} -> {OUTPUT_TOPIC} (Ctrl+C to stop)")
    try:
        for msg in consumer:
            upper = msg.value.upper()
            producer.send(OUTPUT_TOPIC, value=upper, key=msg.key)
            producer.flush()
            print(f"in={msg.value!r} out={upper!r}")
    except KeyboardInterrupt:
        pass
    finally:
        consumer.close()
        producer.close()


if __name__ == "__main__":
    main()
