package models;
import models.Response;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void testResponseSettersAndGetters() {
        Response response = new Response();
        response.setRoomInventory("Room Items");
        response.setPlayerInventory("Player Items");
        response.setMessage("Hello");
        response.setError("None");
        response.setGameOver(true);
        response.setGameOverImage("/images/GameOver.png");

        assertEquals("Room Items", response.getRoomInventory());
        assertEquals("Player Items", response.getPlayerInventory());
        assertEquals("Hello", response.getMessage());
        assertTrue(response.isGameOver());
        assertEquals("/images/GameOver.png", response.getGameOverImage());
    }

    @Test
    void testJsonConversion() {
        Response response = new Response();
        String json = response.toJson();
        assertTrue(json.contains("\"gameOver\":false"));
    }
}
