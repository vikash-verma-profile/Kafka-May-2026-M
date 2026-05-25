@echo off
set NAME=%~1
set CONNECT_URL=%~2
if "%CONNECT_URL%"=="" set CONNECT_URL=http://localhost:8083
if "%NAME%"=="" (
  curl -s "%CONNECT_URL%/connectors"
  echo.
  exit /b 0
)
curl -s "%CONNECT_URL%/connectors/%NAME%/status"
echo.
