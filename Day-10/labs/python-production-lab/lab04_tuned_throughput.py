"""
Lab 04 — tuned producer throughput (batching + compression).

Run: python lab04_tuned_throughput.py
"""

from __future__ import annotations

import sys
import time

from kafka import KafkaProducer

from config import DEFAULT_BOOTSTRAP


def main() -> None:
    bootstrap = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_BOOTSTRAP
    topic = sys.argv[2] if len(sys.argv) > 2 else "bench"
    count = int(sys.argv[3]) if len(sys.argv) > 3 else 100_000
    record_size = 1024
    payload = b"x" * record_size

    producer = KafkaProducer(
        bootstrap_servers=bootstrap,
        acks="all",
        linger_ms=20,
        batch_size=65536,
        compression_type="lz4",
        retries=3,
    )

    start = time.perf_counter()
    for _ in range(count):
        producer.send(topic, value=payload)
    producer.flush()
    elapsed = time.perf_counter() - start
    mb = (count * record_size) / (1024 * 1024)
    print(f"Tuned: {count / elapsed:.0f} records/s, {mb / elapsed:.2f} MB/s in {elapsed:.2f}s")


if __name__ == "__main__":
    main()
