@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)

echo Seeding 50 messages into retention-lab...
powershell -NoProfile -Command "1..50 | ForEach-Object { 'msg-' + $_ } | & '%KAFKA_HOME%\bin\windows\kafka-console-producer.bat' --bootstrap-server localhost:9092 --topic retention-lab"
echo Done.
