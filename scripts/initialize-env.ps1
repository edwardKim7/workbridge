[CmdletBinding()]
param(
    [switch]$Force
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $repoRoot 'infra/.env'

if ((Test-Path $envFile) -and -not $Force) {
    Write-Host 'infra/.env가 이미 있어 기존 값을 유지합니다.' -ForegroundColor DarkGray
    exit 0
}

function New-HexSecret {
    param([int]$ByteLength)

    $bytes = New-Object byte[] $ByteLength
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $rng.GetBytes($bytes)
    }
    finally {
        $rng.Dispose()
    }
    return -join ($bytes | ForEach-Object { $_.ToString('x2') })
}

$lines = @(
    'DB_NAME=workbridge'
    'DB_USERNAME=workbridge'
    "DB_PASSWORD=$(New-HexSecret -ByteLength 18)"
    "DB_ROOT_PASSWORD=$(New-HexSecret -ByteLength 22)"
    'BIND_ADDRESS=127.0.0.1'
    'SERVER_PORT=8080'
    "JWT_SECRET=$(New-HexSecret -ByteLength 32)"
    'JWT_ACCESS_TTL=15m'
    'JWT_REFRESH_TTL=14d'
    'LOGIN_MAX_FAILURES=5'
    'LOGIN_LOCK_DURATION=15m'
    'SESSION_COOKIE_SECURE=false'
    'ERP_CONNECT_TIMEOUT=2s'
    'ERP_READ_TIMEOUT=3s'
    'ERP_MAX_ATTEMPTS=2'
    'ERP_RETRY_BACKOFF=250ms'
)

$utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllLines($envFile, $lines, $utf8WithoutBom)
Write-Host 'infra/.env에 로컬 전용 임의 비밀번호와 JWT 키를 생성했습니다.' -ForegroundColor Green
