// File: junit/models/GameStateManagerTest.java
package models;

import GameEngine.GameEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameStateManagerTest {
    @Test
    public void testLoadSaveCycle() {
        GameEngine eng = new GameEngine();
        eng.start();
        // simply ensure no exception on load/save
        GameStateManager.saveState(eng);
        assertDoesNotThrow(() -> GameStateManager.loadState(eng));
    }
}
