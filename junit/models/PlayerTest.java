package models;
import models.Inventory;
import models.Player;
import models.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

class PlayerTest {
    private Player player;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory(new ArrayList<>(), 100);
        player = new Player("TestPlayer", 100, 0, inventory, "Long desc", "Short desc", 1.0, 0, 0);
    }

    @Test
    void testInitialState() {
        assertEquals("TestPlayer", player.getName());
        assertEquals(100, player.getHp());
        assertEquals(0, player.getSkillPoints());
        assertEquals(0, player.getAvailableSkillPoints());
        assertEquals(1, player.getLevel());
        assertEquals(0, player.getAttackBoost());
        assertEquals(0, player.getdefenseBoost());
    }





    @Test
    void testMultipleLevelUps() {
        // Give enough points for multiple level ups
        // Level 1->2 needs 10, Level 2->3 needs 25 (total 35)
        player.setSkillPoints(50);
        
        assertEquals(3, player.getLevel()); // Should be level 3
        assertTrue(player.getAvailableSkillPoints() > 0); // Should have some points left
        assertEquals(120, player.getMaxHp()); // Base HP + 20 from two level ups
    }

  

    @Test
    void testRequiredPointsForNextLevel() {
        assertEquals(10, player.getRequiredSkillPointsForNextLevel()); // Level 1
        
        player.setSkillPoints(10); // Level up to 2
        assertEquals(25, player.getRequiredSkillPointsForNextLevel()); // Level 2
        
        player.setSkillPoints(35); // Level up to 3
        assertEquals(62, player.getRequiredSkillPointsForNextLevel()); // Level 3
    }

    @Test
    void testAttackAndDefenseBoosts() {
        player.setSkillPoints(10);
        player.setAttackBoost(5);
        player.setdefenseBoost(3);
        
        assertEquals(5, player.getAttackBoost());
        assertEquals(3, player.getdefenseBoost());
    }

    @Test
    void testInventoryManagement() {
        Item testItem = new Item(1, 1, "TestItem", null, "Test desc", "Short desc");
        inventory.addItem(testItem);
        
        assertEquals(1, player.getInventorySize());
        assertEquals("TestItem", player.getItemName(0));
    }

    @Test
    void testHealthManagement() {
        player.setHp(50);
        assertEquals(50, player.getHp());
        
        player.setMaxHp(150);
        assertEquals(150, player.getMaxHp());
    }

    @Test
    void testDamageMultiplier() {
        player.setdamageMulti(1.5);
        assertEquals(1.5, player.getdamageMulti());
    }

    @Test
    void testHasKey() {
        Item key = new Item(5, 1, "goldKey", null, "Opens golden doors", "goldKey");
        player.addItem(key);
        assertTrue(player.hasKey("goldKey"));
        assertFalse(player.hasKey("silverKey"));
    }
}
