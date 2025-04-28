// File: junit/models/AttackPlayerTest.java
package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AttackPlayerTest {
    @Test
    public void testDamageMultiplier() {
        AttackPlayer ap = new AttackPlayer();
        assertTrue(ap.getAttackBoost() > 0);
    }
}
