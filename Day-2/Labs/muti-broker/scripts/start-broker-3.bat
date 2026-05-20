@echo off
cd /d C:\kafka-bin\kafka_2.13-4.2.0
echo Starting BROKER 3 (config\broker-3.properties) on port 9095, node.id=4
bin\windows\kafka-server-start.bat config\broker-3.properties
