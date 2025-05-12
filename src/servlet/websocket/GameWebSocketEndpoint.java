package servlet.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import javax.servlet.ServletContext;

import GameEngine.GameEngine;
import models.Response;
import models.Player;
import com.google.gson.Gson;

@ServerEndpoint(value = "/gameSocket")
public class GameWebSocketEndpoint {
    private static Map<Session, Integer> sessionToPlayerId = new ConcurrentHashMap<>();
    private static ServletContext servletContext;
    private static Gson gson = new Gson();
    
    // Track whether each player is typing
    private static Map<Integer, Boolean> playerTypingStatus = new ConcurrentHashMap<>();
    
    public static void setServletContext(ServletContext context) {
        servletContext = context;
    }
    
    @OnOpen
    public void onOpen(Session session) {
        // Session is established but player not authenticated yet
        System.out.println("WebSocket connection opened: " + session.getId());
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            // Parse the incoming message
            Map<String, Object> jsonMessage = gson.fromJson(message, Map.class);
            String type = (String) jsonMessage.get("type");
            
            // Get the game engine
            GameEngine gameEngine = (GameEngine) servletContext.getAttribute("gameEngine");
            if (gameEngine == null) {
                sendErrorMessage(session, "Game engine not available");
                return;
            }
            
            switch (type) {
                case "authenticate":
                    // Handle authentication - link websocket session to player
                    Integer playerId = ((Double) jsonMessage.get("playerId")).intValue();
                    Integer userId = ((Double) jsonMessage.get("userId")).intValue();
                    
                    // Get the player from game engine
                    Player player = gameEngine.getPlayerById(playerId);
                    
                    // Validate player exists and belongs to the user
                    if (player == null) {
                        sendErrorMessage(session, "Invalid player ID");
                        return;
                    }
                    
                    // Verify player belongs to this user
                    if (player.getUserId() != userId) {
                        System.out.println("WARNING: WebSocket authentication attempt - " +
                            "Player " + playerId + " belongs to user " + player.getUserId() + 
                            " but was requested by user " + userId);
                        sendErrorMessage(session, "Player does not belong to this user");
                        return;
                    }
                    
                    // Associate this session with the player ID
                    sessionToPlayerId.put(session, playerId);
                    
                    // Send initial state
                    Response gameState = gameEngine.display(playerId);
                    sendGameState(session, gameState);
                    
                    System.out.println("WebSocket authenticated for player: " + playerId + " user: " + userId);
                    break;
                    
                case "command":
                    // Process game command
                    Integer playerIdCmd = sessionToPlayerId.get(session);
                    if (playerIdCmd == null) {
                        sendErrorMessage(session, "Not authenticated");
                        return;
                    }
                    
                    String command = (String) jsonMessage.get("command");
                    gameEngine.processInput(command, playerIdCmd);
                    
                    // Get updated state for this player
                    Response updatedState = gameEngine.display(playerIdCmd);
                    sendGameState(session, updatedState);
                    
                    // Update typing status
                    playerTypingStatus.put(playerIdCmd, false);
                    
                    // Notify other players in the same room of changes
                    notifyPlayersInSameRoom(gameEngine, playerIdCmd, session);
                    break;
                    
                case "typing":
                    // Update typing status
                    Integer playerIdTyping = sessionToPlayerId.get(session);
                    if (playerIdTyping == null) {
                        return;
                    }
                    
                    boolean isTyping = (boolean) jsonMessage.get("isTyping");
                    playerTypingStatus.put(playerIdTyping, isTyping);
                    
                    // Notify other players in the same room of typing status
                    notifyTypingStatus(gameEngine, playerIdTyping, isTyping, session);
                    break;
                    
                case "ping":
                    // Heartbeat, send pong back
                    Map<String, Object> pongMessage = new ConcurrentHashMap<>();
                    pongMessage.put("type", "pong");
                    session.getBasicRemote().sendText(gson.toJson(pongMessage));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                sendErrorMessage(session, "Error processing message: " + e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void notifyTypingStatus(GameEngine gameEngine, int playerId, boolean isTyping, Session originSession) throws IOException {
        Player player = gameEngine.getPlayerById(playerId);
        if (player != null) {
            int roomNum = player.getCurrentRoomNum();
            
            // Iterate through all connected sessions
            for (Map.Entry<Session, Integer> entry : sessionToPlayerId.entrySet()) {
                Session otherSession = entry.getKey();
                Integer otherPlayerId = entry.getValue();
                
                // Skip the player who is typing
                if (otherSession.equals(originSession)) {
                    continue;
                }
                
                // Check if other player is in the same room
                Player otherPlayer = gameEngine.getPlayerById(otherPlayerId);
                if (otherPlayer != null && otherPlayer.getCurrentRoomNum() == roomNum) {
                    // Send typing status
                    Map<String, Object> message = new ConcurrentHashMap<>();
                    message.put("type", "playerTyping");
                    message.put("playerId", playerId);
                    message.put("playerName", player.getName());
                    message.put("isTyping", isTyping);
                    otherSession.getBasicRemote().sendText(gson.toJson(message));
                }
            }
        }
    }
    
    private void notifyPlayersInSameRoom(GameEngine gameEngine, int playerId, Session originSession) throws IOException {
        Player player = gameEngine.getPlayerById(playerId);
        if (player != null) {
            int roomNum = player.getCurrentRoomNum();
            
            // Iterate through all connected sessions
            for (Map.Entry<Session, Integer> entry : sessionToPlayerId.entrySet()) {
                Session otherSession = entry.getKey();
                Integer otherPlayerId = entry.getValue();
                
                // Skip the player who initiated the action
                if (otherSession.equals(originSession)) {
                    continue;
                }
                
                // Check if other player is in the same room
                Player otherPlayer = gameEngine.getPlayerById(otherPlayerId);
                if (otherPlayer != null && otherPlayer.getCurrentRoomNum() == roomNum) {
                    // Send updated game state to this player
                    Response otherPlayerState = gameEngine.display(otherPlayerId);
                    sendGameState(otherSession, otherPlayerState);
                }
            }
        }
    }
    
    private void sendGameState(Session session, Response gameState) throws IOException {
        Map<String, Object> message = new ConcurrentHashMap<>();
        message.put("type", "gameState");
        message.put("data", gameState);
        session.getBasicRemote().sendText(gson.toJson(message));
    }
    
    private void sendErrorMessage(Session session, String errorMessage) throws IOException {
        Map<String, Object> message = new ConcurrentHashMap<>();
        message.put("type", "error");
        message.put("message", errorMessage);
        session.getBasicRemote().sendText(gson.toJson(message));
    }
    
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("WebSocket connection closed: " + closeReason.getReasonPhrase());
        Integer playerId = sessionToPlayerId.get(session);
        if (playerId != null) {
            playerTypingStatus.remove(playerId);
        }
        sessionToPlayerId.remove(session);
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error: " + throwable.getMessage());
        throwable.printStackTrace();
        Integer playerId = sessionToPlayerId.get(session);
        if (playerId != null) {
            playerTypingStatus.remove(playerId);
        }
        sessionToPlayerId.remove(session);
    }
} 