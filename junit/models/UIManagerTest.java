package models;
import models.UIManager;
import GameEngine.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UIManagerTest {
    private UIManager uiManager;
    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        engine.start();
        uiManager = new UIManager(engine);
    }

   
}
