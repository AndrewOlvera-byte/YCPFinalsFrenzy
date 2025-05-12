#!/bin/bash
echo "Downloading WebSocket dependencies for YCP Finals Frenzy..."

# Create lib directory if it doesn't exist
mkdir -p war/WEB-INF/lib

# Set the URLs for the required JARs
WEBSOCKET_API_URL="https://repo1.maven.org/maven2/javax/websocket/javax.websocket-api/1.1/javax.websocket-api-1.1.jar"
TYRUS_SERVER_URL="https://repo1.maven.org/maven2/org/glassfish/tyrus/tyrus-server/1.13.1/tyrus-server-1.13.1.jar"
TYRUS_CLIENT_URL="https://repo1.maven.org/maven2/org/glassfish/tyrus/tyrus-client/1.13.1/tyrus-client-1.13.1.jar"
TYRUS_CONTAINER_URL="https://repo1.maven.org/maven2/org/glassfish/tyrus/tyrus-container-servlet/1.13.1/tyrus-container-servlet-1.13.1.jar"
TYRUS_CORE_URL="https://repo1.maven.org/maven2/org/glassfish/tyrus/tyrus-core/1.13.1/tyrus-core-1.13.1.jar"
TYRUS_SPI_URL="https://repo1.maven.org/maven2/org/glassfish/tyrus/tyrus-spi/1.13.1/tyrus-spi-1.13.1.jar"
GSON_URL="https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.5/gson-2.8.5.jar"

# Remove existing GSON 2.8.9 if it exists to prevent conflicts
echo "Checking for existing GSON jar..."
if [ -f "war/WEB-INF/lib/gson-2.8.9.jar" ]; then
    echo "Removing outdated GSON 2.8.9 jar..."
    rm "war/WEB-INF/lib/gson-2.8.9.jar"
fi

# Download the JARs
echo "Downloading WebSocket API..."
curl -sS -o "war/WEB-INF/lib/javax.websocket-api-1.1.jar" "$WEBSOCKET_API_URL"

echo "Downloading Tyrus Server..."
curl -sS -o "war/WEB-INF/lib/tyrus-server-1.13.1.jar" "$TYRUS_SERVER_URL"

echo "Downloading Tyrus Client..."
curl -sS -o "war/WEB-INF/lib/tyrus-client-1.13.1.jar" "$TYRUS_CLIENT_URL"

echo "Downloading Tyrus Container..."
curl -sS -o "war/WEB-INF/lib/tyrus-container-servlet-1.13.1.jar" "$TYRUS_CONTAINER_URL"

echo "Downloading Tyrus Core..."
curl -sS -o "war/WEB-INF/lib/tyrus-core-1.13.1.jar" "$TYRUS_CORE_URL"

echo "Downloading Tyrus SPI..."
curl -sS -o "war/WEB-INF/lib/tyrus-spi-1.13.1.jar" "$TYRUS_SPI_URL"

echo "Downloading Gson 2.8.5 (compatible version)..."
curl -sS -o "war/WEB-INF/lib/gson-2.8.5.jar" "$GSON_URL"

echo
echo "Dependencies downloaded successfully! Please rebuild your project."
echo "Note: GSON has been downgraded to version 2.8.5 to fix compatibility issues."
echo 