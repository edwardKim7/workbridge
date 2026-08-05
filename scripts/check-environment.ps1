[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'

function Get-ToolStatus {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][string]$VersionCommand,
        [bool]$RequiredForDocker = $false,
        [bool]$RequiredForMobile = $false
    )

    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if ($null -eq $command) {
        return [PSCustomObject]@{
            Tool = $Name
            Docker = $RequiredForDocker
            Mobile = $RequiredForMobile
            Installed = $false
            Version = '-'
        }
    }

    $global:LASTEXITCODE = 0
    $output = @(Invoke-Expression "$VersionCommand 2>&1")
    return [PSCustomObject]@{
        Tool = $Name
        Docker = $RequiredForDocker
        Mobile = $RequiredForMobile
        Installed = ($LASTEXITCODE -eq 0)
        Version = [string]($output | Select-Object -First 1)
    }
}

$results = @(
    Get-ToolStatus -Name 'git' -VersionCommand 'git --version' -RequiredForDocker $true
    Get-ToolStatus -Name 'docker' -VersionCommand 'docker --version' -RequiredForDocker $true
    Get-ToolStatus -Name 'java' -VersionCommand 'java -version'
    Get-ToolStatus -Name 'mvn' -VersionCommand 'mvn --version'
    Get-ToolStatus -Name 'flutter' -VersionCommand 'flutter --version' -RequiredForMobile $true
)

$results | Format-Table -AutoSize

if (Get-Command docker -ErrorAction SilentlyContinue) {
    docker compose version
    if ($LASTEXITCODE -ne 0) {
        throw 'Docker Compose 플러그인을 확인하지 못했습니다.'
    }
}

$missingDocker = $results | Where-Object { $_.Docker -and -not $_.Installed }
if ($missingDocker.Count -gt 0) {
    throw '웹·DB·Mock ERP Docker 실행에 필요한 Git 또는 Docker Desktop이 없습니다.'
}

if (($results | Where-Object { $_.Tool -eq 'flutter' }).Installed) {
    Write-Host 'Flutter 앱 생성·테스트·실행이 가능합니다.' -ForegroundColor Green
}
else {
    Write-Host 'Flutter SDK가 없어 모바일 앱은 아직 실행할 수 없습니다.' -ForegroundColor Yellow
}

Write-Host 'Docker 실행환경 기본 점검을 완료했습니다.' -ForegroundColor Green
