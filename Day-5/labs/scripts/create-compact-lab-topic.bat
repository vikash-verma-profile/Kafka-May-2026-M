@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)

"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --create ^
  --topic compact-lab ^
  --bootstrap-server localhost:9092 ^
  --partitions 1 ^
  --replication-factor 1 ^
  --config cleanup.policy=compact

if errorlevel 1 echo Topic may already exist.
