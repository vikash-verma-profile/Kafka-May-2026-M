"""
Deploy a Kafka Connect connector from JSON config.

Run:
    python deploy_connector.py ../configs/mysql-orders-source.json
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

import requests

from config import DEFAULT_CONNECT_URL


def main() -> None:
    if len(sys.argv) < 2:
        print("Usage: python deploy_connector.py <connector.json> [connect-url]")
        sys.exit(1)

    path = Path(sys.argv[1])
    connect_url = sys.argv[2] if len(sys.argv) > 2 else DEFAULT_CONNECT_URL
    payload = json.loads(path.read_text(encoding="utf-8"))

    resp = requests.post(f"{connect_url}/connectors", json=payload, timeout=30)
    print(resp.status_code, resp.text)
    resp.raise_for_status()


if __name__ == "__main__":
    main()
