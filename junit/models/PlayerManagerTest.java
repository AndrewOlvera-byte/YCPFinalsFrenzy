// File: junit/models/PlayerManagerTest.java
package models;

import GameEngine.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerManagerTest {
    private GameEngine engine;

    @BeforeEach
    public void setUp() {
        engine = new GameEngine();
        engine.start();
    }

    @Test
    public void testLoadDefaults() {
        Player p = engine.getPlayer();
        assertNotNull(p.getName());
        assertTrue(p.getHp() > 0);
    }
}
