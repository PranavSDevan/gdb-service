$root = Split-Path -Parent $MyInvocation.MyCommand.Path

# Load environment variables from .env file if it exists
$envFile = Join-Path $root ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $key, $value = $line.Split("=", 2)
            [System.Environment]::SetEnvironmentVariable($key.Trim(), $value.Trim(), "Process")
        }
    }
}

$services = @(
    "auth-service",
    "account-service",
    "aadhar-service",
    "company-service",
    "payment-gateway-service",
    "transactions-service",
    "users-service",
    "credit-cards-service",
    "bank-statements-service",
    "settings-service",
    "ai-service"
)

Write-Host "Starting all services..." -ForegroundColor Cyan

foreach ($service in $services) {
    $path = Join-Path $root $service
    if (Test-Path $path) {
        Write-Host "Starting $service..." -ForegroundColor Green
        Start-Process "cmd.exe" -ArgumentList "/k cd /d `"$path`" && mvn spring-boot:run"
        Start-Sleep -Seconds 1
    } else {
        Write-Host "Skipping $service (not found)" -ForegroundColor Yellow
    }
}

$frontendPath = Join-Path $root "frontend"
if (Test-Path $frontendPath) {
    Write-Host "Starting frontend..." -ForegroundColor Green
    Start-Process "cmd.exe" -ArgumentList "/k cd /d `"$frontendPath`" && npm run dev"
}

Write-Host "Done! All windows launched." -ForegroundColor Cyan