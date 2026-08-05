[CmdletBinding()]
param(
    [int]$Port = 8080
)

$ErrorActionPreference = 'Stop'
$baseUrl = "http://localhost:$Port"
$traceId = "powershell-check-$([Guid]::NewGuid().ToString('N').Substring(0, 12))"

$ping = Invoke-WebRequest -Uri "$baseUrl/api/v1/system/ping" `
    -Headers @{ 'X-Trace-Id' = $traceId; Accept = 'application/json' } `
    -UseBasicParsing
$pingBody = $ping.Content | ConvertFrom-Json

if ($ping.StatusCode -ne 200 -or -not $pingBody.success) {
    throw 'System API 응답을 확인하지 못했습니다.'
}
if ($ping.Headers['X-Trace-Id'] -ne $traceId -or $pingBody.traceId -ne $traceId) {
    throw '요청·응답 Trace ID가 일치하지 않습니다.'
}
if ($pingBody.data.status -ne 'UP' -or $pingBody.data.database -ne 'UP') {
    throw '서버 또는 DB 상태가 UP이 아닙니다.'
}

$health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get
if ($health.status -ne 'UP') {
    throw 'WorkBridge Actuator 상태가 UP이 아닙니다.'
}

Write-Host '서버, DB, Trace ID와 Actuator 상태를 확인했습니다.' -ForegroundColor Green
$pingBody | ConvertTo-Json -Depth 5
