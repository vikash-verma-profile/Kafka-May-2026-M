@echo off
REM Recreate orders-topic for Day-3 labs (4 partitions, RF=1 on local broker)
REM WARNING: deletes all data in orders-topic. Dev / local KRaft only.

if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)

echo Deleting orders-topic (if it exists)...
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --delete ^
  --topic orders-topic ^
  --bootstrap-server localhost:9092

echo Waiting for delete to complete...
timeout /t 3 /nobreak >nul

echo Creating orders-topic (4 partitions, replication-factor 1)...
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --create ^
  --topic orders-topic ^
  --bootstrap-server localhost:9092 ^
  --partitions 4 ^
  --replication-factor 1

echo.
echo Topic details:
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --describe ^
  --topic orders-topic ^
  --bootstrap-server localhost:9092

echo.
echo Done. Start the consumer, then run BasicProducer in a second terminal.
