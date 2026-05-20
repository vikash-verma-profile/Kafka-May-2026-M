@echo off
cd /d C:\kafka-bin\kafka_2.13-4.2.0
echo Starting Broker 1 - PLAINTEXT localhost:9092
bin\windows\kafka-server-start.bat "%~dp0..\config\broker1.properties"
