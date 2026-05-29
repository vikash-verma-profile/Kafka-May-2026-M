# Shared client and broker config snippets

| File | Use |
|------|-----|
| [client-scram.properties](client-scram.properties) | Multi-line SCRAM client config (Lab 01+) |
| [client-sasl-ssl.properties.template](client-sasl-ssl.properties.template) | TLS + SCRAM template (Lab 03) |
| [broker-sasl-ssl.properties.snippet](broker-sasl-ssl.properties.snippet) | Broker TLS listener snippet |

**Recommended for Kafka 4.x CLI on Windows:** [../my-config/client-scram-oneshot.properties](../my-config/client-scram-oneshot.properties) (single-line `sasl.jaas.config` — avoids line-continuation issues).

**Cluster configs (controller + 3 brokers):** [../my-config/](../my-config/)

**Errors:** [../TROUBLESHOOTING.md](../TROUBLESHOOTING.md)
