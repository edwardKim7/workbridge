[CmdletBinding()]
param(
    [switch]$UseDocker,
    [switch]$SkipTests
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$mavenArguments = @('-B', '-ntp', '-pl', 'server', '-am', 'clean', 'package')
if ($SkipTests) {
    $mavenArguments += '-DskipTests'
}

Push-Location $repoRoot
try {
    if (-not $UseDocker -and (Get-Command mvn -ErrorAction SilentlyContinue)) {
        & mvn @mavenArguments
    }
    elseif (Get-Command docker -ErrorAction SilentlyContinue) {
        docker run --rm --mount "type=bind,source=$repoRoot,target=/workspace" `
            --workdir /workspace `
            maven:3.9.11-eclipse-temurin-17 `
            mvn @mavenArguments
    }
    else {
        throw 'Maven 또는 Docker Desktop 중 하나가 필요합니다.'
    }

    if ($LASTEXITCODE -ne 0) {
        throw 'WAR 빌드가 실패했습니다.'
    }

    Write-Host "WAR 생성: $(Join-Path $repoRoot 'server/target/workbridge.war')" -ForegroundColor Green
}
finally {
    Pop-Location
}
