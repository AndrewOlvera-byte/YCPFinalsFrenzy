package models;

public class Response {
    // ─── Fields ────────────────────────────────────────────────────────────────
    private String  roomInventory;
    private String  playerInventory;
    private String playerCompanion;
    private String companionInventory;
    private String  charactersInRoom;
    private String companionsInRoom;
    private String  playerInfo;
    private String  roomConnections;
    private String  message;
    private String  error;
    private String  roomImage;
    private String  roomNumber;
    private String  roomItemsOverlay;
    private String  roomCharactersOverlay;
    private String roomCompanionsOverlay;
    private String questOverlay = "";
    private String playerInventoryOverlay = "";

    // New fields for player inventory row
    private String[] playerInventoryItems;
    private int playerInventorySize;

    // ▶ New "game over" fields
    private boolean gameOver = false;
    private String  gameOverImage = "";

    // ─── Constructors ─────────────────────────────────────────────────────────
    /** No-arg constructor so you can do `new Response()` and then setters */
    public Response() { }

    /** Optional multi-arg constructor for full initialization */
    public Response(String roomInventory,
                    String playerInventory,
                    String playerCompanion,
                    String companionInventory,
                    String charactersInRoom,
                    String companionsInRoom,
                    String playerInfo,
                    String roomConnections,
                    String message,
                    String error,
                    String roomImage,
                    String roomNumber,
                    String roomItemsOverlay,
                    String roomCharactersOverlay,
                    String roomCompanionsOverlay,
                    String questOverlay,
                    String playerInventoryOverlay)
    {
        this.roomInventory         = roomInventory;
        this.playerInventory       = playerInventory;
        this.playerCompanion       = playerCompanion;
        this.companionInventory    = companionInventory;
        this.charactersInRoom      = charactersInRoom;
        this.companionsInRoom      = companionsInRoom;
        this.playerInfo            = playerInfo;
        this.roomConnections       = roomConnections;
        this.message               = message;
        this.error                 = error;
        this.roomImage             = roomImage;
        this.roomNumber            = roomNumber;
        this.roomItemsOverlay      = roomItemsOverlay;
        this.roomCharactersOverlay = roomCharactersOverlay;
        this.roomCompanionsOverlay = roomCompanionsOverlay;
        this.questOverlay          = questOverlay;
        this.playerInventoryOverlay = playerInventoryOverlay;

        // Parse player inventory items
        parsePlayerInventoryItems();
    }

    // New method to parse player inventory items
    private void parsePlayerInventoryItems() {
        // Remove "Player Inventory:" line and split by newline, then trim
        String[] lines = playerInventory.split("\n");
        
        // Create a list to store actual item names
        java.util.List<String> items = new java.util.ArrayList<>();
        
        for (String line : lines) {
            if (line.trim().isEmpty() || line.startsWith("Player Inventory:")) continue;
            
            // Split the line and take the second part (item name)
            String[] parts = line.trim().split("\\t");
            if (parts.length > 1) {
                items.add(parts[1].trim());
            }
        }
        
        // Convert to array
        playerInventoryItems = items.toArray(new String[0]);
        playerInventorySize = playerInventoryItems.length;
    }

    // New getters for inventory row
    public String[] getPlayerInventoryItems() {
        return playerInventoryItems;
    }

    public int getPlayerInventorySize() {
        return playerInventorySize;
    }

    // ─── Existing Getters ──────────────────────────────────────────────────────────────
    public String  getRoomInventory()         { return roomInventory; }
    public String  getPlayerInventory()       { return playerInventory; }
    public String getPlayerCompanion() 		  { return playerCompanion; }
    public String getCompanionInventory() 	  { return companionInventory; }
    public String  getCharactersInRoom()      { return charactersInRoom; }
    public String getCompanionsInRoom()       { return companionsInRoom; }
    public String  getPlayerInfo()            { return playerInfo; }
    public String  getRoomConnections()       { return roomConnections; }
    public String  getMessage()               { return message; }
    public String  getError()                 { return error; }
    public String  getRoomImage()             { return roomImage; }
    public String  getRoomNumber()            { return roomNumber; }
    public String  getRoomItemsOverlay()      { return roomItemsOverlay; }
    public String  getRoomCharactersOverlay() { return roomCharactersOverlay; }
    public String  getRoomCompanionsOverlay() { return roomCompanionsOverlay; }
    public String  getQuestOverlay()          { return questOverlay; }
    public boolean isGameOver()               { return gameOver; }
    public String  getGameOverImage()         { return gameOverImage; }
    public String getPlayerInventoryOverlay() { return playerInventoryOverlay; }

    // ─── Setters ──────────────────────────────────────────────────────────────
    public void setRoomInventory(String s)         { this.roomInventory = s; }
    public void setPlayerInventory(String s)       { 
        this.playerInventory = s; 
        parsePlayerInventoryItems(); // Reparse when inventory is set
    }
    public void setPlayerCompanion(String s) 	   { this.playerCompanion = s; }
    public void setCompanionInventory(String s)    { this.companionInventory = s; }
    public void setCharactersInRoom(String s)      { this.charactersInRoom = s; }
    public void setCompanionsInRoom(String s)      { this.companionsInRoom = s; }
    public void setPlayerInfo(String s)            { this.playerInfo = s; }
    public void setRoomConnections(String s)       { this.roomConnections = s; }
    public void setMessage(String s)               { this.message = s; }
    public void setError(String s)                 { this.error = s; }
    public void setRoomImage(String s)             { this.roomImage = s; }
    public void setRoomNumber(String s)            { this.roomNumber = s; }
    public void setRoomItemsOverlay(String s)      { this.roomItemsOverlay = s; }
    public void setRoomCharactersOverlay(String s) { this.roomCharactersOverlay = s; }
    public void setRoomCompanionsOverlay(String s) { this.roomCompanionsOverlay = s; }
    public void setQuestOverlay(String s)          { this.questOverlay = s; }
    public void setGameOver(boolean b)             { this.gameOver = b; }
    public void setGameOverImage(String s)         { this.gameOverImage = s; }
    public void setPlayerInventoryOverlay(String s) { this.playerInventoryOverlay = s; }

    // ─── Convert to a JSON‐style string ───────────────────────────────────────
    public String toJson() {
        // Convert player inventory items to JSON array
        StringBuilder itemsJson = new StringBuilder("[");
        if (playerInventoryItems != null) {
            for (int i = 0; i < playerInventoryItems.length; i++) {
                itemsJson.append("\"").append(escapeJson(playerInventoryItems[i])).append("\"");
                if (i < playerInventoryItems.length - 1) {
                    itemsJson.append(",");
                }
            }
        }
        itemsJson.append("]");

        return "{"
            + "\"roomInventory\":\""        + escapeJson(roomInventory)           + "\","
            + "\"playerInventory\":\""      + escapeJson(playerInventory)         + "\","
            + "\"playerCompanion\":\"" 		+ escapeJson(playerCompanion) 		  + "\","
            + "\"companionInventory\":\""   + escapeJson(companionInventory)	  + "\","
            + "\"charactersInRoom\":\""     + escapeJson(charactersInRoom)        + "\","
            + "\"companionsInRoom\":\""    + escapeJson(companionsInRoom)        + "\","
            + "\"playerInfo\":\""           + escapeJson(playerInfo)              + "\","
            + "\"roomConnections\":\""      + escapeJson(roomConnections)         + "\","
            + "\"message\":\""              + escapeJson(message)                 + "\","
            + "\"error\":\""                + escapeJson(error)                   + "\","
            + "\"roomImage\":\""            + escapeJson(roomImage)               + "\","
            + "\"roomNumber\":\""           + escapeJson(roomNumber)              + "\","
            + "\"roomItemsOverlay\":\""     + escapeJson(roomItemsOverlay)        + "\","
            + "\"roomCharactersOverlay\":\""+ escapeJson(roomCharactersOverlay)   + "\","
            + "\"roomCompanionsOverlay\":\""+ escapeJson(roomCompanionsOverlay)   + "\","
            + "\"questOverlay\":\""         + escapeJson(questOverlay)            + "\","
            + "\"playerInventoryOverlay\":\""+ escapeJson(playerInventoryOverlay)  + "\","
            + "\"gameOver\":"               + gameOver                            + ","
            + "\"gameOverImage\":\""        + escapeJson(gameOverImage)           + "\","
            + "\"playerInventoryItems\":"   + itemsJson.toString()                + ","
            + "\"playerInventorySize\":"    + playerInventorySize
            + "}";
    }

    /** Helper to escape double quotes in JSON strings */
    private String escapeJson(String s) {
        return (s == null) ? "" : s.replace("\"", "\\\"");
    }
}