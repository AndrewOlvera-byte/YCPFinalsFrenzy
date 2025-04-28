package models;
import models.ConversationManager;
import models.ConversationTree;
import GameEngine.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConversationManagerTest {
    private ConversationManager conversationManager;
    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        engine.start();
        conversationManager = new ConversationManager(engine);
    }

   
}
