# Lab 08- Producer Retries

**Objective:** Configure retries and backoff when the broker is temporarily unavailable.

## Code

`RetriesProducer.java`

## Step 1- Review configuration

| Property | Value in lab |
|----------|----------------|
| `retries` | `3` |
| `retry.backoff.ms` | `1000` |
| `acks` | `all` |

## Step 2- Run the producer

Sends 5 messages with 2-second pause between each:

```powershell
mvn -q exec:java "-Dexec.mainClass=com.kafka.producer.lab.RetriesProducer"
```

## Step 3- Simulate broker failure (optional, advanced)

1. Start `RetriesProducer`.
2. While it runs, **stop** the Kafka broker (Ctrl+C on broker terminal).
3. Restart the broker within the retry window.
4. Watch logs: client should retry and eventually succeed or fail after max retries.

## Step 4- Without retries

Temporarily set `RETRIES_CONFIG` to `0` and repeat- failures surface immediately.

## What you learned

- Transient network/leader errors can be retried automatically
- Backoff avoids hammering a recovering cluster
- Retries + non-idempotent producer can cause **duplicates** (fixed in Lab 09)

## Next lab

→ [Lab 09- Idempotent Producer](../lab-09-idempotent-producer/README.md)
