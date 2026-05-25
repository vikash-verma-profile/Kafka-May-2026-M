"""List connectors or show status for one connector."""

from __future__ import annotations

import json
import sys

import requests

from config import DEFAULT_CONNECT_URL


def main() -> None:
    connect_url = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_CONNECT_URL
    name = sys.argv[2] if len(sys.argv) > 2 else None

    if name:
        url = f"{connect_url}/connectors/{name}/status"
    else:
        url = f"{connect_url}/connectors"

    resp = requests.get(url, timeout=15)
    resp.raise_for_status()
    print(json.dumps(resp.json(), indent=2))


if __name__ == "__main__":
    main()
