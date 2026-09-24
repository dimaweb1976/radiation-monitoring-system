$ErrorActionPreference = 'Stop'
Write-Host 'Starting demo profile. Simulators will write to the configured database.'
& (Join-Path $PSScriptRoot 'run-local.ps1') '-Dspring-boot.run.profiles=demo' @args
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
