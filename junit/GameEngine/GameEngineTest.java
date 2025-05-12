package GameEngine;
import GameEngine.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import models.*;
import java.util.ArrayList;

class GameEngineTest {
    private GameEngine engine;
    private Player player;
    private Room testRoom;
    private NPC testEnemy;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        
        // Create test player
        player = new Player("TestPlayer", 100, 0, new Inventory(new ArrayList<>(), 100),
                          "Long desc", "Short desc", 1.0, 0, 0);
        engine.setPlayer(player);

        // Create test room with enemy
        Inventory enemyInv = new Inventory(new ArrayList<>(), 100);
        testEnemy = new NPC("TestEnemy", 50, true, new String[]{"Hello"},
                           10, enemyInv, "Enemy desc", "Short desc");
        ArrayList<models.Character> characters = new ArrayList<>();
        characters.add(testEnemy);

        testRoom = new Room("TestRoom", new Inventory(new ArrayList<>(), 100),
                           new Connections(), characters, "Long desc", "Short desc",
                           new ArrayList<>());
        
        ArrayList<Room> rooms = new ArrayList<>();
        rooms.add(testRoom);
        engine.getRooms().addAll(rooms);
    }

   


  

    @Test
    void testGameOver() {
        // Set player HP to 0
        player.setHp(0);
        
        // Check game over state
        Response response = engine.display();
        assertTrue(response.isGameOver());
        assertNotNull(response.getGameOverImage());
    }

  

    @Test
    void testLevelProgression() {
        // Start at level 1
        assertEquals(1, player.getLevel());
        
        // Add enough points to level up
        player.setSkillPoints(15);
        assertTrue(player.getLevel() > 1);
        assertTrue(player.getMaxHp() > 100); // HP should increase with level
    }

   

 

    @Test
    void testUIDisplay() {
        Response response = engine.display();
        
        // Check if UI elements are present
        assertNotNull(response.getPlayerInventory());
        assertNotNull(response.getRoomInventory());
        
        // Check overlays
        assertNotNull(response.getPlayerInventoryOverlay());
        assertNotNull(response.getRoomItemsOverlay());
    }
}
