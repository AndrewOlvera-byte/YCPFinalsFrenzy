package models;

// Object to resemble a JSON object because Java 8 doesn't support it.
// Attributes can be called in the JSP/HTML using ${response.attribute} to make putting
// the dynamic text content in the frontend as simple as possible.
public class Response {
    // ─── Fields ────────────────────────────────────────────────────────────────
    private String  roomInventory;
    private String  playerInventory;
    private String  charactersInRoom;
    private String  playerInfo;
    private String  roomConnections;
    private String  message;
    private String  error;
    private String  roomImage;
    private String  roomNumber;
    private String  roomItemsOverlay;
    private String  roomCharactersOverlay;

    // ▶ New “game over” fields
    private boolean gameOver = false;
    private String  gameOverImage = "";

    // ─── Constructors ─────────────────────────────────────────────────────────
    /** No-arg constructor so you can do `new Response()` and then setters */
    public Response() { }

    /** Optional multi-arg constructor for full initialization */
    public Response(String roomInventory,
                    String playerInventory,
                    String charactersInRoom,
                    String playerInfo,
                    String roomConnections,
                    String message,
                    String error,
                    String roomImage,
                    String roomNumber,
                    String roomItemsOverlay,
                    String roomCharactersOverlay)
    {
        this.roomInventory         = roomInventory;
        this.playerInventory       = playerInventory;
        this.charactersInRoom      = charactersInRoom;
        this.playerInfo            = playerInfo;
        this.roomConnections       = roomConnections;
        this.message               = message;
        this.error                 = error;
        this.roomImage             = roomImage;
        this.roomNumber            = roomNumber;
        this.roomItemsOverlay      = roomItemsOverlay;
        this.roomCharactersOverlay = roomCharactersOverlay;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────
    public String  getRoomInventory()         { return roomInventory; }
    public String  getPlayerInventory()       { return playerInventory; }
    public String  getCharactersInRoom()      { return charactersInRoom; }
    public String  getPlayerInfo()            { return playerInfo; }
    public String  getRoomConnections()       { return roomConnections; }
    public String  getMessage()               { return message; }
    public String  getError()                 { return error; }
    public String  getRoomImage()             { return roomImage; }
    public String  getRoomNumber()            { return roomNumber; }
    public String  getRoomItemsOverlay()      { return roomItemsOverlay; }
    public String  getRoomCharactersOverlay() { return roomCharactersOverlay; }
    public boolean isGameOver()               { return gameOver; }
    public String  getGameOverImage()         { return gameOverImage; }

    // ─── Setters ──────────────────────────────────────────────────────────────
    public void setRoomInventory(String s)         { this.roomInventory = s; }
    public void setPlayerInventory(String s)       { this.playerInventory = s; }
    public void setCharactersInRoom(String s)      { this.charactersInRoom = s; }
    public void setPlayerInfo(String s)            { this.playerInfo = s; }
    public void setRoomConnections(String s)       { this.roomConnections = s; }
    public void setMessage(String s)               { this.message = s; }
    public void setError(String s)                 { this.error = s; }
    public void setRoomImage(String s)             { this.roomImage = s; }
    public void setRoomNumber(String s)            { this.roomNumber = s; }
    public void setRoomItemsOverlay(String s)      { this.roomItemsOverlay = s; }
    public void setRoomCharactersOverlay(String s) { this.roomCharactersOverlay = s; }
    public void setGameOver(boolean b)             { this.gameOver = b; }
    public void setGameOverImage(String s)         { this.gameOverImage = s; }

    // ─── Convert to a JSON‐style string ───────────────────────────────────────
    public String toJson() {
        return "{"
            + "\"roomInventory\":\""        + escapeJson(roomInventory)           + "\","
            + "\"playerInventory\":\""      + escapeJson(playerInventory)         + "\","
            + "\"charactersInRoom\":\""     + escapeJson(charactersInRoom)        + "\","
            + "\"playerInfo\":\""           + escapeJson(playerInfo)              + "\","
            + "\"roomConnections\":\""      + escapeJson(roomConnections)         + "\","
            + "\"message\":\""              + escapeJson(message)                 + "\","
            + "\"error\":\""                + escapeJson(error)                   + "\","
            + "\"roomImage\":\""            + escapeJson(roomImage)               + "\","
            + "\"roomNumber\":\""           + escapeJson(roomNumber)              + "\","
            + "\"roomItemsOverlay\":\""     + escapeJson(roomItemsOverlay)        + "\","
            + "\"roomCharactersOverlay\":\""+ escapeJson(roomCharactersOverlay)   + "\","
            + "\"gameOver\":"               + gameOver                             + ","
            + "\"gameOverImage\":\""        + escapeJson(gameOverImage)           + "\""
            + "}";
    }

    /** Helper to escape double quotes in JSON strings */
    private String escapeJson(String s) {
        return (s == null) ? "" : s.replace("\"", "\\\"");
    }
}
