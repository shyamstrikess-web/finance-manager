@echo off
setlocal
echo Building the FinanceManager executable...
echo This will download the necessary dependencies and create a native Windows .exe installer.

:: Set JAVA_HOME to Android Studio's bundled Java 21 to avoid Java 26 compatibility issues with Gradle
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
echo Using Java version:
"%JAVA_HOME%\bin\java.exe" -version
echo.

:: Generate a local JKS truststore from the Windows Certificate Store using Java 26 keytool
:: This ensures that any local network proxy certificates (like NetFilterSDK) are trusted by Java 21.
if not exist "windows_truststore.jks" (
    echo Exporting Windows certificates to avoid SSL errors...
    "C:\Program Files\Java\jdk-26.0.2\bin\keytool.exe" -importkeystore -srckeystore NONE -srcstoretype Windows-ROOT -destkeystore windows_truststore.jks -deststorepass changeit >nul 2>&1
)

:: Run gradle using the custom truststore
call gradlew.bat jpackageImage "-Djavax.net.ssl.trustStore=%~dp0windows_truststore.jks" "-Djavax.net.ssl.trustStorePassword=changeit"

if errorlevel 1 goto failed

echo.
echo ==============================================
echo BUILD SUCCESSFUL!
echo You can find the installer .exe file in:
echo build\jpackage\
echo ==============================================
echo.
pause
exit /b 0

:failed
echo.
echo ==============================================
echo BUILD FAILED!
echo Please check the error messages above.
echo ==============================================
echo.
pause
exit /b 1
