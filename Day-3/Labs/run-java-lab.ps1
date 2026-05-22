# Usage: .\run-java-lab.ps1 com.kafka.producer.lab.BasicProducer [args...]
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$MainClass,
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$Remaining
)

$ErrorActionPreference = "Stop"
$projectDir = Join-Path $PSScriptRoot "java-kafka-producer-lab"
Set-Location $projectDir

if ($Remaining.Count -gt 0) {
    $execArgs = $Remaining -join " "
    mvn -q exec:java "-Dexec.mainClass=$MainClass" "-Dexec.args=$execArgs"
} else {
    mvn -q exec:java "-Dexec.mainClass=$MainClass"
}
