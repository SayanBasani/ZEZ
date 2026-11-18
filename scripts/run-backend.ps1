# Starts the Zez Express/PostgreSQL Backend Server
$ErrorActionPreference = "Stop"

$BackendDir = Join-Path $PSScriptRoot "..\Zez-Backend"
Set-Location $BackendDir

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  Starting Zez Backend Server...         " -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# Check if node_modules exists
if (-not (Test-Path "$BackendDir\node_modules")) {
    Write-Host "node_modules not found. Running npm install..." -ForegroundColor Yellow
    npm install
}

# Check if port 3000 is occupied
$Occupied = Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue
if ($Occupied) {
    $PidUsingPort = $Occupied.OwningProcess | Select-Object -First 1
    Write-Host "Port 3000 is currently occupied by PID $PidUsingPort." -ForegroundColor Yellow
    Write-Host "Reusing running backend or restart PID $PidUsingPort if needed." -ForegroundColor Yellow
}

Write-Host "Starting server on http://localhost:3000..." -ForegroundColor Green
node src/server.js
