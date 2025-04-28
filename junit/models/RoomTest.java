package models;
import models.Inventory;
import models.Item;
import models.Room;
import models.Connections;
import models.Character;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

class RoomTest {
    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room("Test Room", new Inventory(new ArrayList<>(), 100), new Connections(), new ArrayList<>(), "MasterKey", "A vast room", "A room");
    }

    @Test
    void testRoomName() {
        assertEquals("Test Room", room.getRoomName());
    }

    @Test
    void testAddItem() {
        Item item = new Item(5, 1, "Torch", null, "Lights up", "Torch");
        room.addItem(item);
        assertEquals(1, room.getInventorySize());
    }

    @Test
    void testGetRequiredKey() {
        assertEquals("MasterKey", room.getRequiredKey());
    }

    @Test
    void testRoomDescriptionSwitch() {
        assertEquals("A vast room", room.getRoomDescription());
        assertEquals("A room", room.getRoomDescription()); // After examining
    }
}
