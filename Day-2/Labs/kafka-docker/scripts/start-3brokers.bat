@echo off
cd /d "%~dp0.."
echo Stopping single-broker stack if running...
docker compose down 2>nul
echo Starting 3-broker cluster...
docker compose -f docker-compose-3brokers.yml up -d
if errorlevel 1 exit /b 1
echo Waiting for brokers (about 20 seconds)...
timeout /t 20 /nobreak >nul
echo Bootstrap servers: localhost:9092,localhost:9093,localhost:9094
docker compose -f docker-compose-3brokers.yml ps
