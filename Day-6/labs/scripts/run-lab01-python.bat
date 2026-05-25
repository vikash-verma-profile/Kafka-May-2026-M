@echo off
cd /d "%~dp0..\python-serialization-lab"
pip install -r requirements.txt -q
python lab01_four_formats.py
