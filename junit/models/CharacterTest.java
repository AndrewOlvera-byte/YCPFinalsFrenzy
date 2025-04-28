// File: junit/models/CharacterTest.java
package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterTest {
    @Test
    public void testBaseCharacter() {
        Character c = new NPC("X", 10, false, new String[]{}, 5, null, "L","S");
        assertEquals(10, c.getHp());
        c.setHp(0);
        assertEquals(0, c.getHp());
    }
}
