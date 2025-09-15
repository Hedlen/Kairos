@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Travel Light Camera - Build and Test Script
echo ========================================
echo.

echo [INFO] [1/6] Checking ADB tools...
adb version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] ADB tool not found, please ensure Android SDK is installed and added to PATH
    echo Please install Android SDK or add platform-tools to system PATH
    pause
    exit /b 1
)
echo [SUCCESS] ADB tool check passed
echo.

echo [INFO] [2/6] Checking Android device connection...
adb devices | findstr "device$" >nul
if %errorlevel% neq 0 (
    echo [ERROR] No connected Android device detected
    echo Please ensure:
    echo 1. Device is connected to computer via USB
    echo 2. USB debugging is enabled on device
    echo 3. Computer is authorized for debugging
    echo.
    echo Current device status:
    adb devices
    pause
    exit /b 1
)
echo [SUCCESS] Device connection check passed
echo Connected devices:
adb devices
echo.

echo [INFO] [3/6] Cleaning project...
call gradlew clean
if %errorlevel% neq 0 (
    echo [ERROR] Project clean failed
    pause
    exit /b 1
)
echo [SUCCESS] Project clean completed
echo.

echo [INFO] [4/6] Building project...
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

echo [INFO] [5/6] Installing APK to device...
adb install -r "%APK_PATH%"
if %errorlevel% neq 0 (
    echo [ERROR] APK installation failed
    echo Possible reasons:
    echo 1. Insufficient device storage
    echo 2. App signature issues
    echo 3. Device permission restrictions
    pause
    exit /b 1
)
echo [SUCCESS] APK installation successful
echo.

echo [INFO] [6/6] Launching application...
adb shell am start -n com.travellight.camera/.MainActivity
if %errorlevel% neq 0 (
    echo [WARN] Application launch may have failed, please check device manually
) else (
    echo [SUCCESS] Application launched successfully
)
echo.

echo ========================================
echo [SUCCESS] Build and test process completed!
echo ========================================
echo.
echo Application package: com.travellight.camera
echo APK path: %APK_PATH%
echo.
echo To view application logs, run:
echo adb logcat -s TravelLightCamera
echo.
echo To uninstall application, run:
echo adb uninstall com.travellight.camera
echo.

pause