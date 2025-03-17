package models;
import java.util.ArrayList;
import models.Inventory;
import models.Connections;
import models.Item;
import models.Character;


public class Room
{
	private String roomName = "";
	private Inventory inventory;
	private Connections connections;
	private ArrayList<Character> characterContainer;
	
	//Constructor to initialize the room for loadData() in GameEngine which instantiates the room states for the game
	public Room(String roomName, Inventory inventory, Connections connections, ArrayList<Character> characterContainer)
	{
		this.roomName = roomName;
		this.inventory = inventory;
		this.connections = connections;
		this.characterContainer = characterContainer;
	}
	
	// Returns the index of the available room to switch to based on direction input, and returns -1 if it is null
	public int getConnectedRoom(String direction)
	{
		//check direction is getting the output of connections for key[direction] it is null if it 
		int result = this.connections.checkDirection(direction);
		if (result != null)
		{
			return result;
		}
		return -1;
	}
	
	// Adds item to room inventory (player dropping item)
	public void addItem(Item item)
	{
		this.inventory.addItem(item);
	}
	
	// Removes item from room (player picks up item)
	public void removeItem(int itemNum)
	{
		this.inventory.removeItem(itemNum);
	}
	
	// returns the int of the current character characterNum in characterContainer
	public int getCharacterHealth(int characterNum)
	{
		Character currentCharacter = characterContainer.get(characterNum);
		return currentCharacter.getHealth();
	}
	
	// sets the health of the character (player attacks character)
	public int setCharacterHealth(int characterNum, int health)
	{
		Character currentCharacter = characterContainer.get(characterNum);
		return currentCharacter.setHealth(health);
	}
	
	// returns the attack damage of a character using item itemNum in it's inventory (character attacks player)
	public int getCharacterAttackDmg(int characterNum, int itemNum)
	{
		Character currentCharacter = characterContainer.get(characterNum);
		return currentCharacter.getAttackDmg(itemNum);
	}
	
	// returns the actual item object to add into the player's inventory, and in gameEngine it calls removeItem() for the same item as well
	public Item getItem(int itemNum)
	{
		return this.inventory.getItem(itemNum);
	}
	
	// returns room name for the generated response object for the servlet
	public String getRoomName()
	{
		return this.roomName;
	}
	
	// sets room name (most likely not to be used unless a room changes mid game, like cave-collapsed instead of cave)
	public void setRoomName(String roomName)
	{
		this.roomName = roomName;
	}
	
	// returns the character's name for a given index (can be used in another method return characterNames() which returns all of them formatted to be used in the response
	public String getCharacterName(int characterNum)
	{
		Character currentCharacter = characterContainer.get(characterNum);
		return currentCharacter.getName();
	}
}