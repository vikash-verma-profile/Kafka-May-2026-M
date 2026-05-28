# SQL scripts (MySQL)

| File | Purpose |
| ---- | ------- |
| [init-ordersdb.sql](init-ordersdb.sql) | Creates `ordersdb` + `orders` / `customers` tables; creates `analytics` database |

## Run (Windows)

```bat
cd /d C:\Users\om\Desktop\KafKa\Day-8\labs\sql
mysql -u root -p < init-ordersdb.sql
```

Or execute in **MySQL Workbench** connected to `localhost:3306`.

## Used by labs

| Database | Table | Labs |
| -------- | ----- | ---- |
| `ordersdb` | `orders` | 02, 05, 06, 08 |
| `analytics` | `orders_fact` (auto-created by sink) | 03 |
