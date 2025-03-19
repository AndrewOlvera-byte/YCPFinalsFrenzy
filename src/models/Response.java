package models;

// object to resemble a JSON object because java 8 doesn't support it
// attributes can be called in the jsp/html using ${response.attribute} to make putting the dynamic text content in the frontend as simple as possible
public class Response
{
	// Strings with formattings like \n and \t to make output look good for now to show the user what to input
	private String roomInventory;
	private String playerInventory;
	private String charactersInRoom;
	private String playerInfo;
	private String roomConnections;
	private String message;
	private String error;
	public Response(String roomInventory, String playerInventory, String charactersInRoom, String playerInfo, String roomConnections, String message, String error)
	{
		this.roomInventory = roomInventory;
		this.playerInventory = playerInventory;
		this.charactersInRoom = charactersInRoom;
		this.playerInfo = playerInfo;
		this.roomConnections = roomConnections;
		this.message = message;
		this.error = error;
	}
	
	// getters needed for JSP/html implementation
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
	
	 public String toJson() {
	        return "{\"roomInventory\":\"" + escapeJson(roomInventory) + "\", \"playerInventory\":" + escapeJson(playerInventory) + "\", \"charactersInRoom\":" + escapeJson(charactersInRoom) + "\", \"playerInfo\":" + escapeJson(playerInfo) + "\", \"roomConnections\":" + escapeJson(roomConnections) + "\", \"message\":" + escapeJson(message) + "\", \"error\":" + escapeJson(error) + "}";
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