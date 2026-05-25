@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)

"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --create ^
  --topic drill-orders ^
  --bootstrap-server localhost:9092,localhost:9094,localhost:9095 ^
  --partitions 6 ^
  --replication-factor 3 ^
  --config min.insync.replicas=2

if errorlevel 1 echo Topic may already exist.
