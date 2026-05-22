@echo off
rem Usage: dump-log-segment.bat <full-path-to-.log|.index|.timeindex> [--print-data-log]
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)
if "%~1"=="" (
  echo Usage: dump-log-segment.bat C:\kafka-data\...\00000000000000000000.log [--print-data-log]
  exit /b 1
)

"%KAFKA_HOME%\bin\windows\kafka-dump-log.bat" --files "%~1" %2 %3 %4
