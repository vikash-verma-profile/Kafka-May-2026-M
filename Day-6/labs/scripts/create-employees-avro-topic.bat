@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)

"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --create ^
  --topic employees-avro ^
  --bootstrap-server localhost:9092 ^
  --partitions 3 ^
  --replication-factor 1

if errorlevel 1 echo Topic may already exist.
