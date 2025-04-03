
package models;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

public class UtilityTest {
    @Test
    public void testUtilityProperties() {
        Utility util = new Utility(5, 1, "Potion", null, "Heals you.", "Heals.", 50, 1.5);

        assertEquals(50, util.getHealing());
        
        assertEquals("Potion", util.getName());
    }
}
