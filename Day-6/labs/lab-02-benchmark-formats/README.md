# Lab 02 — Benchmark the Four Formats

**Objective:** Measure payload size and serialization time for JSON, XML, Avro, and Protobuf over 100,000 iterations.

From **Seralization.pptx** — Slide 14.

---

## Implementation

| Track | Directory | Command |
|-------|-----------|---------|
| **Java** | `labs\java-serialization-lab` | `mvn -q exec:java "-Dexec.mainClass=com.training.kafka.lab02.FormatBenchmark"` |
| **Python** | `labs\python-serialization-lab` | `python lab02_format_benchmark.py` |

---

## Prerequisites

- [Lab 01](../lab-01-serialize-pojo-four-formats/README.md) completed (reusable serializers)
- No Kafka broker required

---

## Step 1 — Reuse Lab 01 code

Import or copy the four serializer implementations into a `FormatBenchmark` class.

---

## Step 2 — Single-record size

For one `Employee` instance, serialize once per format and print:

```text
Format    Size (bytes)
JSON      ...
XML       ...
Avro      ...
Protobuf  ...
```

---

## Step 3 — Warm-up and timed loop

```java
int iterations = 100_000;
Employee emp = new Employee(101, "Asha", "a@x.io");

// Warm-up (discard first runs)
for (int i = 0; i < 10_000; i++) serializeJson(emp);

long start = System.nanoTime();
for (int i = 0; i < iterations; i++) serializeJson(emp);
long elapsedMs = (System.nanoTime() - start) / 1_000_000;
```

Repeat for XML, Avro, Protobuf.

---

## Step 4 — Print results table

| Format | Size (B) | Time (ms) | Ratio vs JSON (time) |
|--------|----------|-----------|----------------------|
| JSON | | | 1.0 |
| XML | | | |
| Avro | | | |
| Protobuf | | | |

**Expected:** Avro/Protobuf ~2× smaller and ~3–5× faster than JSON.

---

## Step 5 — Stretch: 20-field record

Add fields (dept, salary, address, …) to `Employee` and re-run. The gap between text and binary formats should widen.

---

## Checkpoint

- [ ] Table printed with all four formats
- [ ] Avro or Protobuf fastest; XML slowest
- [ ] Size ratio documented in one sentence

---

## Deliverable

Short note: which format you would pick for Kafka at 1M events/sec and why.
