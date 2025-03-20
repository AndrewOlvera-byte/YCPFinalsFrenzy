package modelsTest;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import models.Room;
import models.Inventory;
import models.Item;
import models.Weapon;
import models.Character;
import models.Connections;

public class RoomTest {
    private Room room;
    private Inventory inventory;
    private Connections connections;
    private ArrayList<Character> characters;
    private Item testItem;
    private Character testCharacter;
    
    @Before
    public void setUp() {
        // Prepare an empty inventory and a connections instance.
        inventory = new Inventory(new ArrayList<Item>(), 100);
        connections = new Connections();
        // Set a connection for "North" to room id 1.
        connections.setConnection("North", 1);
        // Initialize the character container.
        characters = new ArrayList<>();
        // Create a room with name "Test Room".
        room = new Room("Test Room", inventory, connections, characters);
        
        // Create a test item (Weapon) for testing addItem/getItem methods.
        String[] components = {"Iron"};
        testItem = new Weapon(50, 5, "Test Sword", components, 25);
        
        // Create a test character with a weapon in its inventory.
        ArrayList<Item> charItems = new ArrayList<>();
        charItems.add(new Weapon(30, 3, "Test Dagger", components, 15));
        Inventory charInventory = new Inventory(charItems, 50);
        testCharacter = new Character("Goblin", 100, charInventory);
    }
    
    @Test
    public void testGetConnectedRoom() {
        // For "North", we should get the connection we set.
        assertEquals(1, room.getConnectedRoom("North"));
        // For an unset direction, it should return -1.
        assertEquals(-1, room.getConnectedRoom("East"));
    }
    
    @Test
    public void testAddAndRemoveItem() {
        int initialSize = inventory.getSize();
        room.addItem(testItem);
        assertEquals(initialSize + 1, inventory.getSize());
        // Verify the item can be retrieved.
        assertEquals("Test Sword", room.getItem(0).getName());
        
        // Remove the item and check the size.
        room.removeItem(0);
        assertEquals(initialSize, inventory.getSize());
    }
    
    @Test
    public void testGetItemName() {
        room.addItem(testItem);
        assertEquals("Test Sword", room.getItemName(0));
    }
    
    @Test
    public void testCharacterHealthMethods() {
        // Add a character to the room.
        characters.add(testCharacter);
        assertEquals(100, room.getCharacterHealth(0));
        
        // Change health and verify.
        room.setCharacterHealth(0, 80);
        assertEquals(80, room.getCharacterHealth(0));
    }
    
    @Test
    public void testGetCharacterAttackDmg() {
        // Add the test character (with a weapon) to the room.
        characters.add(testCharacter);
        // The character's inventory weapon "Test Dagger" should return its attack damage.
        assertEquals(15, room.getCharacterAttackDmg(0, 0));
    }
    
    @Test
    public void testHandleCharacterDeath() {
        // Add the test character to the room.
        characters.add(testCharacter);
        int initialRoomInventorySize = inventory.getSize();
        
        // The character has one item in its inventory.
        assertEquals(1, testCharacter.getInventorySize());
        
        // Kill the character.
        room.handleCharacterDeath(0);
        // The character container should now be empty.
        assertEquals(0, room.getCharacterContainerSize());
        // The dropped item should have been added to the room's inventory.
        assertEquals(initialRoomInventorySize + 1, inventory.getSize());
        assertEquals("Test Dagger", inventory.getItem(inventory.getSize() - 1).getName());
    }
    
    @Test
    public void testRoomNameMethods() {
        // Check getter and setter for room name.
        assertEquals("Test Room", room.getRoomName());
        room.setRoomName("New Room");
        assertEquals("New Room", room.getRoomName());
    }
    
    @Test
    public void testCharacterNameAndRemoval() {
        characters.add(testCharacter);
        assertEquals("Goblin", room.getCharacterName(0));
        assertEquals(1, room.getCharacterTotal());
        
        room.removeCharacter(0);
        assertEquals(0, room.getCharacterTotal());
    }
    
    @Test
    public void testJustAttackedMethods() {
        characters.add(testCharacter);
        // Initially, justAttacked should be false.
        assertFalse(room.getCharacterJustAttacked(0));
        room.setCharacterJustAttacked(0, true);
        assertTrue(room.getCharacterJustAttacked(0));
    }
    
    @Test
    public void testIsCharAgressive() {
        characters.add(testCharacter);
        // By default, a Character returns false for isAgressive.
        assertFalse(room.isCharAgressive(0));
    }
}
