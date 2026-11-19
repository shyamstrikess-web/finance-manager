@echo off
setlocal
title Finance Manager

echo ==============================================
echo       Starting Finance Manager Application
echo ==============================================

:: Ensure XAMPP MySQL and Apache are running if installed
if exist "C:\xampp\mysql\bin\mysqld.exe" (
    powershell -NoProfile -Command "if (-not (Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue)) { Start-Process -FilePath 'C:\xampp\mysql\bin\mysqld.exe' -ArgumentList '--defaults-file=C:\xampp\mysql\bin\my.ini --standalone' -WindowStyle Hidden }"
)
if exist "C:\xampp\apache\bin\httpd.exe" (
    powershell -NoProfile -Command "if (-not (Get-NetTCPConnection -LocalPort 80 -State Listen -ErrorAction SilentlyContinue)) { Start-Process -FilePath 'C:\xampp\apache\bin\httpd.exe' -ArgumentList '-d C:\xampp\apache' -WindowStyle Hidden }"
)

:: Check if the native package executable exists
if exist "%~dp0build\jpackage\FinanceManager\FinanceManager.exe" (
    echo Launching FinanceManager...
    cd /d "%~dp0build\jpackage\FinanceManager"
    start "" "FinanceManager.exe"
    exit /b 0
)

:: Otherwise fallback to gradle run
echo Native executable not found, running via Gradle...
cd /d "%~dp0"
call gradlew.bat run
