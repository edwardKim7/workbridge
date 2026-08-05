[CmdletBinding()]
param(
    [switch]$UseDocker,
    [switch]$SkipMobile
)

$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'test-server.ps1') -UseDocker:$UseDocker
& (Join-Path $PSScriptRoot 'test-mock-erp.ps1') -UseDocker:$UseDocker
if (-not $SkipMobile) {
    & (Join-Path $PSScriptRoot 'test-mobile.ps1')
}
Write-Host '요청한 전체 테스트 명령이 성공했습니다.' -ForegroundColor Green
