@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)

set BS=%~1
if "%BS%"=="" set BS=localhost:9092

"%KAFKA_HOME%\bin\windows\kafka-producer-perf-test.bat" ^
  --topic bench ^
  --num-records 1000000 ^
  --record-size 1024 ^
  --throughput -1 ^
  --producer-props bootstrap.servers=%BS% batch.size=65536 linger.ms=20 compression.type=lz4 max.in.flight.requests.per.connection=5 enable.idempotence=true acks=all
