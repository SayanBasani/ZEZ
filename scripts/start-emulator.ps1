# Starts or verifies the Android Emulator
param(
    [string]$AvdName = "Pixel_6",
    [switch]$ColdBoot = $false,
    [switch]$WipeData = $false
)

$ErrorActionPreference = "Continue"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  Checking Android Device / Emulator...  " -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# Check if a device is already online
$Devices = adb devices | Where-Object { $_ -match "\s+device$" }
if ($Devices) {
    Write-Host "Active Android device/emulator already online!" -ForegroundColor Green
    adb devices
    exit 0
}

# Check if an emulator is stuck in offline state
$OfflineDevices = adb devices | Where-Object { $_ -match "emulator.*offline" }
if ($OfflineDevices) {
    Write-Host "Found hung offline emulator. Restarting ADB and cleaning locks..." -ForegroundColor Yellow
    Stop-Process -Name qemu-system-x86_64, emulator -Force -ErrorAction SilentlyContinue
    adb kill-server
    adb start-server
}

# Check for stale lock files in AVD folder
$AvdPath = "$env:USERPROFILE\.android\avd\$AvdName.avd"
if (Test-Path $AvdPath) {
    $Locks = Get-ChildItem -Path $AvdPath -Filter "*.lock" -ErrorAction SilentlyContinue
    if ($Locks) {
        Write-Host "Removing stale lock files from $AvdPath..." -ForegroundColor Yellow
        $Locks | Remove-Item -Force -Recurse -ErrorAction SilentlyContinue
    }
}

$EmulatorExe = "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe"
if (-not (Test-Path $EmulatorExe)) {
    Write-Error "Emulator executable not found at: $EmulatorExe"
    exit 1
}

$ArgsList = @("-avd", $AvdName)

if ($WipeData) {
    $ArgsList += "-wipe-data"
    Write-Host "Launching with -wipe-data..." -ForegroundColor Yellow
} elseif ($ColdBoot) {
    $ArgsList += "-no-snapshot-load"
    Write-Host "Launching with -no-snapshot-load (cold boot)..." -ForegroundColor Yellow
}

Write-Host "Starting emulator '$AvdName' in standalone window..." -ForegroundColor Cyan
Start-Process -FilePath $EmulatorExe -ArgumentList $ArgsList -WindowStyle Normal

Write-Host "Waiting for emulator to boot up (this may take 1-2 minutes on first boot)..." -ForegroundColor Yellow
$MaxWaitSeconds = 180
$Elapsed = 0

while ($Elapsed -lt $MaxWaitSeconds) {
    Start-Sleep -Seconds 5
    $Elapsed += 5
    $BootCompleted = adb shell getprop sys.boot_completed 2>$null
    if ($BootCompleted -match "1") {
        Write-Host "Emulator is fully booted and ready!" -ForegroundColor Green
        adb devices
        exit 0
    }
    Write-Host "[$Elapsed s] Still booting... please wait." -ForegroundColor Gray
}

Write-Warning "Emulator started but sys.boot_completed did not return 1 within $MaxWaitSeconds seconds. It may still be finishing background optimization."
exit 0
