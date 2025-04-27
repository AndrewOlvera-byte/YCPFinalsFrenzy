package models;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import models.DerbyDatabase;

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
        
        // Update the database to reflect items dropped from a character death
        try (Connection conn = DerbyDatabase.getConnection()) {
            // Find character ID (different approach for NPC vs player)
            int characterId = -1;
            String tableName = null;
            String idColumnName = null;
            
            if (slainChar instanceof NPC) {
                tableName = "NPC_INVENTORY";
                idColumnName = "npc_id";
                
                // Get NPC ID
                try (PreparedStatement psChar = conn.prepareStatement(
                    "SELECT npc_id FROM NPC WHERE name = ?")) {
                    psChar.setString(1, slainChar.getName());
                    ResultSet rsChar = psChar.executeQuery();
                    if (rsChar.next()) {
                        characterId = rsChar.getInt("npc_id");
                    }
                }
            } else {
                // For other character types like Player
                tableName = "PLAYER_INVENTORY";
                idColumnName = "player_id";
                characterId = 1; // Assuming player_id is always 1
            }
            
            // Get this room's ID
            int roomId = -1;
            try (PreparedStatement psRoom = conn.prepareStatement(
                "SELECT room_id FROM ROOM WHERE room_name = ?")) {
                psRoom.setString(1, this.roomName);
                ResultSet rsRoom = psRoom.executeQuery();
                if (rsRoom.next()) {
                    roomId = rsRoom.getInt("room_id");
                }
            }
            
            if (characterId != -1 && roomId != -1 && tableName != null) {
                // For each dropped item
                for (Item item : droppedItems) {
                    int itemId = -1;
                    
                    // Get item ID
                    try (PreparedStatement psItem = conn.prepareStatement(
                        "SELECT item_id FROM ITEM WHERE name = ?")) {
                        psItem.setString(1, item.getName());
                        ResultSet rsItem = psItem.executeQuery();
                        if (rsItem.next()) {
                            itemId = rsItem.getInt("item_id");
                        }
                    }
                    
                    if (itemId != -1) {
                        // 1. Delete from character's inventory
                        try (PreparedStatement psDel = conn.prepareStatement(
                            "DELETE FROM " + tableName + " WHERE " + idColumnName + " = ? AND item_id = ?")) {
                            psDel.setInt(1, characterId);
                            psDel.setInt(2, itemId);
                            psDel.executeUpdate();
                        }
                        
                        // 2. Check if item already exists in room inventory
                        boolean itemInRoom = false;
                        try (PreparedStatement psCheck = conn.prepareStatement(
                            "SELECT 1 FROM ROOM_INVENTORY WHERE room_id = ? AND item_id = ?")) {
                            psCheck.setInt(1, roomId);
                            psCheck.setInt(2, itemId);
                            itemInRoom = psCheck.executeQuery().next();
                        }
                        
                        // 3. Insert into ROOM_INVENTORY if not already there
                        if (!itemInRoom) {
                            try (PreparedStatement psIns = conn.prepareStatement(
                                "INSERT INTO ROOM_INVENTORY (room_id, item_id) VALUES (?, ?)")) {
                                psIns.setInt(1, roomId);
                                psIns.setInt(2, itemId);
                                psIns.executeUpdate();
                            }
                        }
                    }
                }
                
                // If it's an NPC, also remove it from NPC_ROOM
                if (slainChar instanceof NPC) {
                    try (PreparedStatement psDelNpc = conn.prepareStatement(
                        "DELETE FROM NPC_ROOM WHERE npc_id = ? AND room_id = ?")) {
                        psDelNpc.setInt(1, characterId);
                        psDelNpc.setInt(2, roomId);
                        psDelNpc.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to update database for character death: " + e.getMessage());
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
			message += "\n<b>" + npc.getName() + " dropped " + item.getName() + "</b>";
			
			// Update the database to reflect the item moving from NPC to room
			try (Connection conn = DerbyDatabase.getConnection()) {
				// First, find the NPC's ID and the item's ID in the database
				int npcId = -1;
				int itemId = -1;
				int roomId = -1;
				
				// Get NPC ID
				try (PreparedStatement psNpc = conn.prepareStatement(
					"SELECT npc_id FROM NPC WHERE name = ?")) {
					psNpc.setString(1, npc.getName());
					ResultSet rsNpc = psNpc.executeQuery();
					if (rsNpc.next()) {
						npcId = rsNpc.getInt("npc_id");
					}
				}
				
				// Get item ID
				try (PreparedStatement psItem = conn.prepareStatement(
					"SELECT item_id FROM ITEM WHERE name = ?")) {
					psItem.setString(1, item.getName());
					ResultSet rsItem = psItem.executeQuery();
					if (rsItem.next()) {
						itemId = rsItem.getInt("item_id");
					}
				}
				
				// Get this room's ID
				try (PreparedStatement psRoom = conn.prepareStatement(
					"SELECT room_id FROM ROOM WHERE room_name = ?")) {
					psRoom.setString(1, this.roomName);
					ResultSet rsRoom = psRoom.executeQuery();
					if (rsRoom.next()) {
						roomId = rsRoom.getInt("room_id");
					}
				}
				
				// If we found all IDs, update the database
				if (npcId != -1 && itemId != -1 && roomId != -1) {
					// 1. Delete from NPC_INVENTORY
					try (PreparedStatement psDel = conn.prepareStatement(
						"DELETE FROM NPC_INVENTORY WHERE npc_id = ? AND item_id = ?")) {
						psDel.setInt(1, npcId);
						psDel.setInt(2, itemId);
						psDel.executeUpdate();
					}
					
					// 2. Check if item already exists in room inventory
					boolean itemInRoom = false;
					try (PreparedStatement psCheck = conn.prepareStatement(
						"SELECT 1 FROM ROOM_INVENTORY WHERE room_id = ? AND item_id = ?")) {
						psCheck.setInt(1, roomId);
						psCheck.setInt(2, itemId);
						itemInRoom = psCheck.executeQuery().next();
					}
					
					// 3. Insert into ROOM_INVENTORY if not already there
					if (!itemInRoom) {
						try (PreparedStatement psIns = conn.prepareStatement(
							"INSERT INTO ROOM_INVENTORY (room_id, item_id) VALUES (?, ?)")) {
							psIns.setInt(1, roomId);
							psIns.setInt(2, itemId);
							psIns.executeUpdate();
						}
					}
				}
			} catch (SQLException e) {
				System.err.println("Failed to update database for NPC item drop: " + e.getMessage());
			}
		}
		return message;
	}
    /** Allow managers to fetch and mutate the room's inventory */
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