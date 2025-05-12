package models;
import models.Inventory;
import models.Item;
import models.Room;
import models.Connections;
import models.Character;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

class RoomTest {
    private Room room;
    private Inventory roomInventory;
    private ArrayList<models.Character> characters;
    private ArrayList<Companion> companions;
    private NPC testEnemy;
    private Player testPlayer;

    @BeforeEach
    void setUp() {
        roomInventory = new Inventory(new ArrayList<>(), 100);
        characters = new ArrayList<>();
        companions = new ArrayList<>();
        
        // Create test enemy
        Inventory enemyInv = new Inventory(new ArrayList<>(), 100);
        testEnemy = new NPC("TestEnemy", 50, true, new String[]{"Hello"}, 10, enemyInv, 
                          "Enemy desc", "Short desc");
        characters.add(testEnemy);
        
        // Create test player
        testPlayer = new Player("TestPlayer", 100, 0, new Inventory(new ArrayList<>(), 100),
                              "Long desc", "Short desc", 1.0, 0, 0);
        
        room = new Room("TestRoom", roomInventory, new Connections(), characters,
                       "Required Key", "Long room desc", "Short room desc", companions);
    }

    @Test
    void testInitialState() {
        assertEquals("TestRoom", room.getRoomName());
        assertEquals("Required Key", room.getRequiredKey());
        assertEquals(1, room.getCharacterContainerSize());
        assertEquals(0, room.getCompanionContainerSize());
    }

    @Test
    void testCharacterManagement() {
        // Test getting character
        assertEquals(testEnemy, room.getCharacter(0));
        assertEquals("TestEnemy", room.getCharacterName(0));
        assertEquals(50, room.getCharacterHealth(0));
        
        // Test removing character
        room.removeCharacter(0);
        assertEquals(0, room.getCharacterContainerSize());
    }


    @Test
    void testCompanionManagement() {
        // Create and add companion
        Companion companion = new Companion("TestCompanion", 100, true, new String[]{"Hello"}, 5,
                                          new Inventory(new ArrayList<>(), 100),
                                          "Companion desc", "Short desc", true);
        room.addCompanion(companion);
        
        assertEquals(1, room.getCompanionContainerSize());
        assertEquals("TestCompanion", room.getCompanionName(0));
        assertEquals(100, room.getCompanionHealth(0));
        
        // Test removing companion
        room.removeCompanion(0);
        assertEquals(0, room.getCompanionContainerSize());
    }

    @Test
    void testInventoryManagement() {
        // Add items to room
        Item sword = new Item(1, 1, "RoomSword", null, "A sword", "Short desc");
        Item potion = new Item(2, 2, "RoomPotion", null, "A potion", "Short desc");
        
        room.addItem(sword);
        room.addItem(potion);
        
        assertEquals(2, room.getInventorySize());
        assertEquals("RoomSword", room.getItemName(0));
        assertEquals("RoomPotion", room.getItemName(1));
        
        // Test removing items
        Item removedItem = room.getItem(0);
        room.removeItem(0);
        
        assertEquals("RoomSword", removedItem.getName());
        assertEquals(1, room.getInventorySize());
    }

    @Test
    void testRoomConnections() {
        Room connectedRoom = new Room("ConnectedRoom", new Inventory(new ArrayList<>(), 100),
                                    new Connections(), new ArrayList<>(), null,
                                    "Long desc", "Short desc", new ArrayList<>());
        
        // Add connections manually since Connections class doesn't have direct add method
        connectedRoom.getConnections().setConnection("North", 1);
        connectedRoom.getConnections().setConnection("South", 2);
        
        assertEquals(1, connectedRoom.getConnectedRoom("North"));
        assertEquals(2, connectedRoom.getConnectedRoom("South"));
        assertEquals(-1, connectedRoom.getConnectedRoom("East")); // No connection
    }

    @Test
    void testAggressiveCharacters() {
        // Test aggressive NPC
        assertTrue(room.isCharAgressive(0)); // First NPC is aggressive
        
        // Add non-aggressive NPC
        NPC peaceful = new NPC("Peaceful", 50, false, new String[]{"Hello"}, 10,
                              new Inventory(new ArrayList<>(), 100),
                              "Peaceful desc", "Short desc");
        characters.add(peaceful);
        
        assertFalse(room.isCharAgressive(1)); // Second NPC is not aggressive
    }

    @Test
    void testCharacterAttackDamage() {
        // Add weapon to enemy
        Weapon enemyWeapon = new Weapon(1, 1, "EnemySword", null, 20, "Enemy sword", "A sword");
        testEnemy.addItem(enemyWeapon);
        
        int damage = room.getCharacterAttackDmg(0, 0);
        assertEquals(20, damage); // Base weapon damage
    }

    @Test
    void testRequiredKeyManagement() {
        assertEquals("Required Key", room.getRequiredKey());
        
        room.setRequiredKey(null);
        assertNull(room.getRequiredKey());
        
        room.setRequiredKey("New Key");
        assertEquals("New Key", room.getRequiredKey());
    }

    @Test
    void testRoomName() {
        assertEquals("TestRoom", room.getRoomName());
        
        room.setRoomName("New Room Name");
        assertEquals("New Room Name", room.getRoomName());
    }

    @Test
    void testCharacterJustAttacked() {
        assertFalse(room.getCharacterJustAttacked(0));
        
        room.setCharacterJustAttacked(0, true);
        assertTrue(room.getCharacterJustAttacked(0));
    }
}
