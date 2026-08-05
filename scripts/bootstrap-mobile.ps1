[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$mobileDir = Join-Path $repoRoot 'mobile'

if (-not (Get-Command flutter -ErrorAction SilentlyContinue)) {
    throw 'Flutter SDK가 필요합니다. flutter --version이 동작하도록 PATH를 설정해 주세요.'
}

Push-Location $mobileDir
try {
    if (-not (Test-Path (Join-Path $mobileDir 'android'))) {
        flutter create --platforms=android --org com.workbridge --project-name workbridge_mobile .
        if ($LASTEXITCODE -ne 0) {
            throw 'Android 호스트 프로젝트 생성에 실패했습니다.'
        }
    }

    $manifest = Join-Path $mobileDir 'android/app/src/main/AndroidManifest.xml'
    if (Test-Path $manifest) {
        $content = Get-Content $manifest -Raw
        if ($content -notmatch 'usesCleartextTraffic') {
            $content = $content -replace '<application', '<application android:usesCleartextTraffic="true"'
            [System.IO.File]::WriteAllText($manifest, $content, (New-Object System.Text.UTF8Encoding($false)))
        }
    }

    flutter pub get
    if ($LASTEXITCODE -ne 0) {
        throw 'Flutter 패키지 설치에 실패했습니다.'
    }

    Write-Host 'Flutter Android 호스트와 패키지 준비를 완료했습니다.' -ForegroundColor Green
}
finally {
    Pop-Location
}
