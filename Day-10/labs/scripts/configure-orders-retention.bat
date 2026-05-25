@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)

set BS=%~1
if "%BS%"=="" set BS=localhost:9092

"%KAFKA_HOME%\bin\windows\kafka-topics.bat" --create ^
  --topic orders --bootstrap-server %BS% --partitions 6 --replication-factor 1 2>nul

"%KAFKA_HOME%\bin\windows\kafka-configs.bat" --bootstrap-server %BS% ^
  --entity-type topics --entity-name orders ^
  --alter --add-config segment.bytes=134217728,retention.ms=604800000

"%KAFKA_HOME%\bin\windows\kafka-configs.bat" --bootstrap-server %BS% ^
  --entity-type topics --entity-name orders --describe
