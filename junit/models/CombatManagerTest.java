package models;
import models.CombatManager;
import GameEngine.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CombatManagerTest {
    private CombatManager combatManager;
    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        engine.start();
        combatManager = new CombatManager(engine);
    }

 
}
