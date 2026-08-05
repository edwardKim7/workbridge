[CmdletBinding()]
param(
    [int]$Port = 8080,
    [string]$AdminLoginId = 'admin',
    [Parameter(Mandatory = $true)][SecureString]$AdminPassword,
    [string]$ApproverLoginId = 'approver',
    [Parameter(Mandatory = $true)][SecureString]$ApproverPassword
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$baseUrl = "http://localhost:$Port"
$adminPlain = [System.Net.NetworkCredential]::new('', $AdminPassword).Password
$approverPlain = [System.Net.NetworkCredential]::new('', $ApproverPassword).Password

function Get-CsrfToken {
    param([object]$Response)

    $input = $Response.InputFields | Where-Object { $_.name -eq '_csrf' } | Select-Object -First 1
    if ($input -and $input.value) {
        return [System.Net.WebUtility]::HtmlDecode($input.value)
    }
    $meta = [regex]::Match($Response.Content, 'name="_csrf"\s+content="([^"]+)"')
    if (-not $meta.Success) {
        $meta = [regex]::Match($Response.Content, 'content="([^"]+)"\s+name="_csrf"')
    }
    if (-not $meta.Success) {
        throw '화면에서 CSRF 토큰을 찾지 못했습니다.'
    }
    return [System.Net.WebUtility]::HtmlDecode($meta.Groups[1].Value)
}

function Get-CsrfHeaderName {
    param([object]$Response)
    $meta = [regex]::Match($Response.Content, 'name="_csrf_header"\s+content="([^"]+)"')
    if (-not $meta.Success) {
        $meta = [regex]::Match($Response.Content, 'content="([^"]+)"\s+name="_csrf_header"')
    }
    return $(if ($meta.Success) { [System.Net.WebUtility]::HtmlDecode($meta.Groups[1].Value) } else { 'X-CSRF-TOKEN' })
}

try {
    & (Join-Path $PSScriptRoot 'verify-running.ps1') -Port $Port

    $loginPage = Invoke-WebRequest -Uri "$baseUrl/login" -SessionVariable AdminSession -UseBasicParsing
    $loginCsrf = Get-CsrfToken -Response $loginPage
    $login = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -WebSession $AdminSession `
        -ContentType 'application/x-www-form-urlencoded' `
        -Body @{ loginId = $AdminLoginId; password = $adminPlain; _csrf = $loginCsrf } `
        -UseBasicParsing
    if ($login.Content -match '로그인 정보를 확인') {
        throw '관리자 세션 로그인에 실패했습니다.'
    }

    $interfacePage = Invoke-WebRequest -Uri "$baseUrl/web/admin/interfaces" -WebSession $AdminSession -UseBasicParsing
    $csrf = Get-CsrfToken -Response $interfacePage
    $csrfHeader = Get-CsrfHeaderName -Response $interfacePage
    $writeHeaders = @{ $csrfHeader = $csrf; Accept = 'application/json'; 'Content-Type' = 'application/json' }

    $approved = Invoke-RestMethod -Uri "$baseUrl/api/v1/approvals?status=APPROVED&page=0&size=20" `
        -WebSession $AdminSession -Method Get
    $approval = $approved.data.items | Where-Object { $_.businessType -eq 'OPPORTUNITY' } | Select-Object -First 1
    if ($null -eq $approval) {
        throw 'ERP 전송 검증에 사용할 승인 완료 영업기회를 찾지 못했습니다.'
    }

    $interface = Invoke-RestMethod -Uri "$baseUrl/api/v1/interfaces/erp/$($approval.businessId)/send" `
        -WebSession $AdminSession -Method Post -Headers $writeHeaders -Body '{"simulationMode":"NORMAL"}'
    if (-not $interface.success -or $interface.data.status -ne 'SUCCESS') {
        throw 'Mock ERP 정상 전송 결과가 SUCCESS가 아닙니다.'
    }

    $operations = Invoke-RestMethod -Uri "$baseUrl/api/v1/operations/summary" -WebSession $AdminSession -Method Get
    if (-not $operations.success) {
        throw '운영 요약 API 응답을 확인하지 못했습니다.'
    }

    $migrations = Invoke-RestMethod -Uri "$baseUrl/api/v1/migrations?page=0&size=5" -WebSession $AdminSession -Method Get
    if (-not $migrations.success) {
        throw '이관 목록 API 응답을 확인하지 못했습니다.'
    }

    if ($PSVersionTable.PSVersion.Major -ge 7) {
        $sample = Get-Item (Join-Path $repoRoot 'sample-data/customers-valid.csv')
        try {
            $migration = Invoke-RestMethod -Uri "$baseUrl/api/v1/migrations/customers" `
                -WebSession $AdminSession -Method Post -Headers @{ $csrfHeader = $csrf; Accept = 'application/json' } `
                -Form @{ file = $sample }
            if (-not $migration.success) {
                throw '정상 CSV 이관 응답이 성공이 아닙니다.'
            }
        }
        catch {
            $details = $_.ErrorDetails.Message
            if ($details -notmatch 'DUPLICATE_MIGRATION_FILE') {
                throw
            }
            Write-Host '정상 CSV는 이전 실행에서 이미 이관되어 중복 정책이 적용되었습니다.' -ForegroundColor Yellow
        }
    }
    else {
        Write-Host 'PowerShell 7 미만에서는 multipart 자동 검증을 건너뜁니다. 웹 이관 화면에서 샘플 CSV를 업로드해 주세요.' -ForegroundColor Yellow
    }

    $mobileLoginBody = @{
        loginId = $ApproverLoginId
        password = $approverPlain
        deviceInfo = 'PowerShell smoke test'
    } | ConvertTo-Json
    $mobileLogin = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method Post `
        -ContentType 'application/json' -Body $mobileLoginBody
    if (-not $mobileLogin.success -or [string]::IsNullOrWhiteSpace($mobileLogin.data.accessToken)) {
        throw '모바일 JWT 로그인에 실패했습니다.'
    }

    $bearer = @{ Authorization = "Bearer $($mobileLogin.data.accessToken)"; Accept = 'application/json' }
    $pending = Invoke-RestMethod -Uri "$baseUrl/api/v1/approvals?view=MY_APPROVALS&status=REQUESTED&page=0&size=20" `
        -Headers $bearer -Method Get
    if (-not $pending.success) {
        throw '모바일 결재 대기 API 응답을 확인하지 못했습니다.'
    }

    $refreshBody = @{
        refreshToken = $mobileLogin.data.refreshToken
        deviceInfo = 'PowerShell smoke test rotation'
    } | ConvertTo-Json
    $refreshed = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/refresh" -Method Post `
        -ContentType 'application/json' -Body $refreshBody
    if (-not $refreshed.success -or $refreshed.data.refreshToken -eq $mobileLogin.data.refreshToken) {
        throw 'Refresh Token 회전을 확인하지 못했습니다.'
    }

    $logoutHeaders = @{ Authorization = "Bearer $($refreshed.data.accessToken)"; Accept = 'application/json' }
    $logoutBody = @{ refreshToken = $refreshed.data.refreshToken } | ConvertTo-Json
    $logout = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/logout" -Method Post `
        -Headers $logoutHeaders -ContentType 'application/json' -Body $logoutBody
    if (-not $logout.success) {
        throw '모바일 로그아웃 응답을 확인하지 못했습니다.'
    }

    Write-Host '주요 업무 흐름의 스모크 테스트를 완료했습니다.' -ForegroundColor Green
    [PSCustomObject]@{
        InterfaceStatus = $interface.data.status
        InterfaceId = $interface.data.interfaceId
        PendingApprovals = $pending.data.totalElements
        RefreshRotated = $true
        MigrationBatches = $migrations.data.totalElements
        OperationTraceId = $operations.traceId
    } | Format-List
}
finally {
    $adminPlain = $null
    $approverPlain = $null
}
