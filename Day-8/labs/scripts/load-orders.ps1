# Day 8 Lab 06-generate INSERT load for PostgreSQL orders table
param(
    [int]$Count = 500,
    [string]$Host = "localhost",
    [string]$Database = "ordersdb",
    [string]$User = "postgres"
)

for ($i = 1; $i -le $Count; $i++) {
    $customerId = Get-Random -Minimum 1 -Maximum 100
    $total = [math]::Round((Get-Random -Minimum 10 -Maximum 5000) + (Get-Random) / 100.0, 2)
    $sql = "INSERT INTO orders (customer_id, order_total) VALUES ($customerId, $total);"
    & psql -h $Host -U $User -d $Database -c $sql 2>$null
    if ($i % 50 -eq 0) { Write-Host "Inserted $i rows..." }
}
Write-Host "Done. Inserted $Count orders."
