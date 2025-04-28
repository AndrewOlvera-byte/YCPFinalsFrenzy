// File: junit/models/DefensePlayerTest.java
package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DefensePlayerTest {
    @Test
    public void testDefenseBoost() {
        DefensePlayer dp = new DefensePlayer(null, 0, 0, null, null, null, 20, 20);
        assertTrue(dp.getDefenseBoost() >= 0);
    }
}
