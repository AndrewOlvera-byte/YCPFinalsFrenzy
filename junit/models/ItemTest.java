// File: junit/models/ItemTest.java
package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {
    @Test
    public void testAttributes() {
        Item it = new Item(5, 2, "Foo", null, "Long", "Short");
        assertEquals("Foo", it.getName());
        assertEquals(5, it.getValue());
        assertEquals("Long", it.getDescription());
    }
}
