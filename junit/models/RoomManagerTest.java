package models;
import GameEngine.GameEngine;
import models.RoomManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoomManagerTest {
    private RoomManager roomManager;
    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        engine.start();
        roomManager = new RoomManager(engine);
    }

   
}
