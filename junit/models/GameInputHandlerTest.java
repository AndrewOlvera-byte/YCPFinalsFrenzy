package models;
import models.GameInputHandler;
import GameEngine.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameInputHandlerTest {
    private GameInputHandler inputHandler;
    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        engine.start();
        inputHandler = new GameInputHandler(engine);
    }

  
}
