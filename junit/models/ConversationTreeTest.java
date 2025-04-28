package models;
import models.ConversationTree;
import models.ConversationNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConversationTreeTest {
    private ConversationTree tree;
    private ConversationNode root;

    @BeforeEach
    void setUp() {
        root = new ConversationNode("Root Node");
        tree = new ConversationTree(root);
    }

    @Test
    void testTreeInitialization() {
        assertEquals("Root Node", tree.getCurrentNode().getMessage());
    }

    @Test
    void testTraverseNullInput() {
        tree.traverse(null); // Should safely do nothing
        assertEquals("Root Node", tree.getCurrentNode().getMessage());
    }
}
