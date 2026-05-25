@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)

set BS=%~1
if "%BS%"=="" set BS=localhost:9092

"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --create ^
  --topic bench ^
  --bootstrap-server %BS% ^
  --partitions 6 ^
  --replication-factor 3

if errorlevel 1 echo Topic may already exist.
