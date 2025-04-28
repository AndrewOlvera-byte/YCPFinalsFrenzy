// File: junit/models/GameInputHandlerTest.java
package models;

import GameEngine.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        assertFalse(handler.processInput("foobar"));
    }

    @Test
    public void testHelpCommand() {
        assertTrue(handler.processInput("help"));
    }
}
