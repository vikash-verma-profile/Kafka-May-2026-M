@echo off
cd /d "%~dp0kafka-python-lab"
if not exist ".venv\Scripts\python.exe" (
  echo Run setup first: cd kafka-python-lab ^&^& python -m venv .venv ^&^& .venv\Scripts\pip install -r requirements.txt
  exit /b 1
)
call .venv\Scripts\python.exe simple_consumer.py %*
