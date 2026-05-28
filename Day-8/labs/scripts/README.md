# Lab scripts (Windows)

Helper scripts for Kafka Connect labs. Run from **`labs`** folder unless noted.

| Script | Purpose |
| ------ | ------- |
| [deploy-connector.bat](deploy-connector.bat) | POST connector JSON to Connect REST |
| [connect-status.bat](connect-status.bat) | GET connector status |
| [load-orders.ps1](load-orders.ps1) | Lab 06 — bulk INSERT into MySQL `orders` |

## deploy-connector.bat

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs
.\scripts\deploy-connector.bat .\configs\mysql-orders-source.json http://localhost:8083
```

Uses `curl` to `POST` the JSON to `/connectors`. On success you get connector JSON back; on failure, an error body (e.g. **400** missing plugin class, **409** name already exists).

| Symptom | Fix |
| ------- | --- |
| `deploy-connector.bat` not recognized | Prefix with `.\scripts\` from the `labs` folder |
| `: was unexpected at this time` | Run from `labs` with `.\scripts\deploy-connector.bat ...` (cmd parses unescaped `(` in echo lines) |

## connect-status.bat

```powershell
.\scripts\connect-status.bat mysql-orders-source http://localhost:8083
```

## load-orders.ps1 (Lab 06)

```powershell
cd C:\Users\om\Desktop\KafKa\Day-8\labs\scripts
.\load-orders.ps1 -Count 500 -User root -Password YOUR_PASSWORD
```

Parameters:

| Param | Default | Description |
| ----- | ------- | ----------- |
| `-Count` | 500 | Number of INSERTs |
| `-Host` | localhost | MySQL host |
| `-Port` | 3306 | MySQL port |
| `-Database` | ordersdb | Database name |
| `-User` | root | MySQL user |
| `-Password` | root | MySQL password |

## Related

- [configs/README.md](../configs/README.md)
- [Lab 02](../lab-02-postgresql-jdbc-source/README.md)
