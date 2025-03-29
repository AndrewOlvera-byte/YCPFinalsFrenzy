package models;
import java.util.ArrayList;



public class Room
{
	private String roomName = "";
	private Inventory inventory;
	private Connections connections;
	private ArrayList<models.Character> characterContainer;
	private String longdescription;
	private String shortdescription;
	private Boolean examined;
	
	//Constructor to initialize the room for loadData() in GameEngine which instantiates the room states for the game
	public Room(String roomName, Inventory inventory, Connections connections, ArrayList<Character> characterContainer, String longdescription, String shortdescription)
	{
		this.roomName = roomName;
		this.inventory = inventory;
		this.connections = connections;
		this.characterContainer = characterContainer;
		this.longdescription = longdescription;
		this.shortdescription = shortdescription;
		this.examined = false;
	}
	
	// Returns the index of the available room to switch to based on direction input, and returns -1 if it is null
	public int getConnectedRoom(String direction)
	{
		//check direction is getting the output of connections for key[direction] it is null if it 
		Integer result = this.connections.getConnection(direction);
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
		return currentCharacter.getHp();
	}
	
	// sets the health of the character (player attacks character)
	public void setCharacterHealth(int characterNum, int health)
	{
		Character currentCharacter = characterContainer.get(characterNum);
		currentCharacter.setHp(health);
	}
	
	// returns the attack damage of a character using item itemNum in it's inventory (character attacks player)
	public int getCharacterAttackDmg(int characterNum, int itemNum)
	{
		Character currentCharacter = characterContainer.get(characterNum);
		return currentCharacter.getAttackDmg(itemNum);
	}
	
	public void handleCharacterDeath(int characterNum) {
	    Character slainChar = characterContainer.get(characterNum);
	    
	 // Get the dropped items from character
	    ArrayList<Item> droppedItems = slainChar.dropAllItems();

	    // Add dropped items to the room's inventory
	    for (Item item : droppedItems) {
	        this.addItem(item);
	    }


	    // Remove character from room
	    removeCharacter(characterNum);
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
	
	public Character getCharacter(int characterNum) {
		return characterContainer.get(characterNum);
	}
	
	public void removeCharacter(int characterNum)
	{
		this.characterContainer.remove(characterNum);
	}
	
	public int getCharacterTotal()
	{
		return this.characterContainer.size();
	}
	
	public boolean isCharAgressive(int characterNum) {
	    Character currentCharacter = characterContainer.get(characterNum);
	    
	    // Check if the character is an instance of NPC
	    if (currentCharacter instanceof NPC) {
	        NPC npc = (NPC) currentCharacter;  // Cast to NPC
	        return npc.getAggresion();  // Access aggression property
	    }
	    
	    // If not an NPC, return false or handle the logic for non-NPC characters (like Player)
	    return false;  // Assuming non-NPC characters are not aggressive
	}

	
	public boolean getCharacterJustAttacked(int characterNum)
	{
		Character currentCharacter = characterContainer.get(characterNum);
		boolean justAttacked = currentCharacter.getJustAttacked();
		return justAttacked;
	}
	
	public void setCharacterJustAttacked(int characterNum, boolean value)
	{
		Character currentCharacter = characterContainer.get(characterNum);
		currentCharacter.setJustAttacked(value);
	}
	
	public int getInventorySize()
	{
		int size = inventory.getSize();
		return size;
	}
	
	public String getItemName(int itemNum)
	{
		return inventory.getItemName(itemNum);
	}
	
	public int getCharacterContainerSize()
	{
		return characterContainer.size();
	}
	
	
	public String getRoomDescription() {
		if(!examined) {
			examined = true;
			return longdescription;
		}
		else {
			return shortdescription;
		}
	}
	
	public void setDescription(String longdescription, String shortdescription) {
		this.longdescription = longdescription;
		this.shortdescription = shortdescription;
	}
	
}