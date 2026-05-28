@echo off
if "%~1"=="" (
  echo Usage: deploy-connector.bat ^<config-json-path^> [connect-url]
  echo Example ^(cmd^): deploy-connector.bat ..\configs\mysql-orders-source.json
  echo Example ^(PowerShell from labs^): .\scripts\deploy-connector.bat .\configs\mysql-orders-source.json
  exit /b 1
)

set CONFIG=%~1
set CONNECT_URL=%~2
if "%CONNECT_URL%"=="" set CONNECT_URL=http://localhost:8083

echo POST %CONFIG% to %CONNECT_URL%/connectors
curl -s -X POST -H "Content-Type: application/json" --data @"%CONFIG%" "%CONNECT_URL%/connectors"
echo.
