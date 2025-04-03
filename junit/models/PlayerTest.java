
package models;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {
    @Test
    public void testPlayerAttributes() {
        ArrayList<Item> items = new ArrayList<>();
        Inventory inventory = new Inventory(items, 50);
        Player player = new Player("Hero", 100, 0, inventory, "Long desc", "Short desc", 1);

        assertEquals("Hero", player.getName());
        assertEquals(100, player.getHp());
        assertEquals(0, player.getGold());
        assertEquals("Long desc", player.getCharDescription());
        assertEquals(1.0, player.getdamageMulti());

        player.setHp(90);
        assertEquals(90, player.getHp());

        player.setdamageMulti(1.5);
        assertEquals(1.5, player.getdamageMulti());
    }
}
