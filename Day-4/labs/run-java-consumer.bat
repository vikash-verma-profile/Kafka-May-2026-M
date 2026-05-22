@echo off
REM Usage: run-java-consumer.bat com.kafka.consumer.lab.BasicConsumer [args...]
if "%~1"=="" (
  echo Usage: run-java-consumer.bat MainClass [args...]
  exit /b 1
)
set MAIN=%~1
shift
cd /d "%~dp0java-kafka-consumer-lab"
REM Use %1..%9 after shift; %* on Windows can still include the main class name.
call mvn -q exec:java -Dexec.mainClass=%MAIN% "-Dexec.args=%1 %2 %3 %4 %5 %6 %7 %8 %9"
