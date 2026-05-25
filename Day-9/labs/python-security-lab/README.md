# Day 9 Python Security Lab

Python helpers for SASL produce and consumer lag checks.

Broker must have SCRAM enabled for Lab 01. Use [../configs/client-scram.properties](../configs/client-scram.properties).

## Setup

```powershell
pip install -r requirements.txt
..\scripts\create-scram-user.bat
```

## Scripts

| Script | Lab |
|--------|-----|
| `lab01_scram_producer.py` | 01-SCRAM producer |
| `lab05_lag_check.py` | 05-print consumer lag |

ACL and TLS labs still use `kafka-acls.bat` / broker configs from [../scripts](../scripts/).
