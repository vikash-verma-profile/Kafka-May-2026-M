@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)

set BS=%~1
if "%BS%"=="" set BS=localhost:9092

echo === Broker API versions ===
"%KAFKA_HOME%\bin\windows\kafka-broker-api-versions.bat" --bootstrap-server %BS%
echo.
echo === Topics ===
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --bootstrap-server %BS% --list
echo.
echo === Describe orders (if exists) ===
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --bootstrap-server %BS% --describe --topic orders 2>nul
echo.
echo === Consumer groups ===
"%KAFKA_HOME%\bin\windows\kafka-consumer-groups.bat" --bootstrap-server %BS% --list
