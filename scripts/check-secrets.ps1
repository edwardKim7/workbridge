[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repoRoot = [System.IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$repoRootPrefix = $repoRoot.TrimEnd([char[]]@('\', '/')) + [System.IO.Path]::DirectorySeparatorChar

function Get-RepositoryRelativePath {
    param([Parameter(Mandatory)][string]$FullName)

    $absolutePath = [System.IO.Path]::GetFullPath($FullName)
    if (-not $absolutePath.StartsWith($repoRootPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "저장소 밖의 경로입니다: $absolutePath"
    }

    return $absolutePath.Substring($repoRootPrefix.Length).Replace('\', '/')
}

$blockedFileNames = @(
    '.npmrc',
    '.pypirc',
    'application-secret.yml',
    'application-local.yml',
    'credentials.json',
    'google-services.json',
    'service-account.json',
    'id_rsa',
    'id_ed25519',
    'id_ecdsa'
)

$blockedExtensions = @('.key', '.pem', '.ppk', '.p12', '.pfx', '.jks', '.keystore')

$patterns = [ordered]@{
    'PEM private key' = '-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----'
    'AWS access key' = 'AKIA[0-9A-Z]{16}'
    'GitHub token' = 'gh[pousr]_[A-Za-z0-9]{30,}'
    'API key with sk prefix' = 'sk-[A-Za-z0-9_-]{20,}'
    'Google API key' = 'AIza[0-9A-Za-z_-]{35}'
    'Google service account' = '"type"\s*:\s*"service_account"'
    'Slack token' = 'xox[baprs]-[0-9A-Za-z-]{20,}'
    'Stripe live secret' = 'sk_live_[0-9A-Za-z]{16,}'
    'npm access token' = 'npm_[0-9A-Za-z]{20,}'
}

$ignoredDirectories = @('.git', 'target', 'build', '.dart_tool')
$findings = [System.Collections.Generic.List[string]]::new()

$files = Get-ChildItem -Path $repoRoot -Recurse -File | Where-Object {
    $relativePath = Get-RepositoryRelativePath -FullName $_.FullName
    $pathSegments = $relativePath -split '/'
    -not ($pathSegments | Where-Object { $ignoredDirectories -contains $_ })
}

foreach ($file in $files) {
    $relativePath = Get-RepositoryRelativePath -FullName $file.FullName

    $isEnvironmentFile = $file.Name -eq '.env' -or
        ($file.Name -like '.env.*' -and $file.Name -ne '.env.example')
    $hasBlockedExtension = $blockedExtensions -contains $file.Extension.ToLowerInvariant()

    if ($blockedFileNames -contains $file.Name -or $isEnvironmentFile -or $hasBlockedExtension) {
        $findings.Add("금지된 비밀 파일 후보: $relativePath")
        continue
    }

    if ($file.Length -gt 2MB) {
        continue
    }

    try {
        $content = Get-Content -Path $file.FullName -Raw -ErrorAction Stop
    }
    catch {
        continue
    }

    foreach ($entry in $patterns.GetEnumerator()) {
        if ($content -match $entry.Value) {
            $findings.Add("$($entry.Key): $relativePath")
        }
    }
}

if ($findings.Count -gt 0) {
    $findings | Sort-Object -Unique | ForEach-Object { Write-Error $_ }
    throw '커밋하면 안 되는 비밀값 후보가 발견되었습니다.'
}

Write-Host '고신뢰 비밀값 패턴과 금지 파일명을 확인했습니다.' -ForegroundColor Green
Write-Host '이 검사는 보조 수단입니다. GitHub Secret Scanning과 수동 검토도 함께 사용하세요.' -ForegroundColor DarkGray
