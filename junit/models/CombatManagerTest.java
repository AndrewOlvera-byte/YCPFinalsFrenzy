package models;
import models.CombatManager;
import GameEngine.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

class CombatManagerTest {
    private CombatManager combatManager;
    private GameEngine gameEngine;
    private Player player;
    private Room room;
    private NPC enemy;

    @BeforeEach
    void setUp() {
        // Create real objects
        gameEngine = new GameEngine();
        player = new Player("TestPlayer", 100, 0, new Inventory(new ArrayList<>(), 100), 
                          "Long desc", "Short desc", 1.0, 0, 0);
        
        // Create enemy NPC
        Inventory enemyInv = new Inventory(new ArrayList<>(), 100);
        enemy = new NPC("TestEnemy", 50, true, new String[]{"Hello"}, 10, enemyInv, 
                       "Enemy desc", "Short desc");
        
        // Create room with enemy
        ArrayList<Character> characters = new ArrayList<>();
        characters.add(enemy);
        room = new Room("TestRoom", new Inventory(new ArrayList<>(), 100), 
                       new Connections(), characters, "Long desc", "Short desc", 
                       new ArrayList<>());

        // Setup game engine
        ArrayList<Room> rooms = new ArrayList<>();
        rooms.add(room);
        gameEngine.getRooms().addAll(rooms);
        gameEngine.setCurrentRoomNum(0);
        gameEngine.setPlayer(player);

        combatManager = new CombatManager(gameEngine);
    }

   
    @Test
    void testDefenseAndArmorReduction() {
        // Give enemy a weapon
        Weapon enemyWeapon = new Weapon(1, 1, "EnemySword", null, 20, "Enemy sword", "A sword");
        enemy.getInventory().addItem(enemyWeapon);
        
        // Set player's defense
        player.setdefenseBoost(5);
        
        // Add armor
        Armor armor = new Armor(1, 1, "TestArmor", null, "Test armor", "Armor", 0, 0, 5, ArmorSlot.TORSO);
        player.equip(ArmorSlot.TORSO, armor);
        
        // Test enemy attack
        int damage = combatManager.charAttackPlayer(0, 0, true);
        
        // Damage should be: 20 - 5 (defense) - 5 (armor) = 10
        assertEquals(10, damage);
        assertEquals(90, player.getHp()); // 100 - 10 = 90
    }

    @Test
    void testNonAggressiveEnemy() {
        // Make enemy non-aggressive
        enemy = new NPC("TestEnemy", 50, false, new String[]{"Hello"}, 10, 
                       new Inventory(new ArrayList<>(), 100), "Enemy desc", "Short desc");
        
        // Give enemy a weapon
        Weapon enemyWeapon = new Weapon(1, 1, "EnemySword", null, 20, "Enemy sword", "A sword");
        enemy.getInventory().addItem(enemyWeapon);
        
        // Test attack
        int damage = combatManager.charAttackPlayer(0, 0, false);
        
        // Should not attack
        assertEquals(0, damage);
        assertEquals(100, player.getHp()); // Player HP should be unchanged
    }
}
