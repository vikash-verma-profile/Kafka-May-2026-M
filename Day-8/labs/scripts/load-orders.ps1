# Day 8 Lab 06 - generate INSERT load for MySQL orders table
param(
    [int]$Count = 500,
    [string]$Host = "localhost",
    [int]$Port = 3306,
    [string]$Database = "ordersdb",
    [string]$User = "root",
    [string]$Password = "root"
)

for ($i = 1; $i -le $Count; $i++) {
    $customerId = Get-Random -Minimum 1 -Maximum 100
    $total = [math]::Round((Get-Random -Minimum 10 -Maximum 5000) + (Get-Random) / 100.0, 2)
    $sql = "INSERT INTO orders (customer_id, order_total) VALUES ($customerId, $total);"
    & mysql -h $Host -P $Port -u $User "-p$Password" -D $Database -e $sql 2>$null
    if ($i % 50 -eq 0) { Write-Host "Inserted $i rows..." }
}
Write-Host "Done. Inserted $Count orders."
