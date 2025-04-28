// File: junit/models/PlayerManagerTest.java
package models;

import GameEngine.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerManagerTest {
    private GameEngine engine;

    @BeforeEach
    public void setUp() {
        engine = new GameEngine();
        engine.start();
    }

   
}
