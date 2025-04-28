package models;
import models.Character;
import models.Inventory;
import models.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

class CharacterTest {
    private Character character;

    @BeforeEach
    void setUp() {
        character = new Character("TestChar", 100, new Inventory(new ArrayList<>(), 100), "Long desc", "Short desc");
    }

    @Test
    void testCharacterInitialization() {
        assertEquals("TestChar", character.getName());
        assertEquals(100, character.getHp());
    }

    @Test
    void testSetAndGetHp() {
        character.setHp(80);
        assertEquals(80, character.getHp());
    }

    @Test
    void testInventoryInteraction() {
        Item item = new Item(10, 5, "Sword", null, "Sharp sword", "Sword");
        character.addItem(item);
        assertEquals("Sword", character.getItemName(0));
        assertEquals(1, character.getInventorySize());
    }
}
