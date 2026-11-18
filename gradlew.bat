@echo off
setlocal
set GRADLE_VERSION=9.2.1

where gradle >nul 2>nul
if %ERRORLEVEL%==0 (
    gradle %*
    exit /b %ERRORLEVEL%
)

if "%GRADLE_USER_HOME%"=="" set GRADLE_USER_HOME=%USERPROFILE%\.gradle
set BOOTSTRAP=%GRADLE_USER_HOME%\deadly-bootstrap
set GRADLE_HOME=%BOOTSTRAP%\gradle-%GRADLE_VERSION%
set ZIP_FILE=%BOOTSTRAP%\gradle-%GRADLE_VERSION%-bin.zip

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
    if not exist "%BOOTSTRAP%" mkdir "%BOOTSTRAP%"
    if not exist "%ZIP_FILE%" (
        powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP_FILE%'"
        if errorlevel 1 exit /b 1
    )
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%ZIP_FILE%' -DestinationPath '%BOOTSTRAP%' -Force"
    if errorlevel 1 exit /b 1
)

call "%GRADLE_HOME%\bin\gradle.bat" %*
exit /b %ERRORLEVEL%
