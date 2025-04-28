// File: junit/models/InventoryManagerTest.java
package models;

import GameEngine.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryManagerTest {
    private GameEngine engine;

    @BeforeEach
    public void setUp() {
        engine = new GameEngine();
        engine.start();
    }

    @Test
    public void testPickupInvalid() {
        assertEquals("\n<b>Pick up what?</b>", engine.pickupItem(-1));
    }

    @Test
    public void testDropInvalid() {
        assertEquals("\n<b>Invalid item selection.</b>", engine.dropItem(99));
    }
}
