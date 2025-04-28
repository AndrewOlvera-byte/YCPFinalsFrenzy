// File: junit/GameEngine/GameEngineTest.java
package GameEngine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameEngineTest {
    private GameEngine engine;

    @BeforeEach
    public void setUp() {
        engine = new GameEngine();
        engine.start();
    }

    @Test
    public void testStartSetsRunning() {
        assertTrue(engine.getRunningMessage() != null);
    }

    @Test
    public void testInitialRoomName() {
        String name = engine.getCurrentRoomName();
        assertNotNull(name);
    }

    @Test
    public void testPlayerAttackNoTarget() {
        String res = engine.playerAttackChar(-1, -1);
        assertTrue(res.contains("Attack who with what?"));
    }
}
