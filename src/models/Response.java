package models;

// object to resemble a JSON object because java 8 doesn't support it
// attributes can be called in the jsp/html using ${response.attribute} to make putting the dynamic text content in the frontend as simple as possible
public class Response
{
	// Strings with formattings like \n and \t to make output look good for now to show the user what to input
	private String roomInventory;
	private String playerInventory;
	private String roomConnections;
	private String message;
	public Response(String roomInventory, String playerInventory, String roomConnections, String message)
	{
		this.roomInventory = roomInventory;
		this.playerInventory = playerInventory;
		this.roomConnections = roomConnections;
		this.message = message;
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
	
	public String getRoomConnections()
	{
		return this.roomConnections;
	}
	
	public String getMessage()
	{
		return this.message;
	}
}