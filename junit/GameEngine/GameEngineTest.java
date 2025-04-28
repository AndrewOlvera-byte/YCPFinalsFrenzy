package GameEngine;
import GameEngine.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {
    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        engine.start();
    }

 
}
