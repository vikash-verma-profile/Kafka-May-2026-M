# Kafka cluster manifest — Lab 02

Apply this directory’s YAML after the Strimzi operator is **Running** in namespace `kafka`.

## Apply

```bash
kubectl apply -f kafka-persistent.yaml -n kafka
```

From this folder:

```bash
cd C:\Users\om\Desktop\KafKa\Day-10\labs\lab-02-kubernetes-strimzi\create-kafka
kubectl apply -f kafka-persistent.yaml -n kafka
```

## Files

| File | Description |
|------|-------------|
| `kafka-persistent.yaml` | `KafkaNodePool` + `Kafka` for Strimzi 1.0 / KRaft |

## Resources created

### KafkaNodePool `dual-role`

- **Replicas:** 3
- **Roles:** `controller` + `broker` (combined KRaft nodes)
- **Storage:** 10 Gi persistent volume per node (`deleteClaim: false`)

### Kafka `my-cluster`

| Setting | Value |
|---------|--------|
| Kafka version | 4.1.0 |
| Metadata version | 4.1-IV0 |
| Internal listener | `plain` on 9092 (in-cluster) |
| External listener | `external` on 9094 (NodePort + `advertisedHost: localhost`) |
| Entity operator | Topic + user operators enabled |

### Replication defaults

```yaml
offsets.topic.replication.factor: 3
transaction.state.log.replication.factor: 3
transaction.state.log.min.isr: 2
default.replication.factor: 3
min.insync.replicas: 2
```

## External access (Windows / Docker Desktop)

| Service | NodePort | Port-forward | Client connects to |
|---------|----------|--------------|------------------|
| `my-cluster-kafka-external-bootstrap` | 30094 | `30094:9094` | `localhost:30094` |
| `my-cluster-dual-role-0` | 30095 | `30095:9094` | (metadata) |
| `my-cluster-dual-role-1` | 30096 | `30096:9094` | (metadata) |
| `my-cluster-dual-role-2` | 30097 | `30097:9094` | (metadata) |

Run all four port-forwards via [scripts/start-strimzi-port-forwards.bat](../../scripts/start-strimzi-port-forwards.bat).

**Do not** run `kafka-topics.bat` from this folder — use `%KAFKA_HOME%\bin\windows\` with `--bootstrap-server localhost:30094`.

## Verify

```bash
kubectl get kafka,kafkanodepool,pods,svc -n kafka
kubectl wait kafka/my-cluster --for=condition=Ready --timeout=900s -n kafka
```

```bash
kubectl exec my-cluster-dual-role-0 -n kafka -c kafka -- bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

See [../README.md](../README.md) for full lab steps.
