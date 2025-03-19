/*package GameEngineTest;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import GameEngine.GameEngine;
import models.Room;
import models.Player;
import models.Item;
import models.Inventory;
import models.Response;
import models.Character; // our game-specific character class

public class GameEngineTest {

    private GameEngine engine;
    private Room room;

    @Before
    public void setUp() throws Exception {
        engine = new GameEngine();
        engine.loadData();  // load rooms, player, etc.
        
        // Access the private 'rooms' field using reflection.
        Field roomsField = GameEngine.class.getDeclaredField("rooms");
        roomsField.setAccessible(true);
        ArrayList<Room> rooms = (ArrayList<Room>) roomsField.get(engine);
        room = rooms.get(0);  // use the first room for our tests
    }
    
    // Dummy implementation of models.Character for testing.
    private class DummyCharacter extends models.Character {
		private int health;
		

        public DummyCharacter(int health) {
        	
        	super("DummyCharacter", new Inventory(new ArrayList<Item>(), 32), 100);  // assumes a default constructor is available
            this.health = health;
        }
        
        @Override
        public int getAttackDmg(int itemNum) {
            return 10;
        }
        
        @Override
        public int getHealth() {
            return health;
        }
        
        @Override
        public void setHealth(int health) {
            this.health = health;
        }
        
        @Override
        public String getName() {
            return "DummyCharacter";
        }
    }
    
    @Test
    public void testUpdateCurrentRoom() {
        // Since loadRooms() does not set any connections,
        // updateCurrentRoom should return false for any direction.
        assertFalse(engine.updateCurrentRoom("north"));
    }
    
    @Test
    public void testPlayerAttackChar() throws Exception {
        // Use reflection to access the room's private character container.
        Field charContainerField = room.getClass().getDeclaredField("characterContainer");
        charContainerField.setAccessible(true);
        ArrayList<models.Character> charContainer = (ArrayList<models.Character>) charContainerField.get(room);
        
        // Add a dummy character with 50 health.
        DummyCharacter dummyChar = new DummyCharacter(50);
        charContainer.add(dummyChar);
        
        // Call playerAttackChar with any item number (0) and character index (0).
        // Player's attack damage (getAttackDmg) should be 10.
        engine.playerAttackChar(0, 0);
        
        // The dummy character’s health should have decreased from 50 to 40.
        assertEquals(40, dummyChar.getHealth());
    }
    
    @Test
    public void testCharAttackPlayer() throws Exception {
        // Use reflection to access the room's character container.
        Field charContainerField = room.getClass().getDeclaredField("characterContainer");
        charContainerField.setAccessible(true);
        ArrayList<models.Character> charContainer = (ArrayList<models.Character>) charContainerField.get(room);
        
        // Add a dummy character with 50 health.
        DummyCharacter dummyChar = new DummyCharacter(50);
        charContainer.add(dummyChar);
        
        // Access the private player field using reflection.
        Field playerField = GameEngine.class.getDeclaredField("player");
        playerField.setAccessible(true);
        Player player = (Player) playerField.get(engine);
        int initialHealth = player.getHealth();
        
        // When the character attacks the player, the player's health should drop by 10.
        engine.charAttackPlayer(0, 0);
        assertEquals(initialHealth - 10, player.getHealth());
    }
    
    @Test
    public void testPickupAndDropItem() throws Exception {
        // Create a dummy item. We assume an Item(String name, int weight) constructor.
    	String[] stringArr = {"Screw"};
        Item dummyItem = new Item(10, 2, "Sword", stringArr);
        
        // Add the dummy item to the room's inventory.
        room.addItem(dummyItem);
        
        // The player picks up the item from index 0 in the room inventory.
        engine.pickupItem(0);
        
        // Access the private player field to check the player's inventory.
        Field playerField = GameEngine.class.getDeclaredField("player");
        playerField.setAccessible(true);
        Player player = (Player) playerField.get(engine);
        
        // Verify that the player's inventory now has the item.
        Item itemFromPlayer = player.getItem(0);
        assertEquals("Goggles", itemFromPlayer.getName());
        
        // Now, drop the item back to the room.
        engine.dropItem(0);
        
        // After dropping, the player's inventory should be empty.
        try {
            player.getItem(0);
            fail("Player inventory should be empty after dropping the item.");
        } catch (IndexOutOfBoundsException e) {
            // Expected behavior if the inventory is empty.
        }
        
        // Verify that the room's inventory now contains the item.
        Item itemFromRoom = room.getItem(0);
        assertEquals("Sword", itemFromRoom.getName());
    }
    
    @Test
    public void testDisplayAndProcessInput() {
        // processInput should return true for valid input.
        assertTrue(engine.processInput("test input"));
        
        // Test that display() returns a Response object with the expected dummy data.
        Response response = engine.display();
        assertNotNull(response);
        // Assuming Response has getters for its attributes.
        assertEquals("Test Room Inventory", response.getRoomInventory());
        assertEquals("Test Player Inventory", response.getPlayerInventory());
        assertEquals("Test room connections", response.getRoomConnections());
        assertEquals("Test message", response.getMessage());
        assertEquals("Test error", response.getError());
    }
}
*/