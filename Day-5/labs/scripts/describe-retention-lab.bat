@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)

"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --bootstrap-server localhost:9092 --describe --topic retention-lab
"%KAFKA_HOME%\bin\windows\kafka-configs.bat" --bootstrap-server localhost:9092 --entity-type topics --entity-name retention-lab --describe
