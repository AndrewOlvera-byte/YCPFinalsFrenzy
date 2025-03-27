package GameEngineTest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import models.Room;
import models.Response;
import models.Player;
import models.Inventory;
import models.Item;
import models.Weapon;

import GameEngine.GameEngine;

import java.util.ArrayList;

public class GameEngineTest {
    private GameEngine gameEngine;
    
    @Before
    public void setUp() {
        gameEngine = new GameEngine();
        gameEngine.loadData();
    }
    
    @Test
    public void testInitialRoom() {
        // After loadData(), the initial room should be "First Room".
        assertEquals("First Room", gameEngine.getCurrentRoomName());
    }
    
    @Test
    public void testUpdateCurrentRoom() {
        // From "First Room", updateCurrentRoom("North") should work (as set in loadRooms).
        boolean updated = gameEngine.updateCurrentRoom("North");
        assertTrue(updated);
        // After moving north, current room should be "Second Room".
        assertEquals("Second Room", gameEngine.getCurrentRoomName());
    }
    
    @Test
    public void testGetMapOutput() {
        // Initially in "First Room", the "North" connection should return 1.
        int mapOutput = gameEngine.getMapOutput("North");
        assertEquals(1, mapOutput);
        
        // For an unset direction, expect -1.
        int eastOutput = gameEngine.getMapOutput("East");
        assertEquals(-1, eastOutput);
    }
    
    @Test
    public void testPlayerAttackChar() {
        // Move to "Second Room" where a character ("Moe") exists.
        gameEngine.updateCurrentRoom("North");
        // The player's weapon from loadPlayer ("Dagger") has attack damage 40.
        // Attack the character "Moe".
        String attackMessage = gameEngine.playerAttackChar(gameEngine.CharItemNameToID("Dagger"), gameEngine.CharNameToID("Moe"));
        // Expected outcome: since 400-40 > 0, the boss's health should reduce.
        assertEquals("\nMoe has taken 40 damage.\nMoe Hit back for 90", attackMessage);
        
        // Use reflection to access the current room (private field "rooms") and check "Moe"'s health.
        Room currentRoom = getCurrentRoomFromGameEngine();
        assertNotNull(currentRoom);
        assertEquals(360, currentRoom.getCharacterHealth(0));
    }
    
    @Test
    public void testCharAttackPlayer() {
        // Move to "Second Room" where "Moe" is located.
        gameEngine.updateCurrentRoom("North");
        int playerHpBefore = getPlayerHpViaReflection(gameEngine);
        // Boss "Moe" uses his weapon (the "Trident" with 90 damage) to attack.
        gameEngine.charAttackPlayer(0, 0);
        int playerHpAfter = getPlayerHpViaReflection(gameEngine);
        // Player's HP should decrease by 90.
        assertEquals(playerHpBefore - 90, playerHpAfter);
    }
    
    @Test
    public void testGetGo() {
        // In "First Room", "go north" should move to "Second Room".
        String message = gameEngine.getGo("North");
        assertTrue(message.contains("Moved to Room Second Room"));
        
        // Trying an invalid direction should return an appropriate message.
        String invalidMessage = gameEngine.getGo("Up");
        assertEquals("\nThis is not a valid direction", invalidMessage);
    }
    
    @Test
    public void testProcessInput() {
        // Starting in "First Room", process a command to go north.
        gameEngine.processInput("go north");
        Response response = gameEngine.display();
        // The running message should indicate the room change.
        assertTrue(response.getMessage().contains("Moved to Room Second Room"));
    }
    
    @Test
    public void testDisplayResponse() {
        Response response = gameEngine.display();
        // Verify that the Response object fields are not null.
        assertNotNull(response.getRoomInventory());
        assertNotNull(response.getPlayerInventory());
        assertNotNull(response.getCharactersInRoom());
        assertNotNull(response.getPlayerInfo());
        assertNotNull(response.getRoomConnections());
        assertNotNull(response.getMessage());
        assertNotNull(response.getError());
    }
    
    // Helper method: use reflection to get the current room from GameEngine.
    @SuppressWarnings("unchecked")
    private Room getCurrentRoomFromGameEngine() {
        try {
            java.lang.reflect.Field roomsField = GameEngine.class.getDeclaredField("rooms");
            roomsField.setAccessible(true);
            ArrayList<Room> rooms = (ArrayList<Room>) roomsField.get(gameEngine);
            return rooms.get(gameEngine.getCurrentRoomNum());
        } catch(Exception e) {
            return null;
        }
    }
    
    // Helper method: use reflection to get the player's HP from GameEngine.
    private int getPlayerHpViaReflection(GameEngine ge) {
        try {
            java.lang.reflect.Field playerField = GameEngine.class.getDeclaredField("player");
            playerField.setAccessible(true);
            Player player = (Player) playerField.get(ge);
            return player.getHp();
        } catch(Exception e) {
            return -1;
        }
    }
}
