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

    @BeforeEach
    void setUp() {
        player = new Player("Hero", 120, 10, new Inventory(new ArrayList<>(), 100), "Long desc", "Short desc", 1.5, 0, 0);
    }

    @Test
    void testPlayerInitialization() {
        assertEquals("Hero", player.getName());
        assertEquals(120, player.getHp());
        assertEquals(1.5, player.getdamageMulti());
    }

    @Test
    void testDamageMultiplierSetter() {
        player.setdamageMulti(2.0);
        assertEquals(2.0, player.getdamageMulti());
    }

    @Test
    void testHasKey() {
        Item key = new Item(5, 1, "goldKey", null, "Opens golden doors", "goldKey");
        player.addItem(key);
        assertTrue(player.hasKey("goldKey"));
        assertFalse(player.hasKey("silverKey"));
    }

    @Test
    void testSkillPoints() {
        // Test initial skill points
        assertEquals(10, player.getSkillPoints());

        // Test adding skill points
        player.addSkillPoints(5);
        assertEquals(15, player.getSkillPoints());

        // Test using skill points
        assertTrue(player.useSkillPoints(3));
        assertEquals(12, player.getSkillPoints());

        // Test using more skill points than available
        assertFalse(player.useSkillPoints(20));
        assertEquals(12, player.getSkillPoints());

        // Test setting skill points
        player.setSkillPoints(20);
        assertEquals(20, player.getSkillPoints());
    }
}
