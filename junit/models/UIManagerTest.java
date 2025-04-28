// File: junit/models/UIManagerTest.java
package models;

import GameEngine.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UIManagerTest {
    private UIManager ui;

    @BeforeEach
    public void setUp() {
        GameEngine eng = new GameEngine();
        eng.start();
        ui = new UIManager(eng);
    }

    @Test
    public void testHelpContainsKeywords() {
        String h = ui.getHelp();
        assertTrue(h.contains("attack"));
        assertTrue(h.contains("drop"));
    }
}
