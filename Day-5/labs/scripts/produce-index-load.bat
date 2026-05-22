@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)

echo Producing 10000 records to index-lab...
"%KAFKA_HOME%\bin\windows\kafka-producer-perf-test.bat" ^
  --topic index-lab ^
  --num-records 10000 ^
  --record-size 100 ^
  --throughput -1 ^
  --producer-props bootstrap.servers=localhost:9092
