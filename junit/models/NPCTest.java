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
    private Inventory npcInventory;
    private ConversationTree conversationTree;

    @BeforeEach
    void setUp() {
        npcInventory = new Inventory(new ArrayList<>(), 100);
        npc = new NPC("TestNPC", 50, true, new String[]{"Hello", "Goodbye"},
                     10, npcInventory, "A test NPC", "Short desc");
    }

    @Test
    void testInitialState() {
        assertEquals("TestNPC", npc.getName());
        assertEquals(50, npc.getHp());
        assertTrue(npc.getAggresion());
        assertEquals("A test NPC", npc.getCharDescription());
    }

    @Test
    void testInventoryManagement() {
        // Add weapon to NPC
        Weapon sword = new Weapon(1, 1, "NPCSword", null, 20, "NPC sword", "A sword");
        npc.addItem(sword);
        
        assertEquals(1, npc.getInventorySize());
        assertEquals("NPCSword", npc.getItemName(0));
        assertEquals(20, npc.getAttackDmg(0)); // Test attack damage with weapon
        
        // Test dropping items
        ArrayList<Item> droppedItems = npc.dropAllItems();
        assertEquals(1, droppedItems.size());
        assertEquals("NPCSword", droppedItems.get(0).getName());
        assertEquals(0, npc.getInventorySize());
    }

    @Test
    void testHealthManagement() {
        assertEquals(50, npc.getHp());
        
        npc.setHp(30);
        assertEquals(30, npc.getHp());
        
        npc.setHp(0);
        assertEquals(0, npc.getHp());
    }

    @Test
    void testConversation() {
        // Test basic dialogue
        String message = npc.getMessage();
        assertEquals("TestNPC says: Hello Goodbye", message);
        
        // Test conversation tree
        ConversationNode root = new ConversationNode("Welcome!");
        ConversationNode response1 = new ConversationNode("Hello there!");
        ConversationNode response2 = new ConversationNode("Goodbye!");
        
        root.addResponse("Hello", response1);
        root.addResponse("Bye", response2);
        
        conversationTree = new ConversationTree(root);
        npc.addConversationTree(conversationTree);
        
        // Test conversation navigation
        assertEquals("Welcome!", npc.getMessage());
        
        npc.interact("Hello");
        assertEquals("Hello there!", npc.getMessage());
    }

    @Test
    void testAggression() {
        assertTrue(npc.getAggresion());
        
        // Create non-aggressive NPC
        NPC peacefulNPC = new NPC("Peaceful", 50, false, new String[]{"Hello"},
                                 10, new Inventory(new ArrayList<>(), 100),
                                 "Peaceful NPC", "Short desc");
        assertFalse(peacefulNPC.getAggresion());
    }

    @Test
    void testCombatBehavior() {
        // Add weapon
        Weapon sword = new Weapon(1, 1, "NPCSword", null, 20, "NPC sword", "A sword");
        npc.addItem(sword);
        
        // Test attack damage calculation
        assertEquals(20, npc.getAttackDmg(0)); // Base weapon damage
        
        // Test just attacked flag
        assertFalse(npc.getJustAttacked());
        npc.setJustAttacked(true);
        assertTrue(npc.getJustAttacked());
    }

    @Test
    void testItemDropping() {
        // Add multiple items
        Item sword = new Item(1, 1, "NPCSword", null, "A sword", "Short desc");
        Item shield = new Item(2, 2, "NPCShield", null, "A shield", "Short desc");
        Item potion = new Item(3, 3, "NPCPotion", null, "A potion", "Short desc");
        
        npc.addItem(sword);
        npc.addItem(shield);
        npc.addItem(potion);
        
        // Test dropping all items
        ArrayList<Item> droppedItems = npc.dropAllItems();
        
        assertEquals(3, droppedItems.size());
        assertEquals(0, npc.getInventorySize());
        assertTrue(droppedItems.stream().anyMatch(item -> item.getName().equals("NPCSword")));
        assertTrue(droppedItems.stream().anyMatch(item -> item.getName().equals("NPCShield")));
        assertTrue(droppedItems.stream().anyMatch(item -> item.getName().equals("NPCPotion")));
    }


}
