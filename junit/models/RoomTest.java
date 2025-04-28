// File: junit/models/RoomTest.java
package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

public class RoomTest {
    @Test
    public void testRoomBasics() {
        Room r = new Room("Hall", new Inventory(null,0), new Connections(), new ArrayList<>(), null, "L", "S");
        assertEquals("Hall", r.getRoomName());
        r.addItem(new Item(1,1,"X",null,"L","S"));
        assertEquals(1, r.getInventorySize());
        r.removeItem(0);
        assertEquals(0, r.getInventorySize());
    }
}
