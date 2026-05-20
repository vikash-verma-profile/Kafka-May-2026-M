@echo off
cd /d C:\kafka-bin\kafka_2.13-4.2.0
echo Starting CONTROLLER (config\controller.properties) on port 9093
bin\windows\kafka-server-start.bat config\controller.properties
