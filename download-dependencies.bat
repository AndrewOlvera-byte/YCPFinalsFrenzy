@echo off
echo Downloading WebSocket dependencies for YCP Finals Frenzy...

rem Create lib directory if it doesn't exist
if not exist "war\WEB-INF\lib" mkdir "war\WEB-INF\lib"

rem Set the URLs for the required JARs
set WEBSOCKET_API_URL=https://repo1.maven.org/maven2/javax/websocket/javax.websocket-api/1.1/javax.websocket-api-1.1.jar
set TYRUS_SERVER_URL=https://repo1.maven.org/maven2/org/glassfish/tyrus/tyrus-server/1.13.1/tyrus-server-1.13.1.jar
set TYRUS_CLIENT_URL=https://repo1.maven.org/maven2/org/glassfish/tyrus/tyrus-client/1.13.1/tyrus-client-1.13.1.jar
set TYRUS_CONTAINER_URL=https://repo1.maven.org/maven2/org/glassfish/tyrus/tyrus-container-servlet/1.13.1/tyrus-container-servlet-1.13.1.jar
set TYRUS_CORE_URL=https://repo1.maven.org/maven2/org/glassfish/tyrus/tyrus-core/1.13.1/tyrus-core-1.13.1.jar
set TYRUS_SPI_URL=https://repo1.maven.org/maven2/org/glassfish/tyrus/tyrus-spi/1.13.1/tyrus-spi-1.13.1.jar
set GSON_URL=https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.5/gson-2.8.5.jar

rem Remove existing GSON 2.8.9 if it exists to prevent conflicts
echo Checking for existing GSON jar...
if exist "war\WEB-INF\lib\gson-2.8.9.jar" (
    echo Removing outdated GSON 2.8.9 jar...
    del "war\WEB-INF\lib\gson-2.8.9.jar"
)

rem Download the JARs using PowerShell (more reliable than curl on Windows)
echo Downloading WebSocket API...
powershell -Command "Invoke-WebRequest -Uri '%WEBSOCKET_API_URL%' -OutFile 'war\WEB-INF\lib\javax.websocket-api-1.1.jar'"

echo Downloading Tyrus Server...
powershell -Command "Invoke-WebRequest -Uri '%TYRUS_SERVER_URL%' -OutFile 'war\WEB-INF\lib\tyrus-server-1.13.1.jar'"

echo Downloading Tyrus Client...
powershell -Command "Invoke-WebRequest -Uri '%TYRUS_CLIENT_URL%' -OutFile 'war\WEB-INF\lib\tyrus-client-1.13.1.jar'"

echo Downloading Tyrus Container...
powershell -Command "Invoke-WebRequest -Uri '%TYRUS_CONTAINER_URL%' -OutFile 'war\WEB-INF\lib\tyrus-container-servlet-1.13.1.jar'"

echo Downloading Tyrus Core...
powershell -Command "Invoke-WebRequest -Uri '%TYRUS_CORE_URL%' -OutFile 'war\WEB-INF\lib\tyrus-core-1.13.1.jar'"

echo Downloading Tyrus SPI...
powershell -Command "Invoke-WebRequest -Uri '%TYRUS_SPI_URL%' -OutFile 'war\WEB-INF\lib\tyrus-spi-1.13.1.jar'"

echo Downloading Gson 2.8.5 (compatible version)...
powershell -Command "Invoke-WebRequest -Uri '%GSON_URL%' -OutFile 'war\WEB-INF\lib\gson-2.8.5.jar'"

echo.
echo Dependencies downloaded successfully! Please rebuild your project.
echo Note: GSON has been downgraded to version 2.8.5 to fix compatibility issues.
echo. 