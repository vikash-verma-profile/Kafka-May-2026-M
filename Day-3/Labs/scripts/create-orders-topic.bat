@echo off
REM Create orders-topic with 4 partitions (Day-3 labs)
REM Set KAFKA_HOME to your Kafka install, e.g. C:\kafka-bin\kafka_2.13-4.2.0

if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  echo Example: set KAFKA_HOME=C:\kafka-bin\kafka_2.13-4.2.0
  exit /b 1
)

"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --create ^
  --topic orders-topic ^
  --bootstrap-server localhost:9092 ^
  --partitions 4 ^
  --replication-factor 1

if errorlevel 1 (
  echo Topic may already exist. Run describe-orders-topic.bat to verify.
)
