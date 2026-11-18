# Builds, installs, and runs the Zez Android App on the connected device/emulator
param(
    [switch]$SkipBuild = $false,
    [switch]$StreamLogs = $false
)

$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $PSScriptRoot
$AndroidDir = Join-Path $Root "ZezAndroid"
$ApkPath = Join-Path $AndroidDir "app\build\outputs\apk\debug\app-debug.apk"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  Zez Android App - Build & Run          " -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# 1. Ensure a device or emulator is available
$Devices = adb devices | Where-Object { $_ -match "\s+device$" }
if (-not $Devices) {
    Write-Host "No online device/emulator found. Starting emulator..." -ForegroundColor Yellow
    & "$PSScriptRoot\start-emulator.ps1"
}

# 2. Build the app (if not skipped)
if (-not $SkipBuild) {
    Write-Host "Compiling native Java debug APK with Gradle..." -ForegroundColor Cyan
    Set-Location $AndroidDir
    & ".\gradlew.bat" assembleDebug
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Gradle build failed."
        exit $LASTEXITCODE
    }
}

if (-not (Test-Path $ApkPath)) {
    Write-Error "APK file not found at $ApkPath"
    exit 1
}

# 3. Install the APK
Write-Host "Installing APK to device ($ApkPath)..." -ForegroundColor Cyan
adb install -r -d $ApkPath
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to install APK onto device."
    exit $LASTEXITCODE
}

# 4. Grant runtime permissions automatically
Write-Host "Granting required permissions..." -ForegroundColor Cyan
adb shell pm grant com.zez.app android.permission.READ_CALL_LOG 2>$null
adb shell pm grant com.zez.app android.permission.READ_MEDIA_IMAGES 2>$null
adb shell pm grant com.zez.app android.permission.READ_EXTERNAL_STORAGE 2>$null

# 5. Launch the MainActivity
Write-Host "Launching com.zez.app/.MainActivity..." -ForegroundColor Green
adb shell am start -n com.zez.app/.MainActivity

Write-Host "=========================================" -ForegroundColor Green
Write-Host "  App successfully launched on device!   " -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green

# 6. Stream logs if requested
if ($StreamLogs) {
    Write-Host "Streaming application logcat (Ctrl+C to stop)..." -ForegroundColor Yellow
    adb logcat -v time -s ZezApiClient:V MainActivity:V CallSyncWorker:V AndroidRuntime:E
}
