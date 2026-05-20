@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)

"%KAFKA_HOME%\bin\windows\kafka-console-consumer.bat" ^
  --topic orders-topic ^
  --from-beginning ^
  --bootstrap-server localhost:9092 ^
  --property print.key=true ^
  --property key.separator=" : "
