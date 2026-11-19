# X-02 并发压测脚本（BE 发起）
# 用法：
#   pwsh -File backend/scripts/loadtest-x02.ps1 -Mode returns -Count 200 -Concurrency 64
#   pwsh -File backend/scripts/loadtest-x02.ps1 -Mode remind  -Count 100 -Concurrency 64
# 前置：app.demo.reset=true 启动或已灌入 X-02 种子（temp/x02-seed.sql）。
param(
    [string]$BaseUrl = 'http://127.0.0.1:8125/api/v1',
    [string]$UserNo = 'LIB001',
    [string]$Password = '123456',
    [ValidateSet('returns', 'remind')]
    [string]$Mode = 'returns',
    [int]$Count = 0,
    [int]$Concurrency = 64
)

$ErrorActionPreference = 'Stop'

$login = Invoke-RestMethod -Method Post -Uri "$BaseUrl/auth/login" -ContentType 'application/json' `
    -Body (@{ userNo = $UserNo; password = $Password } | ConvertTo-Json)
if ($login.code -ne 0) { throw "登录失败：$($login.message)" }
$headers = @{ Authorization = "Bearer $($login.data.token)" }

if ($Mode -eq 'returns') {
    if ($Count -le 0) { $Count = 200 }
    $payloads = 1..$Count | ForEach-Object { @{ barcode = ('X02BC{0:D4}' -f $_) } | ConvertTo-Json -Compress }
    $uri = "$BaseUrl/returns"
} else {
    if ($Count -le 0) { $Count = 100 }
    $payloads = 1..$Count | ForEach-Object { @{ ids = @([string](900000000000040200 + $_)) } | ConvertTo-Json -Compress }
    $uri = "$BaseUrl/admin/remind/batch"
}

$watch = [System.Diagnostics.Stopwatch]::StartNew()
$results = $payloads | ForEach-Object -Parallel {
    $body = $_
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        $resp = Invoke-WebRequest -Method Post -Uri $using:uri -Headers $using:headers `
            -ContentType 'application/json' -Body $body -SkipHttpErrorCheck -TimeoutSec 30
        $code = [int]$resp.StatusCode
        $text = $resp.Content
    } catch {
        $code = -1
        $text = $_.Exception.Message
    }
    $sw.Stop()
    [pscustomobject]@{ ms = [math]::Round($sw.Elapsed.TotalMilliseconds, 1); status = $code; body = $text }
} -ThrottleLimit $Concurrency
$watch.Stop()

$sorted = ($results | Sort-Object ms).ms
function Percentile($values, [double]$p) {
    if ($values.Count -eq 0) { return 0 }
    $idx = [math]::Ceiling($p * $values.Count) - 1
    if ($idx -lt 0) { $idx = 0 }
    return $values[$idx]
}

$grouped = $results | Group-Object status | Sort-Object Name | ForEach-Object { "$($_.Name)=$($_.Count)" }
$fail = $results | Where-Object { $_.status -ge 500 -or $_.status -eq -1 }
$ok = $results | Where-Object { $_.status -eq 200 }
"mode=$Mode count=$($results.Count) concurrency=$Concurrency wall=$([math]::Round($watch.Elapsed.TotalSeconds,2))s"
"status: " + ($grouped -join ', ')
"latency ms: p50=$(Percentile $sorted 0.50) p95=$(Percentile $sorted 0.95) max=$(Percentile $sorted 1.00)"
"success200=$($ok.Count) serverError=$($fail.Count)"
if ($fail.Count -gt 0) { $fail | Select-Object -First 5 | ForEach-Object { "  $($_.status) $($_.body)" } }
if ($ok.Count -gt 0) { "sample: " + $ok[0].body }
