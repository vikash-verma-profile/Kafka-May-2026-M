@echo off
if "%KAFKA_HOME%"=="" (
  echo Set KAFKA_HOME to your Kafka installation folder.
  exit /b 1
)

set BS=localhost:9092
set KT=%KAFKA_HOME%\bin\windows\kafka-topics.bat

call :create streams-input 3
call :create streams-output 3
call :create sentences 3
call :create word-counts 3
call :create orders-raw 6
call :create orders-metrics 6
call :create orders-critical 3
call :create orders 6
call :create order-analytics 6
call :create high-value-orders 3
call :create invalid-orders 3
echo Done.
exit /b 0

:create
"%KT%" --create --topic %1 --bootstrap-server %BS --partitions %2 --replication-factor 1 2>nul
echo Topic %1 (partitions=%2)
exit /b 0
