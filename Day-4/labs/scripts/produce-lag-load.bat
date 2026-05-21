@echo off
REM Prefer kafka-producer-perf-test; fall back to Java LagLoadProducer
if "%KAFKA_HOME%"=="" (
  echo KAFKA_HOME not set - using Java LagLoadProducer
  goto :java
)

if exist "%KAFKA_HOME%\bin\windows\kafka-producer-perf-test.bat" (
  echo Using kafka-producer-perf-test (10000 records)...
  "%KAFKA_HOME%\bin\windows\kafka-producer-perf-test.bat" ^
    --topic lag-demo ^
    --num-records 10000 ^
    --record-size 100 ^
    --throughput 200 ^
    --producer-props bootstrap.servers=localhost:9092
  exit /b 0
)

:java
echo Using Java LagLoadProducer...
cd /d "%~dp0..\java-kafka-consumer-lab"
call mvn -q exec:java -Dexec.mainClass=com.kafka.consumer.lab.LagLoadProducer
