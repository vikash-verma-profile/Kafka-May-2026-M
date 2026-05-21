@echo off
cd /d "%~dp0..\java-kafka-consumer-lab"
call mvn -q exec:java -Dexec.mainClass=com.kafka.consumer.lab.DemoTopicSeeder
