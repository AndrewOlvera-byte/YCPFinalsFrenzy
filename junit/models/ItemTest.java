
package models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {
    @Test
    public void testItemProperties() {
        String[] components = {"wood", "metal"};
        Item item = new Item(10, 5, "Test Item", components, "A test item description.", "A test item.");

        assertEquals(10, item.getValue());
        assertEquals(5, item.getWeight());
        assertEquals("Test Item", item.getName());
        assertArrayEquals(components, item.getComponents());
        assertEquals("A test item description.", item.getDescription());
        assertEquals("A test item.", item.getDescription());
    }
}
