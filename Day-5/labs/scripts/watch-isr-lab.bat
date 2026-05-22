@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)
if "%BS%"=="" set BS=localhost:9092,localhost:9094,localhost:9095

:loop
echo.
echo === %date% %time% ===
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --bootstrap-server %BS% --describe --topic isr-lab
timeout /t 5 /nobreak >nul
goto loop
