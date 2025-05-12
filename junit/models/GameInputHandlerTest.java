package models;
import models.GameInputHandler;
import GameEngine.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

class GameInputHandlerTest {
    private GameInputHandler inputHandler;
    private GameEngine gameEngine;
    private Player player;

    @BeforeEach
    void setUp() {
        gameEngine = new GameEngine();
        player = new Player("TestPlayer", 100, 0, new Inventory(new ArrayList<>(), 100),
                          "Long desc", "Short desc", 1.0, 0, 0);
        gameEngine.setPlayer(player);
        inputHandler = new GameInputHandler(gameEngine);
    }

 

  

    @Test
    void testParseInput() {
        // Test basic command parsing
        String[] result = GameInputHandler.parseInput("increase attack");
        assertEquals("increase", result[0]);
        assertEquals("attack", result[1]);

        // Test command with preposition
        result = GameInputHandler.parseInput("attack enemy with sword");
        assertEquals("attack", result[0]);
        assertEquals("enemy", result[1]);
        assertEquals("sword", result[3]);
    }

    @Test
    void testInvalidCommands() {
        assertFalse(inputHandler.processInput("invalidcommand"));
        assertTrue(gameEngine.getRunningMessage().contains("Invalid command"));
    }

}
