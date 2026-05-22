@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)
if "%BS%"=="" set BS=localhost:9092,localhost:9094,localhost:9095

"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --bootstrap-server %BS% --describe --topic failover-lab
