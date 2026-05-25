@echo off
cd /d "%~dp0..\java-kafka-streams-lab"
mvn -q exec:java -Dexec.mainClass=com.training.kafka.streams.lab02.WordCountApp
