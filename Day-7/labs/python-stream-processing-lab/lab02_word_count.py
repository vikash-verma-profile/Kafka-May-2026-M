"""
Lab 02-word count with in-process state (sentences -> word-counts).

Run: python lab02_word_count.py
"""

from __future__ import annotations

import json
import re
import sys
from collections import defaultdict

from kafka import KafkaConsumer, KafkaProducer

from config import DEFAULT_BOOTSTRAP

INPUT_TOPIC = "sentences"
OUTPUT_TOPIC = "word-counts"


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    counts: dict[str, int] = defaultdict(int)

    consumer = KafkaConsumer(
        INPUT_TOPIC,
        bootstrap_servers=bootstrap,
        group_id="py-wordcount-app",
        auto_offset_reset="earliest",
        enable_auto_commit=True,
        value_deserializer=lambda v: v.decode("utf-8"),
    )
    producer = KafkaProducer(
        bootstrap_servers=bootstrap,
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
        key_serializer=lambda k: k.encode("utf-8"),
    )

    print(f"Word count on {INPUT_TOPIC} (Ctrl+C to stop)")
    try:
        for msg in consumer:
            for word in re.split(r"\W+", msg.value.lower()):
                if not word:
                    continue
                counts[word] += 1
                payload = {"word": word, "count": counts[word]}
                producer.send(OUTPUT_TOPIC, key=word, value=payload)
            producer.flush()
    except KeyboardInterrupt:
        pass
    finally:
        consumer.close()
        producer.close()
        print("Top words:", sorted(counts.items(), key=lambda x: -x[1])[:10])


if __name__ == "__main__":
    main()
