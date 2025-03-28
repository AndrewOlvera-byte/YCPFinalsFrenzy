package models;

// Object to resemble a JSON object because Java 8 doesn't support it.
// Attributes can be called in the JSP/HTML using ${response.attribute} to make putting the dynamic text content in the frontend as simple as possible.
public class Response
{
    // Strings with formatting (like \n and \t) to make output look good for now.
    private String roomInventory;
    private String playerInventory;
    private String charactersInRoom;
    private String playerInfo;
    private String roomConnections;
    private String message;
    private String error;
    private String roomImage; 
    private String roomNumber;  // New field for dynamic room number overlay

    // Updated constructor to include roomNumber
    public Response(String roomInventory, String playerInventory, String charactersInRoom, String playerInfo, String roomConnections, String message, String error, String roomImage, String roomNumber)
    {
        this.roomInventory = roomInventory;
        this.playerInventory = playerInventory;
        this.charactersInRoom = charactersInRoom;
        this.playerInfo = playerInfo;
        this.roomConnections = roomConnections;
        this.message = message;
        this.error = error;
        this.roomImage = roomImage;
        this.roomNumber = roomNumber;
    }
    
    // Getters for JSP/HTML
    public String getRoomInventory()
    {
        return this.roomInventory;
    }
    
    public String getPlayerInventory()
    {
        return this.playerInventory;
    }
    
    public String getCharactersInRoom()
    {
        return this.charactersInRoom;
    }
    
    public String getPlayerInfo()
    {
        return this.playerInfo;
    }
    
    public String getRoomConnections()
    {
        return this.roomConnections;
    }
    
    public String getMessage()
    {
        return this.message;
    }
    
    public String getError()
    {
        return this.error;
    }
    
    public String getRoomImage() {
        return this.roomImage;
    }

    // New getter for room number
    public String getRoomNumber() {
        return this.roomNumber;
    }
    
    public String toJson() {
        return "{\"roomInventory\":\"" + escapeJson(roomInventory) +
                "\", \"playerInventory\":\"" + escapeJson(playerInventory) +
                "\", \"charactersInRoom\":\"" + escapeJson(charactersInRoom) +
                "\", \"playerInfo\":\"" + escapeJson(playerInfo) +
                "\", \"roomConnections\":\"" + escapeJson(roomConnections) +
                "\", \"message\":\"" + escapeJson(message) +
                "\", \"error\":\"" + escapeJson(error) +
                "\", \"roomImage\":\"" + escapeJson(roomImage) +
                "\", \"roomNumber\":\"" + escapeJson(roomNumber) + "\"}";
    }
    
    // Simple helper method to escape double quotes for JSON safety.
    private String escapeJson(String s) 
    {
        if (s == null) {
            return "";
        }
        return s.replace("\"", "\\\"");
    }
}
