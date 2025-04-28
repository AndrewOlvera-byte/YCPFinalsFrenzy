// File: junit/models/RoomManagerTest.java
package models;

import GameEngine.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoomManagerTest {
    private GameEngine engine;

    @BeforeEach
    public void setUp() {
        engine = new GameEngine();
        engine.start();
    }

    @Test
    public void testRoomsLoaded() {
        assertFalse(engine.getRooms().isEmpty());
    }
}
