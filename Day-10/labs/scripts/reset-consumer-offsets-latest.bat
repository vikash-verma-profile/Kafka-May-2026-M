@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)

set BS=%~1
if "%BS%"=="" set BS=localhost:9092
set GROUP=%~2
if "%GROUP%"=="" set GROUP=order-processor

echo Dry-run reset %GROUP% on orders to latest:
"%KAFKA_HOME%\bin\windows\kafka-consumer-groups.bat" --bootstrap-server %BS% ^
  --group %GROUP% --topic orders --reset-offsets --to-latest --dry-run

echo.
set /p CONFIRM=Execute reset? Type YES:
if /i not "%CONFIRM%"=="YES" exit /b 0

"%KAFKA_HOME%\bin\windows\kafka-consumer-groups.bat" --bootstrap-server %BS% ^
  --group %GROUP% --topic orders --reset-offsets --to-latest --execute

"%KAFKA_HOME%\bin\windows\kafka-consumer-groups.bat" --bootstrap-server %BS% ^
  --describe --group %GROUP%
