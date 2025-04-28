package models;
import models.Inventory;
import models.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

class InventoryTest {
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory(new ArrayList<>(), 100);
    }

    @Test
    void testAddItem() {
        Item item = new Item(10, 5, "Key", null, "Opens doors", "Key");
        inventory.addItem(item);
        assertEquals(1, inventory.getSize());
    }

    @Test
    void testRemoveItem() {
        Item item = new Item(10, 5, "Key", null, "Opens doors", "Key");
        inventory.addItem(item);
        inventory.removeItem(0);
        assertEquals(0, inventory.getSize());
    }

    @Test
    void testGetItemName() {
        Item item = new Item(10, 5, "Lantern", null, "Light source", "Lantern");
        inventory.addItem(item);
        assertEquals("Lantern", inventory.getItemName(0));
    }
}
