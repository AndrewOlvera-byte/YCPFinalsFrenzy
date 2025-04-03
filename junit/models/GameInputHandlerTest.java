
package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import GameEngine.GameEngine;

import static org.junit.jupiter.api.Assertions.*;

public class GameInputHandlerTest {
    private GameInputHandler handler;

    @BeforeEach
    public void setUp() {
        GameEngine engine = new GameEngine();
        engine.start();
        handler = new GameInputHandler(engine);
    }

    @Test
    public void testInvalidCommand() {
        boolean result = handler.processInput("flibber flabber");
        assertFalse(result); // Should be invalid
    }

    @Test
    public void testHelpCommand() {
        boolean result = handler.processInput("help");
        assertTrue(result); // Should be valid
    }
}
