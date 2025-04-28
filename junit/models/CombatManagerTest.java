// File: junit/models/CombatManagerTest.java
package models;

import GameEngine.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CombatManagerTest {
    private CombatManager cm;
    private GameEngine eng;

    @BeforeEach
    public void setUp() {
        eng = new GameEngine();
        eng.start();
        cm = new CombatManager(eng);
    }

    @Test
    public void testPlayerAttackInvalid() {
        assertTrue(cm.playerAttackChar(-1, -1).contains("Attack who with what?"));
    }
}
