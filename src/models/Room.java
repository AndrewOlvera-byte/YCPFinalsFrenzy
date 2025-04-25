package models;
import java.util.ArrayList;
import java.util.List;

public class Room
{
    private String roomName = "";
    private Inventory inventory;
    private Connections connections;
    private ArrayList<Character> characterContainer;
    private String requiredKey;
	private String longdescription;
	private String shortdescription;
	private Boolean examined;

    // Updated constructor to include requiredKey
    public Room(String roomName, Inventory inventory, Connections connections, ArrayList<Character> characterContainer, String requiredKey, String longdescription, String shortdescription)
    {
        this.roomName = roomName;
        this.inventory = inventory;
        this.connections = connections;
        this.characterContainer = characterContainer;
        this.requiredKey = requiredKey;
		this.longdescription = longdescription;
		this.shortdescription = shortdescription;
		this.examined = false;
    }
    
    // Overloaded constructor for rooms with no key requirement
    public Room(String roomName, Inventory inventory, Connections connections, ArrayList<Character> characterContainer, String longdescription, String shortdescription)
    {
        this(roomName, inventory, connections, characterContainer, null, longdescription, shortdescription);
    }
    
    // Returns the index of the available room to switch to based on direction input, and returns -1 if it is null
    public int getConnectedRoom(String direction)
    {
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
    
    // returns the int of the current character's health for character at index characterNum
    public int getCharacterHealth(int characterNum)
    {
        Character currentCharacter = characterContainer.get(characterNum);
        return currentCharacter.getHp();
    }
    
    // sets the health of the character (player attacks character)
    public void setCharacterHealth(int characterNum, double newHealth)
    {
        Character currentCharacter = characterContainer.get(characterNum);
        currentCharacter.setHp(newHealth);
    }
    
    // returns the attack damage of a character using item itemNum in its inventory (character attacks player)
    public int getCharacterAttackDmg(int characterNum, int itemNum)
    {
        Character currentCharacter = characterContainer.get(characterNum);
        return currentCharacter.getAttackDmg(itemNum);
    }
    
    public void handleCharacterDeath(int characterNum) {
        Character slainChar = characterContainer.get(characterNum);
        ArrayList<Item> droppedItems = slainChar.dropAllItems();
        for (Item item : droppedItems) {
            this.addItem(item);
        }
        removeCharacter(characterNum);
    }
    
    // returns the actual item object to add into the player's inventory, and in GameEngine it calls removeItem() for the same item as well
    public Item getItem(int itemNum)
    {
        return this.inventory.getItem(itemNum);
    }
    
    // returns room name for the generated response object for the servlet
    public String getRoomName()
    {
        return this.roomName;
    }
    
    // sets room name (most likely not to be used unless a room changes mid game)
    public void setRoomName(String roomName)
    {
        this.roomName = roomName;
    }
    
    // returns the character's name for a given index
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
    
    public boolean isCharAgressive(int characterNum)
    {
        Character currentCharacter = characterContainer.get(characterNum);
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
    
    // New getter for the required key
    public String getRequiredKey() {
        return this.requiredKey;
    }
    
    public void setRequiredKey(String key) {
    	this.requiredKey = key;
    }
    
    // NEW: Getter for a character's maximum health.
    // Assumes the Character class has a getMaxHp() method.
    public int getCharacterMaxHealth(int index) {
        return characterContainer.get(index).getMaxHp();
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
	
	public String talkToNPC(int NPCnum)
	{
		Character character = characterContainer.get(NPCnum);
		NPC npc = (NPC)character;
		return npc.getMessage();
	}
	
	public String[] getNPCResponseOptions(int NPCnum)
	{
		Character character = characterContainer.get(NPCnum);
		NPC npc = (NPC)character;
		return npc.getResponseOptions();
	}
	
	public String interactWithNPC(String choice, int NPCnum)
	{
		Character character = characterContainer.get(NPCnum);
		NPC npc = (NPC)character;
		String message = npc.interact(choice);
		if (npc.isCurrentNodeToAggressive())
		{
			npc.setAgression(true);
			message += "\n" + npc.getName() + " is now aggressive!";
		}
		if (npc.isCurrentNodeDropItem())
		{
			int itemNum = npc.getItemToDrop();
			Item item = npc.getItem(itemNum);
			this.inventory.addItem(item);
			npc.removeItem(itemNum);
			message += "\n" + npc.getName() + " dropped" + item.getName();
		}
		return message;
	}
    /** Allow managers to fetch and mutate the room’s inventory */
    public Inventory getInventory() {
        return this.inventory;
    }

    /** Allow managers to wire up connections from ROOM_CONNECTIONS */
    public Connections getConnections() {
        return this.connections;
    }

    /** Allow managers to add NPCs into the room */
    public List<Character> getCharacterContainer() {
        return this.characterContainer;
    }

	
	public void setDescription(String longdescription, String shortdescription) {
		this.longdescription = longdescription;
		this.shortdescription = shortdescription;
	}
}