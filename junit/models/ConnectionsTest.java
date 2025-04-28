package models;
import models.Connections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionsTest {
    private Connections connections;

    @BeforeEach
    void setUp() {
        connections = new Connections();
    }

    @Test
    void testSetAndGetConnection() {
        connections.setConnection("North", 1);
        assertEquals(1, connections.getConnection("North"));
    }

    @Test
    void testMissingConnection() {
        assertNull(connections.getConnection("South"));
    }
}
