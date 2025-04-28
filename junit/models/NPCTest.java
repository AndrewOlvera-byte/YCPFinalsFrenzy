// File: junit/models/NPCTest.java
package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NPCTest {
    @Test
    public void testNpcBasic() {
        NPC npc = new NPC("Moe", 50, false, new String[]{}, 10, null, "L", "S");
        assertEquals("Moe", npc.getName());
        assertFalse(npc.isAgressive());
        npc.setHp(0);
        assertEquals(0, npc.getHp());
    }
}
