package models;
import java.util.ArrayList;

public class Room
{
    private String roomName = "";
    private Inventory inventory;
    private Connections connections;
    private ArrayList<models.Character> characterContainer;
    
    // New field for a required key (null or empty means no key is required)
    private String requiredKey;

    // Updated constructor to include requiredKey
    public Room(String roomName, Inventory inventory, Connections connections, ArrayList<Character> characterContainer, String requiredKey)
    {
        this.roomName = roomName;
        this.inventory = inventory;
        this.connections = connections;
        this.characterContainer = characterContainer;
        this.requiredKey = requiredKey;
    }
    
    // Overloaded constructor for rooms with no key requirement
    public Room(String roomName, Inventory inventory, Connections connections, ArrayList<Character> characterContainer)
    {
        this(roomName, inventory, connections, characterContainer, null);
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
    public void setCharacterHealth(int characterNum, int health)
    {
        Character currentCharacter = characterContainer.get(characterNum);
        currentCharacter.setHp(health);
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
        return currentCharacter.isAgressive();
    }
    
    public boolean getCharacterJustAttacked(int characterNum)
    {
        Character currentCharacter = characterContainer.get(characterNum);
        return currentCharacter.getJustAttacked();
    }
    
    public void setCharacterJustAttacked(int characterNum, boolean value)
    {
        Character currentCharacter = characterContainer.get(characterNum);
        currentCharacter.setJustAttacked(value);
    }
    
    public int getInventorySize()
    {
        return inventory.getSize();
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
    
    // NEW: Getter for a character's maximum health.
    // Assumes the Character class has a getMaxHp() method.
    public int getCharacterMaxHealth(int index) {
        return characterContainer.get(index).getMaxHp();
    }
}