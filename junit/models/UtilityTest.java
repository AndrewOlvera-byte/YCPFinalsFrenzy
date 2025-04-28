package models;
import models.Utility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UtilityTest {
    private Utility utility;

    @BeforeEach
    void setUp() {
        utility = new Utility(20, 5, "Healing Potion", null, "Heals you", "Potion", 30, 0.0);
    }

    @Test
    void testUtilityInitialization() {
        assertEquals(20, utility.getValue());
        assertEquals(30, utility.getHealing());
        assertEquals(0.0, utility.getDamageMulti());
    }

    @Test
    void testAddComponent() {
        utility.addComponent("Magic Herb");
        assertTrue(utility.getComponents().contains("Magic Herb"));
    }
}
