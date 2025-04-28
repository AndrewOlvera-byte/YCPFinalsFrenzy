// File: junit/models/UtilityTest.java
package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UtilityTest {
    @Test
    public void testHealingUtility() {
        Utility u = new Utility(10,1,"Pot",null,"L","S",40,0.0);
        assertEquals(40, u.getHealing());
    }
}
