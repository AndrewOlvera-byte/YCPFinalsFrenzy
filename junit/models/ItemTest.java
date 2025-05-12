package models;
import models.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemTest {
    private Item item;

    @BeforeEach
    void setUp() {
        item = new Item(50, 10, "Magic Sword", new String[]{"Blade", "Gem"}, "A powerful sword", "Sword");
    }

    @Test
    void testItemInitialization() {
        assertEquals("Magic Sword", item.getName());
        assertEquals(50, item.getValue());
        assertEquals(10, item.getWeight());
    }

    @Test
    void testDescriptionSwitch() {
        assertEquals("A powerful sword", item.getDescription());
        item.getDescription(); // First call sets examined to true
        assertEquals("Sword", item.getDescription()); // Second call returns short description
    }

    @Test
    void testComponentsList() {
        assertEquals(2, item.getComponents().size());
        assertTrue(item.getComponents().contains("Blade"));
    }
}
