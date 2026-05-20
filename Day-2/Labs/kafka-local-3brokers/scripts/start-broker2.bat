@echo off
cd /d C:\kafka-bin\kafka_2.13-4.2.0
echo Starting Broker 2 - PLAINTEXT localhost:9094
bin\windows\kafka-server-start.bat "%~dp0..\config\broker2.properties"
