
package models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConversationTreeTest {
    @Test
    public void testConversationNavigation() {
        ConversationNode root = new ConversationNode("Hello!");
        ConversationNode node1 = new ConversationNode("Hi there!");
        root.addResponse("1.Hi", node1);

        ConversationTree tree = new ConversationTree(root);
        assertEquals("Hello!", tree.getCurrentNodeMessage());

        tree.traverse("1.Hi");
        assertEquals("Hi there!", tree.getCurrentNodeMessage());
    }
}
