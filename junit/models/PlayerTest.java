// File: junit/models/PlayerTest.java
package models;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {
    @Test
    public void testPlayerAttributes() {
        Player p = new Player("Hero", 100, 1, new Inventory(new ArrayList<>(), 5), "L", "S", 1.2);
        assertEquals("Hero", p.getName());
        assertEquals(100, p.getHp());
        assertEquals(1.2, p.getdamageMulti());
        p.setHp(80);
        assertEquals(80, p.getHp());
    }
}
