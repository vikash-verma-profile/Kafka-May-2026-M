@echo off
cd /d "%~dp0kafka-java-lab"
call mvn -q exec:java -Dexec.mainClass=com.kafka.lab.SimpleProducer %*
