# Lab 01-Component Mapping (Answer Key)

| Config line | Component |
|-------------|-----------|
| `connector.class=JdbcSourceConnector` | **Connector** |
| `tasks.max=3` | **Task** parallelism (max 3 tasks) |
| `value.converter=AvroConverter` | **Converter** (value path) |
| `transforms=mask` + `MaskField$Value` | **SMT** |
| JVM running Connect REST + tasks | **Worker** |

**Converter position:** After `poll()`, before records are written to Kafka (source path).

**Single table + tasks.max=3:** Usually **1 task** runs; extra slots unused unless connector splits work.

**Second SMT:** Add to `transforms=mask,dropCol` with `transforms.dropCol.type=...`.
