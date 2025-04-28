// File: junit/models/ConversationManagerTest.java
package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConversationManagerTest {
    @Test
    public void testSetGet() {
        ConversationManager m = new ConversationManager(null);
        ConversationTree t = new ConversationTree(new ConversationNode("H"));
        m.setConversations(Map.of("X", t));
        assertSame(t, m.getConversation("X"));
        assertNull(m.getConversation("Y"));
    }
}
