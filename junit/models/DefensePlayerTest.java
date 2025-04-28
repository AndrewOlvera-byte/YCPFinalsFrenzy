// File: junit/models/DefensePlayerTest.java
package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DefensePlayerTest {
    @Test
    public void testDefenseBoost() {
        DefensePlayer dp = new DefensePlayer();
        assertTrue(dp.getDefenseBoost() >= 0);
    }
}
