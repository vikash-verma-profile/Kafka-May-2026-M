@echo off
REM Forward Strimzi external listener ports to localhost (Docker Desktop on Windows).
REM Keep these windows open while using kafka-topics or other clients.
REM Bootstrap: localhost:30094

start "strimzi-pf-bootstrap" kubectl port-forward svc/my-cluster-kafka-external-bootstrap 30094:9094 -n kafka
start "strimzi-pf-broker0" kubectl port-forward svc/my-cluster-dual-role-0 30095:9094 -n kafka
start "strimzi-pf-broker1" kubectl port-forward svc/my-cluster-dual-role-1 30096:9094 -n kafka
start "strimzi-pf-broker2" kubectl port-forward svc/my-cluster-dual-role-2 30097:9094 -n kafka

echo Started 4 port-forwards. Connect clients to localhost:30094
echo Close the 4 "strimzi-pf-*" windows when done.
