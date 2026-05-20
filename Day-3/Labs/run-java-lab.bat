@echo off
REM Usage: run-java-lab.bat com.kafka.producer.lab.BasicProducer [args...]
if "%~1"=="" (
  echo Usage: run-java-lab.bat MainClass [args...]
  exit /b 1
)
set MAIN=%~1
shift
cd /d "%~dp0java-kafka-producer-lab"
call mvn -q exec:java -Dexec.mainClass=%MAIN% -Dexec.args="%*"
