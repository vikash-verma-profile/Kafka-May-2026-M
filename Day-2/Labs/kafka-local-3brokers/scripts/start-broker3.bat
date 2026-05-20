@echo off
cd /d C:\kafka-bin\kafka_2.13-4.2.0
echo Starting Broker 3 - PLAINTEXT localhost:9095
bin\windows\kafka-server-start.bat "%~dp0..\config\broker3.properties"
