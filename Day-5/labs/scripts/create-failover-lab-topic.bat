@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)

if "%BS%"=="" set BS=localhost:9092,localhost:9094,localhost:9095

"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --create ^
  --topic failover-lab ^
  --bootstrap-server %BS% ^
  --partitions 3 ^
  --replication-factor 3

if errorlevel 1 echo Topic may already exist.
