[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
& (Join-Path $PSScriptRoot 'bootstrap-mobile.ps1')

Push-Location (Join-Path $repoRoot 'mobile')
try {
    flutter analyze
    if ($LASTEXITCODE -ne 0) {
        throw 'Flutter 정적 분석이 실패했습니다.'
    }
    flutter test
    if ($LASTEXITCODE -ne 0) {
        throw 'Flutter 테스트가 실패했습니다.'
    }
}
finally {
    Pop-Location
}
