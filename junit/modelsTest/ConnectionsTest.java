package modelsTest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import models.Connections;
import models.Room;
import models.Inventory;
import models.Item;
import models.Weapon;
import models.Character;

import java.util.ArrayList;

public class ConnectionsTest {
    private Connections connections;
    private Room dummyRoom1;
    private Room dummyRoom2;
    
    @Before
    public void setUp() {
        connections = new Connections();
        
        // Create dummy rooms with minimal required parameters.
        Inventory inv1 = new Inventory(new ArrayList<Item>(), 100);
        Inventory inv2 = new Inventory(new ArrayList<Item>(), 100);
        dummyRoom1 = new Room("Room1", inv1, new Connections(), new ArrayList<Character>());
        dummyRoom2 = new Room("Room2", inv2, new Connections(), new ArrayList<Character>());
        
        // Add rooms (so that roomConnections get initialized)
        connections.addRoom(0, dummyRoom1);
        connections.addRoom(1, dummyRoom2);
    }
    
    @Test
    public void testSetAndGetConnection() {
        connections.setConnection("North", 1);
        assertEquals(Integer.valueOf(1), connections.getConnection("North"));
    }
    
    @Test
    public void testConnectRoomsAndGetNextRoom() {
        // Connect room 0 to room 1 in the "North" direction.
        connections.connectRooms(0, 1, "North");
        Integer nextRoom = connections.getNextRoom(0, "North");
        assertNotNull(nextRoom);
        assertEquals(Integer.valueOf(1), nextRoom);
    }
    
    
}
