@echo off
cd /d "%~dp0kafka-dotnet-lab\src\KafkaDotNetLab"
dotnet run -- producer %*
