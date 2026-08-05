[CmdletBinding()]
param(
    [switch]$RemoveData
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $repoRoot 'infra/.env'
$composeFile = Join-Path $repoRoot 'infra/docker-compose.yml'

if (-not (Test-Path $envFile)) {
    Write-Host 'infra/.env가 없어 실행 중인 구성을 특정할 수 없습니다.' -ForegroundColor Yellow
    exit 0
}

$arguments = @('compose', '--env-file', $envFile, '-f', $composeFile, 'down')
if ($RemoveData) {
    $arguments += '--volumes'
}

& docker @arguments
if ($LASTEXITCODE -ne 0) {
    throw 'Docker Compose 종료에 실패했습니다.'
}

if ($RemoveData) {
    Write-Host '컨테이너와 MySQL 개발 데이터 볼륨을 제거했습니다.' -ForegroundColor Green
}
else {
    Write-Host '컨테이너를 종료했습니다. MySQL 데이터 볼륨은 유지됩니다.' -ForegroundColor Green
}
