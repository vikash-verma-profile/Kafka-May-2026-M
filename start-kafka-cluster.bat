@echo off
REM Start KRaft controller + broker-1 + broker-3 in separate CMD windows.
REM Usage: start-kafka-cluster.bat
REM Optional: set KAFKA_HOME before running if your install path differs.

if "%KAFKA_HOME%"=="" set KAFKA_HOME=C:\kafka-bin\kafka_2.13-4.2.0

if not exist "%KAFKA_HOME%\bin\windows\kafka-server-start.bat" (
  echo Kafka not found at %KAFKA_HOME%
  echo Set KAFKA_HOME to your Kafka folder and run again.
  exit /b 1
)

if not exist "%KAFKA_HOME%\config\controller.properties" (
  echo Missing config\controller.properties under %KAFKA_HOME%
  exit /b 1
)

cd /d "%KAFKA_HOME%"

echo.
echo Kafka cluster startup
echo   KAFKA_HOME=%KAFKA_HOME%
echo   1^) Controller  - config\controller.properties  ^(CONTROLLER :9093^)
echo   2^) Broker-1    - config\broker-1.properties    ^(PLAINTEXT  :9092^)
echo   3^) Broker-2    - config\broker-2.properties    ^(PLAINTEXT  :9094^)
echo   4^) Broker-3    - config\broker-3.properties    ^(PLAINTEXT  :9095^)
echo.

echo Starting controller...
start "Kafka Controller" cmd /k "cd /d "%KAFKA_HOME%" && bin\windows\kafka-server-start.bat config\controller.properties"

echo Waiting for controller to initialize...
timeout /t 8 /nobreak >nul

echo Starting broker-1...
start "Kafka Broker 1" cmd /k "cd /d "%KAFKA_HOME%" && bin\windows\kafka-server-start.bat config\broker-1.properties"

timeout /t 4 /nobreak >nul

echo Starting broker-2...
start "Kafka Broker 2" cmd /k "cd /d "%KAFKA_HOME%" && bin\windows\kafka-server-start.bat config\broker-2.properties"

timeout /t 4 /nobreak >nul

echo Starting broker-3...
start "Kafka Broker 3" cmd /k "cd /d "%KAFKA_HOME%" && bin\windows\kafka-server-start.bat config\broker-3.properties"

echo.
echo Done. Three CMD windows should be open.
echo Wait until each shows "Kafka Server started" before running labs.
echo.
echo Bootstrap servers for this layout:
echo   localhost:9092,localhost:9094,localhost:9095
echo.
echo Stop: Ctrl+C in each window, or close the window.
exit /b 0
