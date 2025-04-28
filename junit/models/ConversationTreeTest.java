// File: junit/models/ConversationTreeTest.java
package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConversationTreeTest {
    @Test
    public void testNavigation() {
        ConversationNode root = new ConversationNode("Hi");
        ConversationNode n1 = new ConversationNode("Bye");
        root.addResponse("1", n1);
        ConversationTree t = new ConversationTree(root);
        assertEquals("Hi", t.getCurrentNodeMessage());
        t.traverse("1");
        assertEquals("Bye", t.getCurrentNodeMessage());
    }
}
