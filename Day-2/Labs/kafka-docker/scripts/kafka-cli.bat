@echo off
rem Run Kafka CLI tools inside the Docker container.
rem Usage: kafka-cli.bat topics --bootstrap-server localhost:9092 --list
rem        kafka-cli.bat console-producer --bootstrap-server localhost:9092 --topic lab-messages
rem        kafka-cli.bat console-consumer --bootstrap-server localhost:9092 --topic lab-messages --from-beginning

setlocal
set "TOOL=%~1"
shift

if "%TOOL%"=="" (
  echo Usage: kafka-cli.bat ^<tool^> [args...]
  echo Tools: topics, console-producer, console-consumer, storage
  exit /b 1
)

docker ps --filter "name=kafka-broker" --format "{{.Names}}" | findstr /i "kafka-broker" >nul 2>&1
if errorlevel 1 (
  echo Kafka container 'kafka-broker' is not running. Start with: docker compose up -d
  exit /b 1
)

if /i "%TOOL%"=="topics" set "SCRIPT=kafka-topics.sh"
if /i "%TOOL%"=="console-producer" set "SCRIPT=kafka-console-producer.sh"
if /i "%TOOL%"=="console-consumer" set "SCRIPT=kafka-console-consumer.sh"
if /i "%TOOL%"=="storage" set "SCRIPT=kafka-storage.sh"

if not defined SCRIPT (
  echo Unknown tool: %TOOL%
  exit /b 1
)

if /i "%TOOL%"=="console-producer" goto :interactive
if /i "%TOOL%"=="console-consumer" goto :interactive

docker exec kafka-broker /opt/kafka/bin/%SCRIPT% %*
goto :eof

:interactive
docker exec -it kafka-broker /opt/kafka/bin/%SCRIPT% %*
endlocal
