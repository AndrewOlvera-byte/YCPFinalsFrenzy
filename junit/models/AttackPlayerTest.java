package models;
import models.AttackPlayer;
import models.Inventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

class AttackPlayerTest {
    private AttackPlayer attackPlayer;

    @BeforeEach
    void setUp() {
        attackPlayer = new AttackPlayer("Attacker", 100, 5, new Inventory(new ArrayList<>(), 100), "Long desc", "Short desc", 2.0, 1.5);
    }

    @Test
    void testAttackPlayerInitialization() {
        assertEquals(1.5, attackPlayer.getAttackBoost());
        assertEquals(2.0, attackPlayer.getdamageMulti());
    }

    @Test
    void testSetAttackBoost() {
        attackPlayer.setAttackBoost(2.0);
        assertEquals(2.0, attackPlayer.getAttackBoost());
    }
}
