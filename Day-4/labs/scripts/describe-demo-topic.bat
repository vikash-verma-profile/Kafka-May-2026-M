@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME first.
  exit /b 1
)
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --describe ^
  --topic demo-topic ^
  --bootstrap-server localhost:9092
