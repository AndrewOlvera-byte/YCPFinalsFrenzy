// File: junit/models/ConnectionsTest.java
package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionsTest {
    @Test
    public void testSetAndGet() {
        Connections con = new Connections();
        con.setConnection("North", 2);
        assertEquals(2, con.get("North"));
        assertEquals(-1, con.get("Invalid"));
    }
}
