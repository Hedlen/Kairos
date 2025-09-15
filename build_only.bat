@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Travel Light Camera - Build Only Script
echo ========================================
echo.

echo [INFO] [1/3] Checking ADB tools...
adb version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] ADB tool not found, please ensure Android SDK is installed and added to PATH
    echo Please install Android SDK or add platform-tools to system PATH
    pause
    exit /b 1
)
echo [SUCCESS] ADB tool check passed
echo.

echo [INFO] [2/3] Cleaning project...
call gradlew clean
if %errorlevel% neq 0 (
    echo [ERROR] Project clean failed
    pause
    exit /b 1
)
echo [SUCCESS] Project clean completed
echo.

echo [INFO] [3/3] Building project...
call gradlew assembleDebug
if %errorlevel% neq 0 (
    echo [ERROR] Project build failed
    echo Please check for syntax errors or dependency issues
    pause
    exit /b 1
)
echo [SUCCESS] Project build successful
echo.

set "APK_PATH=app\build\outputs\apk\debug\app-debug.apk"
if not exist "%APK_PATH%" (
    echo [ERROR] Cannot find compiled APK file
    echo Expected path: %APK_PATH%
    pause
    exit /b 1
)
echo [SUCCESS] Found APK file: %APK_PATH%
echo.

echo ========================================
echo [SUCCESS] Build process completed!
echo ========================================
echo.
echo Application package: com.travellight.camera
echo APK path: %APK_PATH%
echo APK size: 
dir "%APK_PATH%" | findstr app-debug.apk
echo.
echo To install and test on device, run:
echo build_and_test.bat
echo.
echo To install manually:
echo adb install -r "%APK_PATH%"
echo.

pause