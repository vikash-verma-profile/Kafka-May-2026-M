@echo off
cd /d C:\kafka-bin\kafka_2.13-4.2.0
echo Starting BROKER 1 (config\broker-1.properties) on port 9092, node.id=2
bin\windows\kafka-server-start.bat config\broker-1.properties
