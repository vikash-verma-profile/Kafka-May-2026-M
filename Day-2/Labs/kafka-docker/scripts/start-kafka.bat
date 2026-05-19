@echo off
cd /d "%~dp0.."
echo Starting Kafka (Docker)...
docker compose up -d
if errorlevel 1 exit /b 1
echo Waiting for broker on localhost:9092...
timeout /t 8 /nobreak >nul
docker exec kafka-broker /opt/kafka/bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092 >nul 2>&1
if errorlevel 1 (
  echo Broker may still be starting. Check: docker compose logs -f broker
) else (
  echo Kafka is ready at localhost:9092
)
