@echo off
rem Format controller + 3 brokers using C:\kafka-bin\kafka_2.13-4.2.0\config
setlocal enabledelayedexpansion

set "KAFKA_HOME=C:\kafka-bin\kafka_2.13-4.2.0"
set "KAFKA_CONFIG=%KAFKA_HOME%\config"

if not exist "%KAFKA_HOME%\bin\windows\kafka-storage.bat" (
  echo Kafka not found at %KAFKA_HOME%
  exit /b 1
)

echo Creating log directories...
mkdir C:\kafka-data\kraft-controller-logs 2>nul
mkdir C:\kafka-data\kraft-broker-logs-1 2>nul
mkdir C:\kafka-data\kraft-broker-logs-2 2>nul
mkdir C:\kafka-data\kraft-broker-logs-3 2>nul

for /f "delims=" %%i in ('"%KAFKA_HOME%\bin\windows\kafka-storage.bat" random-uuid 2^>nul') do set "CLUSTER_ID=%%i"
echo %CLUSTER_ID%> "%~dp0..\cluster-id.txt"
echo Cluster ID: %CLUSTER_ID%

cd /d "%KAFKA_HOME%"

echo [1/4] Format controller (config\controller.properties)...
call bin\windows\kafka-storage.bat format -t %CLUSTER_ID% -c config\controller.properties --standalone
if errorlevel 1 exit /b 1

echo [2/4] Format broker-1 (config\broker-1.properties)...
call bin\windows\kafka-storage.bat format -t %CLUSTER_ID% -c config\broker-1.properties --no-initial-controllers
if errorlevel 1 exit /b 1

echo [3/4] Format broker-2 (config\broker-2.properties)...
call bin\windows\kafka-storage.bat format -t %CLUSTER_ID% -c config\broker-2.properties --no-initial-controllers
if errorlevel 1 exit /b 1

echo [4/4] Format broker-3 (config\broker-3.properties)...
call bin\windows\kafka-storage.bat format -t %CLUSTER_ID% -c config\broker-3.properties --no-initial-controllers
if errorlevel 1 exit /b 1

echo.
echo Done. Start: controller, then broker-1, broker-2, broker-3.
endlocal
