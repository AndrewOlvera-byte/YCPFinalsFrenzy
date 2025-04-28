package models;
import models.InventoryManager;
import GameEngine.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryManagerTest {
    private GameEngine engine;
    private InventoryManager manager;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        engine.start();
        manager = new InventoryManager(engine);
    }

    
}
