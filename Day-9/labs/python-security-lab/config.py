import os
from pathlib import Path

DEFAULT_BOOTSTRAP = os.environ.get("KAFKA_BOOTSTRAP", "localhost:9096")
CONFIG_DIR = Path(__file__).parent.parent / "configs"
CLIENT_SCRAM_PROPERTIES = CONFIG_DIR / "client-scram.properties"
