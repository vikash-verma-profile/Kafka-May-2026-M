"""
Lab 02 — benchmark serialization size and time.

Run: python lab02_format_benchmark.py
"""

from __future__ import annotations

import time

from lab01_four_formats import (
    Employee,
    deserialize_avro,
    deserialize_json,
    deserialize_protobuf,
    deserialize_xml,
    serialize_avro,
    serialize_json,
    serialize_protobuf,
    serialize_xml,
)

WARMUP = 10_000
ITERATIONS = 100_000


def bench(fn, emp: Employee) -> float:
    for _ in range(WARMUP):
        fn(emp)
    start = time.perf_counter()
    for _ in range(ITERATIONS):
        fn(emp)
    return time.perf_counter() - start


def main() -> None:
    emp = Employee(101, "Asha", "a@x.io")
    rows = [
        ("JSON", serialize_json(emp), bench(serialize_json, emp)),
        ("XML", serialize_xml(emp), bench(serialize_xml, emp)),
        ("Avro", serialize_avro(emp), bench(serialize_avro, emp)),
        ("Protobuf", serialize_protobuf(emp), bench(serialize_protobuf, emp)),
    ]
    baseline = rows[0][2]
    print("| Format   | Size (B) | Time (s) | Ratio vs JSON |")
    print("|----------|----------|----------|---------------|")
    for name, size, seconds in rows:
        ratio = seconds / baseline
        print(f"| {name:8} | {size:8} | {seconds:8.3f} | {ratio:13.2f}x |")


if __name__ == "__main__":
    main()
