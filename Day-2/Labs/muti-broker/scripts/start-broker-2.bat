@echo off
cd /d C:\kafka-bin\kafka_2.13-4.2.0
echo Starting BROKER 2 (config\broker-2.properties) on port 9094, node.id=3
bin\windows\kafka-server-start.bat config\broker-2.properties
