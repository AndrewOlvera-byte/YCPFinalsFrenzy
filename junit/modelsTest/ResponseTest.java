package modelsTest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import models.Response;

public class ResponseTest {
    private Response response;
    
    @Before
    public void setUp() {
        // Create a Response object with sample strings.
        response = new Response("Room Inventory", "Player Inventory", "Characters", "Player Info", "Connections", "Message", "Error");
    }
    
    @Test
    public void testGetters() {
        // Verify that all getter methods return the expected strings.
        assertEquals("Room Inventory", response.getRoomInventory());
        assertEquals("Player Inventory", response.getPlayerInventory());
        assertEquals("Characters", response.getCharactersInRoom());
        assertEquals("Player Info", response.getPlayerInfo());
        assertEquals("Connections", response.getRoomConnections());
        assertEquals("Message", response.getMessage());
        assertEquals("Error", response.getError());
    }
    
    @Test
    public void testToJson() {
        // Check that the JSON output is not null and contains the expected keys.
        String json = response.toJson();
        assertNotNull(json);
        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));
        assertTrue(json.contains("\"roomInventory\""));
        assertTrue(json.contains("\"playerInventory\""));
        assertTrue(json.contains("\"charactersInRoom\""));
        assertTrue(json.contains("\"playerInfo\""));
        assertTrue(json.contains("\"roomConnections\""));
        assertTrue(json.contains("\"message\""));
        assertTrue(json.contains("\"error\""));
    }
}
