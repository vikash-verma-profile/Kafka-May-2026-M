"""
Lab 03 — simple producer throughput benchmark (Python baseline).

Run: python lab03_baseline_throughput.py localhost:9092 bench 100000
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
        acks=1,
        linger_ms=0,
        batch_size=16384,
    )

    start = time.perf_counter()
    for i in range(count):
        producer.send(topic, value=payload)
    producer.flush()
    elapsed = time.perf_counter() - start

    mb = (count * record_size) / (1024 * 1024)
    print(f"Sent {count} records ({mb:.2f} MB) in {elapsed:.2f}s")
    print(f"Throughput: {count / elapsed:.0f} records/s, {mb / elapsed:.2f} MB/s")


if __name__ == "__main__":
    main()
