@echo off
REM Start Elasticsearch + Kibana for Lab 04
cd /d "%~dp0"
echo Starting Elasticsearch (9200) and Kibana (5601)...
docker compose up -d
echo.
echo Wait ~1 minute, then check:
echo   curl http://localhost:9200
echo   http://localhost:5601
docker compose ps
