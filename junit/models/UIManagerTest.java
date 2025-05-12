package models;
import models.UIManager;
import GameEngine.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

class UIManagerTest {
    private UIManager uiManager;
    private GameEngine gameEngine;
    private Player player;
    private Room room;

    @BeforeEach
    void setUp() {
        gameEngine = new GameEngine();
        player = new Player("TestPlayer", 100, 0, new Inventory(new ArrayList<>(), 100),
                          "Long desc", "Short desc", 1.0, 0, 0);
        gameEngine.setPlayer(player);

        // Create a test room
        room = new Room("TestRoom", new Inventory(new ArrayList<>(), 100),
                       new Connections(), new ArrayList<>(),
                       "Long room desc", "Short room desc", new ArrayList<>());
        ArrayList<Room> rooms = new ArrayList<>();
        rooms.add(room);
        gameEngine.getRooms().addAll(rooms);

        uiManager = new UIManager(gameEngine);
    }

    @Test
    void testPlayerInfoDisplay() {
        // Set up player with various stats
        player.setHp(80);

        String info = uiManager.getPlayerInfo();
        
        // Check basic info
        assertTrue(info.contains("TestPlayer"));
        assertTrue(info.contains("Health: 80"));
        
        // Check armor slots
        assertTrue(info.contains("Equipped Armor:"));
        assertTrue(info.contains("HEAD: none"));
        assertTrue(info.contains("TORSO: none"));
    }

    @Test
    void testPlayerInventoryOverlay() {
        // Add some items to player inventory
        Item sword = new Item(1, 1, "TestSword", null, "A test sword", "Short desc");
        Item potion = new Item(2, 2, "TestPotion", null, "A test potion", "Short desc");
        player.getInventory().addItem(sword);
        player.getInventory().addItem(potion);

        String overlay = uiManager.getPlayerInventoryOverlay();
        
        // Check if items are displayed
        assertTrue(overlay.contains("TestSword"));
        assertTrue(overlay.contains("TestPotion"));
    }



    @Test
    void testRoomItemsOverlay() {
        // Add items to room
        Item sword = new Item(1, 1, "RoomSword", null, "A sword in room", "Short desc");
        room.addItem(sword);

        String overlay = uiManager.getRoomItemsOverlay();
        assertTrue(overlay.contains("RoomSword"));
    }

    @Test
    void testRoomCharactersOverlay() {
        // Add an NPC to room
        NPC npc = new NPC("TestNPC", 50, true, new String[]{"Hello"},
                         10, new Inventory(new ArrayList<>(), 100),
                         "NPC desc", "Short desc");
        room.getCharacterContainer().add(npc);

        String overlay = uiManager.getRoomCharactersOverlay();
        assertTrue(overlay.contains("TestNPC"));
    }

    @Test
    void testDisplayResponse() {
        Response response = uiManager.display();
        
        assertNotNull(response);
        assertNotNull(response.getRoomImage());
        assertNotNull(response.getPlayerInventory());
        assertNotNull(response.getRoomInventory());
        
        // Check if overlays are included
        assertNotNull(response.getPlayerInventoryOverlay());
        assertNotNull(response.getRoomItemsOverlay());
    }

   

  
}
