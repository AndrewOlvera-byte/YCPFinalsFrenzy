
package models;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class InventoryTest {
    @Test
    public void testInventoryOperations() {
        ArrayList<Item> items = new ArrayList<>();
        items.add(new Item(5, 2, "Apple", null, "A fruit", "Apple"));
        Inventory inv = new Inventory(items, 100);

        assertEquals(1, inv.getSize());
        assertEquals("Apple", inv.getItem(0).getName());
        assertEquals("Apple", inv.getItemName(0));
        assertEquals("Apple", inv.listItems());

        inv.removeItem(0);
        assertEquals("Inventory is empty.", inv.listItems());
    }
}
