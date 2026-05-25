"""
Lab 10 — capacity planning calculator (same formulas as slide 38).

Run: python lab10_capacity_plan.py
"""

from __future__ import annotations


def main() -> None:
    peak_mb_per_sec = 100
    retention_days = 7
    replication_factor = 3
    headroom = 0.30
    tb_per_broker = 30

    daily_gb = peak_mb_per_sec * 86_400 / 1024
    total_tb = daily_gb * retention_days * replication_factor / 1024
    with_headroom = total_tb * (1 + headroom)
    brokers = int(with_headroom / tb_per_broker) + (1 if with_headroom % tb_per_broker else 0)
    min_partitions = max(4, int(peak_mb_per_sec / 25))

    print(f"Daily ingest:        {daily_gb:.2f} GB/day")
    print(f"Total (7d x RF=3):   {total_tb:.1f} TB")
    print(f"With 30% headroom:   {with_headroom:.1f} TB")
    print(f"Suggested brokers:   {brokers} (@ {tb_per_broker} TB each)")
    print(f"Min partitions:      {min_partitions} (25-50 MB/s per partition target)")


if __name__ == "__main__":
    main()
