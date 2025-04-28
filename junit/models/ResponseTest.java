// File: junit/models/ResponseTest.java
package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResponseTest {
    @Test
    public void testJsonEscaping() {
        Response r = new Response("a\"b","p","c","d","e","f","g","h","1","i","j");
        r.setGameOver(true);
        String j = r.toJson();
        assertTrue(j.contains("\\\""));
        assertTrue(j.contains("\"gameOver\":true"));
    }
}
