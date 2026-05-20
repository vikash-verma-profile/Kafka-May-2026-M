# Lab 14- Errors, Troubleshooting & Best Practices

**Objective:** Reference guide for common producer issues (from course document Sections 14–16).

## Common producer errors

| Error | Typical cause |
|-------|----------------|
| `TimeoutException` | Broker down or overloaded |
| `SerializationException` | Wrong serializer or non-serializable value |
| `BufferExhaustedException` | `buffer.memory` too small for send rate |
| `UnknownTopicOrPartitionException` | Topic not created |
| `NotLeaderOrFollowerException` | Leader election in progress |

## Problem 1- Broker not running

**Symptom:** `Connection refused`, timeouts

**Fix:**

```powershell
bin\windows\kafka-server-start.bat config\server.properties
```

## Problem 2- Topic missing

**Symptom:** `UnknownTopicOrPartitionException`

**Fix:**

```powershell
Labs\scripts\create-orders-topic.bat
```

## Problem 3- Serialization error

**Checklist:**

- Key serializer matches key type (String vs Integer)
- Value serializer matches payload
- JSON is valid UTF-8 string when using `StringSerializer`

## Problem 4- Duplicate messages

**Cause:** Retries without idempotence

**Fix:** Enable `enable.idempotence=true` (Lab 09)

## Recommended best practices

1. Use **message keys** when ordering per entity matters
2. Enable **idempotence** for critical pipelines
3. Use **`acks=all`** for financial/order data
4. Enable **compression** for large payloads
5. Monitor producer metrics (lag, error rate, batch size)
6. Avoid huge messages (default max ~1 MB; configure carefully)

## Practice drill

Intentionally stop the broker and run `BasicProducer`- note the exception class. Restart broker and retry.

## Next lab

→ [Lab 15- Industry Example](../lab-15-industry-example/README.md)
