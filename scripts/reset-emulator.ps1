# Completely stops and resets the Android Emulator with a clean wipe
param(
    [string]$AvdName = "Pixel_6"
)

Write-Host "Stopping all emulator and adb processes..." -ForegroundColor Yellow
Stop-Process -Name qemu-system-x86_64, emulator -Force -ErrorAction SilentlyContinue
adb kill-server
adb start-server

$AvdPath = "$env:USERPROFILE\.android\avd\$AvdName.avd"
if (Test-Path $AvdPath) {
    Write-Host "Removing lock files..." -ForegroundColor Yellow
    Get-ChildItem -Path $AvdPath -Filter "*.lock" -ErrorAction SilentlyContinue | Remove-Item -Force -Recurse -ErrorAction SilentlyContinue
}

Write-Host "Launching emulator with clean data wipe (-wipe-data -no-snapshot-load)..." -ForegroundColor Cyan
& "$PSScriptRoot\start-emulator.ps1" -AvdName $AvdName -WipeData
