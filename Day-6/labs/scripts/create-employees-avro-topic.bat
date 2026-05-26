@echo off
setlocal
set TOPIC=employees-avro
set BOOTSTRAP=localhost:9092
set PARTITIONS=3
set RF=1

if not "%KAFKA_HOME%"=="" (
  echo Using KAFKA_HOME=%KAFKA_HOME%
  "%KAFKA_HOME%\bin\windows\kafka-topics.bat" --create ^
    --topic %TOPIC% ^
    --bootstrap-server %BOOTSTRAP% ^
    --partitions %PARTITIONS% ^
    --replication-factor %RF%
  if errorlevel 1 echo Topic may already exist.
  exit /b 0
)

set COMPOSE_DIR=%~dp0..\..\confluent-local
if not exist "%COMPOSE_DIR%\docker-compose.yml" (
  echo Set KAFKA_HOME or start confluent-local at: %COMPOSE_DIR%
  exit /b 1
)

echo Using Docker Compose in %COMPOSE_DIR%
pushd "%COMPOSE_DIR%"
docker compose exec kafka kafka-topics --create ^
  --topic %TOPIC% ^
  --bootstrap-server %BOOTSTRAP% ^
  --partitions %PARTITIONS% ^
  --replication-factor %RF%
if errorlevel 1 echo Topic may already exist.
popd
