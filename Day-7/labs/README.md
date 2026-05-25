# Day 7-Kafka Streams Labs

Hands-on labs from **Kafka_Streams.pptx**. Discussion exercises from the deck are noted in each lab where relevant.

| Lab | Folder | Topic | Time |
|-----|--------|-------|------|
| 01 | [lab-01-build-stream-processing-app](lab-01-build-stream-processing-app/README.md) | First Streams topology | ~45 min |
| 02 | [lab-02-real-time-word-count](lab-02-real-time-word-count/README.md) | Word count pipeline | ~40 min |
| 03 | [lab-03-filtering-and-aggregation](lab-03-filtering-and-aggregation/README.md) | Filter + aggregate | ~40 min |
| 04 | [lab-04-mini-project-order-pipeline](lab-04-mini-project-order-pipeline/README.md) | Order processing capstone | ~90 min |

## Code

| Track | Path | Notes |
|-------|------|-------|
| **Java** | [java-kafka-streams-lab](java-kafka-streams-lab/) | Kafka Streams API (Labs 01–04) |
| **Python** | [python-stream-processing-lab](python-stream-processing-lab/) | Consumer/producer stream pattern |
| **Scripts** | [scripts](scripts/) | Create Kafka topics |

> Python labs mirror the same topology; Kafka Streams itself is JVM-only.

## Prerequisites

- Java JDK 17+, Maven 3.8+
- Kafka on `localhost:9092`
- `kafka-streams` dependency in your Maven project (version aligned with your broker)
