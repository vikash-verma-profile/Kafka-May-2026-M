@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  echo Example: set KAFKA_HOME=C:\kafka-bin\kafka_2.13-4.2.0
  exit /b 1
)

"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --create ^
  --topic lag-demo ^
  --bootstrap-server localhost:9092 ^
  --partitions 3 ^
  --replication-factor 1

if errorlevel 1 (
  echo Topic may already exist. Run describe-lag-demo-topic.bat to verify.
)
