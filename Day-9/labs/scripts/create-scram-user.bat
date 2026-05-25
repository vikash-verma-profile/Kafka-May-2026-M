@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)

set BS=%~1
if "%BS%"=="" set BS=localhost:9092
set USER=%~2
if "%USER%"=="" set USER=alice
set PASS=%~3
if "%PASS%"=="" set PASS=secret

"%KAFKA_HOME%\bin\windows\kafka-configs.bat" --bootstrap-server %BS% ^
  --alter --add-config "SCRAM-SHA-512=[password=%PASS%]" ^
  --entity-type users --entity-name %USER%

echo Created SCRAM user %USER%
