$ErrorActionPreference = 'Stop'
$localEnvironment = Join-Path $PSScriptRoot '.env.local.ps1'
if (-not (Test-Path -LiteralPath $localEnvironment)) {
    throw "Missing $localEnvironment. Configure local secrets before starting the server."
}

. $localEnvironment
& (Join-Path $PSScriptRoot 'mvnw.cmd') spring-boot:run @args
