# Day 6 helper scripts

Run from **`labs\scripts`** (or use full path).

| Script | When | What it does |
|--------|------|----------------|
| [verify-schema-registry.bat](verify-schema-registry.bat) | Lab 03 | `GET /subjects` and `GET /config` |
| [create-employees-avro-topic.bat](create-employees-avro-topic.bat) | Lab 04 | Creates topic `employees-avro` (Docker if no `KAFKA_HOME`) |
| [register-schema-v2.bat](register-schema-v2.bat) | Lab 06 | POST `employee_v2.avsc` (adds `email` field) |
| [run-lab01.bat](run-lab01.bat) | Lab 01 | Java four-formats lab |
| [run-lab01-python.bat](run-lab01-python.bat) | Lab 01 | Python four-formats lab |

## Lab 06 — register v2 manually (PowerShell)

If `register-schema-v2.bat` fails, use Control Center or this pattern (escape the schema JSON):

```powershell
$schema = Get-Content "..\java-serialization-lab\src\main\avro\employee_v2.avsc" -Raw | ConvertFrom-Json | ConvertTo-Json -Compress
$body = @{ schema = (Get-Content "..\java-serialization-lab\src\main\avro\employee_v2.avsc" -Raw) } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "http://localhost:8081/subjects/employees-avro-value/versions" `
  -ContentType "application/vnd.schemaregistry.v1+json" -Body $body
Invoke-RestMethod http://localhost:8081/subjects/employees-avro-value/versions
```

Expected versions: `[1, 2]`.
