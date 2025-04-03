
package GameEngine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameEngineTest {
    private GameEngine engine;

    @BeforeEach
    public void setUp() {
        engine = new GameEngine();
        engine.start();
    }

    @Test
    public void testInitialRoomName() {
        String roomName = engine.getCurrentRoomName();
        assertNotNull(roomName);
    }

    @Test
    public void testPlayerInfoNotEmpty() {
        String info = engine.getPlayerInfo();
        assertTrue(info.contains("Name:"));
    }

    @Test
    public void testAttackNoTarget() {
        String result = engine.playerAttackChar(-1, -1);
        assertTrue(result.contains("Attack who with what?"));
    }
}
