@echo off
set REGISTRY=http://localhost:8081
set SUBJECT=employees-avro-value
set SCHEMA_FILE=%~dp0..\java-serialization-lab\src\main\avro\employee_v2.avsc

echo Registering schema v2 for %SUBJECT%
curl -s -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" ^
  -d "{\"schema\": %SCHEMA_FILE%}" ^
  "%REGISTRY%/subjects/%SUBJECT%/versions"

echo.
curl -s "%REGISTRY%/subjects/%SUBJECT%/versions"
echo.
