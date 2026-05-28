DEFAULT_CONNECT_URL = "http://localhost:8083"
# Any one broker is enough for clients to discover the cluster (9093 = controller only)
DEFAULT_BOOTSTRAP = "localhost:9092"
# kafka-python cannot auto-detect Kafka 4.x; without this you get NoBrokersAvailable
KAFKA_API_VERSION = (2, 8, 0)


def parse_bootstrap(bootstrap: str) -> list[str]:
    return [s.strip() for s in bootstrap.split(",") if s.strip()]
