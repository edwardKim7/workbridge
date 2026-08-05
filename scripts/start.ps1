[CmdletBinding()]
param(
    [switch]$Rebuild
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $repoRoot 'infra/.env'
$composeFile = Join-Path $repoRoot 'infra/docker-compose.yml'

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Docker Desktop이 필요합니다. scripts/check-environment.ps1을 먼저 실행해 주세요.'
}

docker info *> $null
if ($LASTEXITCODE -ne 0) {
    throw 'Docker Desktop 엔진이 실행 중인지 확인해 주세요.'
}

if (-not (Test-Path $envFile)) {
    & (Join-Path $PSScriptRoot 'initialize-env.ps1')
}

$arguments = @('compose', '--env-file', $envFile, '-f', $composeFile, 'up', '-d')
if ($Rebuild) {
    $arguments += '--build'
}

& docker @arguments
if ($LASTEXITCODE -ne 0) {
    throw 'Docker Compose 실행에 실패했습니다.'
}

$settings = @{}
Get-Content $envFile | ForEach-Object {
    if ($_ -match '^([^#=]+)=(.*)$') {
        $settings[$matches[1]] = $matches[2]
    }
}
$serverPort = if ($settings.SERVER_PORT) { $settings.SERVER_PORT } else { '8080' }

Write-Host ''
Write-Host 'WorkBridge 전체 구성의 시작 명령을 완료했습니다.' -ForegroundColor Green
Write-Host "웹 로그인      : http://localhost:$serverPort/login"
Write-Host "전자결재       : http://localhost:$serverPort/web/approvals"
Write-Host "인터페이스 운영 : http://localhost:$serverPort/web/admin/interfaces"
Write-Host "데이터 이관     : http://localhost:$serverPort/web/admin/migrations"
Write-Host "시스템 운영     : http://localhost:$serverPort/web/admin/operations"
Write-Host "Swagger UI     : http://localhost:$serverPort/swagger-ui.html"
Write-Host '상태 확인       : docker compose --env-file infra/.env -f infra/docker-compose.yml ps' -ForegroundColor DarkGray
