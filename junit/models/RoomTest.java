
package models;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class RoomTest {
    @Test
    public void testRoomInventory() {
        ArrayList<Item> items = new ArrayList<>();
        Inventory inventory = new Inventory(items, 100);
        Connections connections = new Connections();
        ArrayList<Character> characters = new ArrayList<>();

        Room room = new Room("Test Room", inventory, connections, characters, "desc", "short");
        assertEquals("Test Room", room.getRoomName());
        assertEquals("desc", room.getRoomDescription());

        Item item = new Item(10, 1, "Coin", null, "desc", "short");
        room.addItem(item);
        assertEquals(1, room.getInventorySize());
        assertEquals("Coin", room.getItemName(0));
    }
}
