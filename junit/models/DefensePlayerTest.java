package models;
import models.DefensePlayer;
import models.Inventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

class DefensePlayerTest {
    private DefensePlayer defensePlayer;

    @BeforeEach
    void setUp() {
        defensePlayer = new DefensePlayer("Defender", 100, 5, new Inventory(new ArrayList<>(), 100), "Long desc", "Short desc", 1.0, 2.5);
    }

    @Test
    void testDefensePlayerInitialization() {
        assertEquals(150, defensePlayer.getHp()); // 100 base + 50 bonus
        assertEquals(2.5, defensePlayer.getDefenseBoost());
    }

    @Test
    void testSetDefenseBoost() {
        defensePlayer.setDefenseBoost(3.0);
        assertEquals(3.0, defensePlayer.getDefenseBoost());
    }
}
