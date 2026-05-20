@echo off
cd /d "%~dp0.."
docker compose -f docker-compose-3brokers.yml down
echo 3-broker cluster stopped.
