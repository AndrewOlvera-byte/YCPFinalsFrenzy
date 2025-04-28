// File: junit/models/InventoryTest.java
package models;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryTest {
    @Test
    public void testBasicOperations() {
        ArrayList<Item> items = new ArrayList<>();
        items.add(new Item(1, 1, "X", null, "L", "S"));
        Inventory inv = new Inventory(items, 10);
        assertEquals(1, inv.getSize());
        assertEquals("X", inv.getItemName(0));
        inv.removeItem(0);
        assertEquals(0, inv.getSize());
    }
}
