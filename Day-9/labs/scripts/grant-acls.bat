@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)

set BS=%~1
if "%BS%"=="" set BS=localhost:9096
set CFG=%~dp0..\my-config\client-scram-oneshot.properties

echo Grant alice Write on orders...
"%KAFKA_HOME%\bin\windows\kafka-acls.bat" --bootstrap-server %BS% ^
  --command-config "%CFG%" ^
  --add --allow-principal User:alice --operation Write --topic orders

echo Grant bob Read on orders + group billing-svc...
"%KAFKA_HOME%\bin\windows\kafka-acls.bat" --bootstrap-server %BS% ^
  --command-config "%CFG%" ^
  --add --allow-principal User:bob --operation Read --topic orders --group billing-svc

"%KAFKA_HOME%\bin\windows\kafka-acls.bat" --bootstrap-server %BS% ^
  --command-config "%CFG%" ^
  --add --allow-principal User:bob --operation Describe --group billing-svc

echo List ACLs for orders:
"%KAFKA_HOME%\bin\windows\kafka-acls.bat" --bootstrap-server %BS% ^
  --command-config "%CFG%" --list --topic orders
