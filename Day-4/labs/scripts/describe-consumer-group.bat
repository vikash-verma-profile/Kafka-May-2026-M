@echo off
REM Usage: describe-consumer-group.bat GROUP_ID
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME first.
  exit /b 1
)
if "%~1"=="" (
  echo Usage: describe-consumer-group.bat GROUP_ID
  exit /b 1
)
"%KAFKA_HOME%\bin\windows\kafka-consumer-groups.bat" --bootstrap-server localhost:9092 --describe --group %~1
