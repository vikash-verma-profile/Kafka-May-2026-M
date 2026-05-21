@echo off
REM Usage: watch-lag.bat GROUP_ID
REM Polls consumer group lag every 10 seconds
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME first.
  exit /b 1
)
if "%~1"=="" (
  echo Usage: watch-lag.bat GROUP_ID
  exit /b 1
)
:loop
echo.
echo === %date% %time% ===
"%KAFKA_HOME%\bin\windows\kafka-consumer-groups.bat" --bootstrap-server localhost:9092 --describe --group %~1
timeout /t 10 /nobreak >nul
goto loop
