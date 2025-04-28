// File: junit/models/AttackPlayerTest.java
package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AttackPlayerTest {
    @Test
    public void testDamageMultiplier() {
        AttackPlayer ap = new AttackPlayer(null, 0, 0, null, null, null, 20, 20);
        assertTrue(ap.getAttackBoost() > 0);
    }
}
