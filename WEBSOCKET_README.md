# WebSocket Implementation for YCP Finals Frenzy

This document explains the WebSocket implementation that was added to enable real-time multiplayer communication in the YCP Finals Frenzy game.

## Overview

The implementation adds WebSocket support to enable real-time updates between players in the same room. When a player performs an action (like picking up an item or defeating an NPC), all other players in the same room receive immediate updates to their game state without needing to issue a command.

## Features

- **Real-time Game State Updates**: Players can see changes made by other players immediately without refreshing or issuing commands
- **Typing Indicators**: Shows when other players in the same room are typing commands
- **Graceful Fallback**: Falls back to AJAX polling if WebSockets are not supported or connection fails
- **Clear Visual Indication**: UI shows whether WebSocket or AJAX fallback is being used
- **Session Management**: Handles player authentication through WebSockets

## Installation

To install the required dependencies:

### Windows
1. Run `download-dependencies.bat` to download all required JAR files
2. Restart your application server

### Linux/macOS
1. Make the script executable: `chmod +x download-dependencies.sh`
2. Run `./download-dependencies.sh` to download all required JAR files
3. Restart your application server

## Dependencies Added

The implementation adds the following libraries:
- `javax.websocket-api-1.1.jar`: Java WebSocket API
- `tyrus-server-1.13.1.jar`: Tyrus WebSocket server implementation
- `tyrus-client-1.13.1.jar`: Tyrus WebSocket client implementation
- `tyrus-container-servlet-1.13.1.jar`: Servlet container support for Tyrus
- `tyrus-core-1.13.1.jar`: Core Tyrus implementation
- `tyrus-spi-1.13.1.jar`: Tyrus Service Provider Interface
- `gson-2.8.9.jar`: Google JSON library

## Components Added

1. **GameWebSocketEndpoint.java**: The WebSocket server endpoint that handles connections, messages, and broadcasts
2. **WebSocketContextListener.java**: Initializes the WebSocket context with the servlet context
3. **SessionDataServlet.java**: Provides session data to the WebSocket client for authentication
4. **DashboardAjax.html updates**: Added WebSocket client implementation with fallback

## Notes on AJAX Fallback

The implementation includes a clear visual indication when falling back from WebSockets to AJAX polling:

1. A colored status message will appear in the top-right corner:
   - Green: "Using WebSocket (real-time updates)" - WebSockets working
   - Orange: "Using AJAX polling (periodic updates)" - Fallback mode

2. Console messages will show:
   - When WebSocket connection is established or closed
   - When falling back to AJAX polling
   - Any connection errors

3. Additionally, a log message "AJAX polling fallback: getState called for player X" will be printed to the server logs when AJAX polling is active.

## Compatibility

This implementation is compatible with Java 1.8 and uses only standard APIs that should work with most Java EE application servers. The WebSocket specification is part of Java EE 7. 