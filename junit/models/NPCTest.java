package models;
import models.Inventory;
import models.NPC;
import models.ConversationTree;
import models.ConversationNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

class NPCTest {
    private NPC npc;

    @BeforeEach
    void setUp() {
        npc = new NPC("Curly", 80, false, new String[]{"Hello"}, 10, new Inventory(new ArrayList<>(), 50), "Long desc", "Short desc");
    }

    @Test
    void testNPCInitialization() {
        assertEquals("Curly", npc.getName());
        assertEquals(80, npc.getHp());
        assertFalse(npc.getAggresion());
    }

    @Test
    void testSetAggression() {
        npc.setAgression(true);
        assertTrue(npc.getAggresion());
    }

    @Test
    void testConversationTree() {
        ConversationNode root = new ConversationNode("Root Message");
        ConversationTree tree = new ConversationTree(root);
        npc.addConversationTree(tree);

        assertEquals("Root Message", npc.getMessage());
    }
}
