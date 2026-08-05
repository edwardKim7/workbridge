[CmdletBinding()]
param(
    [string]$ApiBaseUrl = 'http://10.0.2.2:8080'
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
& (Join-Path $PSScriptRoot 'bootstrap-mobile.ps1')

Push-Location (Join-Path $repoRoot 'mobile')
try {
    flutter run --dart-define="API_BASE_URL=$ApiBaseUrl"
}
finally {
    Pop-Location
}
