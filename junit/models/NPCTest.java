
package models;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class NPCTest {
    @Test
    public void testAggressionToggle() {
        ArrayList<Item> items = new ArrayList<>();
        Inventory inventory = new Inventory(items, 30);
        NPC npc = new NPC("Enemy", 150, false, new String[]{"Hello!"}, 20, inventory, "desc", "short");

        assertFalse(npc.getAggresion());
        npc.setAgression(true);
        assertTrue(npc.getAggresion());
    }
}
