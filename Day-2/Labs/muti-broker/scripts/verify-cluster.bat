@echo off
set BS=localhost:9092,localhost:9094,localhost:9095
cd /d C:\kafka-bin\kafka_2.13-4.2.0
echo Bootstrap: %BS%
echo.
bin\windows\kafka-broker-api-versions.bat --bootstrap-server %BS%
echo.
bin\windows\kafka-topics.bat --bootstrap-server %BS% --list
