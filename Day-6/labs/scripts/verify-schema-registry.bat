@echo off
set REGISTRY_URL=http://localhost:8081
echo Checking Schema Registry at %REGISTRY_URL%
curl -s %REGISTRY_URL%/subjects
echo.
curl -s %REGISTRY_URL%/config
echo.
