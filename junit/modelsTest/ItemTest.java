package modelsTest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import models.Item;

public class ItemTest {
    private Item item;
    
    @Before
    public void setUp() {
        // Create an Item with specific values.
        String[] components = {"Wood", "Metal"};
        item = new Item(75, 12, "Axe", components);
    }
    
    @Test
    public void testGetters() {
        assertEquals(75, item.getValue());
        assertEquals(12, item.getWeight());
        assertEquals("Axe", item.getName());
        assertArrayEquals(new String[]{"Wood", "Metal"}, item.getComponents());
    }
    
    @Test
    public void testSetters() {
        item.setValue(100);
        item.setWeight(15);
        item.setName("Battle Axe");
        String[] newComponents = {"Steel", "Leather"};
        item.setComponents(newComponents);
        
        assertEquals(100, item.getValue());
        assertEquals(15, item.getWeight());
        assertEquals("Battle Axe", item.getName());
        assertArrayEquals(newComponents, item.getComponents());
    }
}
