@echo off
rem One-time format for 3-broker local cluster (Kafka 4.2)
setlocal enabledelayedexpansion

set "KAFKA_HOME=C:\kafka-bin\kafka_2.13-4.2.0"
set "LAB_CONFIG=%~dp0..\config"
set "CLUSTER_ID_FILE=%~dp0..\cluster-id.txt"
set "CONTROLLERS_FILE=%~dp0..\initial-controllers.txt"

if not exist "%KAFKA_HOME%\bin\windows\kafka-storage.bat" (
  echo Kafka not found at %KAFKA_HOME%
  exit /b 1
)

echo Creating log directories...
mkdir C:\kafka-data\multi-broker-1 2>nul
mkdir C:\kafka-data\multi-broker-2 2>nul
mkdir C:\kafka-data\multi-broker-3 2>nul

echo Generating cluster ID...
for /f "delims=" %%i in ('"%KAFKA_HOME%\bin\windows\kafka-storage.bat" random-uuid 2^>nul') do set "CLUSTER_ID=%%i"
echo %CLUSTER_ID%> "%CLUSTER_ID_FILE%"
echo Cluster ID: %CLUSTER_ID%

echo Generating controller directory IDs...
for /f "delims=" %%i in ('"%KAFKA_HOME%\bin\windows\kafka-storage.bat" random-uuid 2^>nul') do set "DIR1=%%i"
for /f "delims=" %%i in ('"%KAFKA_HOME%\bin\windows\kafka-storage.bat" random-uuid 2^>nul') do set "DIR2=%%i"
for /f "delims=" %%i in ('"%KAFKA_HOME%\bin\windows\kafka-storage.bat" random-uuid 2^>nul') do set "DIR3=%%i"

set "INITIAL=1@localhost:9093:!DIR1!,2@localhost:9193:!DIR2!,3@localhost:9293:!DIR3!"
echo !INITIAL!> "%CONTROLLERS_FILE%"
echo Initial controllers: !INITIAL!

cd /d "%KAFKA_HOME%"

echo Formatting broker 1...
call bin\windows\kafka-storage.bat format -t %CLUSTER_ID% -c "%LAB_CONFIG%\broker1.properties" --initial-controllers "!INITIAL!"
if errorlevel 1 exit /b 1

echo Formatting broker 2...
call bin\windows\kafka-storage.bat format -t %CLUSTER_ID% -c "%LAB_CONFIG%\broker2.properties" --initial-controllers "!INITIAL!"
if errorlevel 1 exit /b 1

echo Formatting broker 3...
call bin\windows\kafka-storage.bat format -t %CLUSTER_ID% -c "%LAB_CONFIG%\broker3.properties" --initial-controllers "!INITIAL!"
if errorlevel 1 exit /b 1

echo.
echo Setup complete. Start brokers in 3 separate terminals:
echo   start-broker1.bat
echo   start-broker2.bat
echo   start-broker3.bat
endlocal
