@echo off
cd /d "%~dp0.."
docker compose down
echo Kafka Docker stack stopped.
