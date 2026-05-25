@echo off
cd /d "%~dp0..\java-serialization-lab"
call mvn -q compile exec:java -Dexec.mainClass=com.training.kafka.lab01.FourFormatsLab
