package modelsTest;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import models.NPC;
import models.Player;
import models.Inventory;
import models.Item;
import models.Weapon;

public class NPCTest {
    private NPC npc;
    private Player player;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    
    @Before
    public void setUp() {
        // Prepare dialogue for the NPC.
        String[] dialogue = {"Hello", "Adventurer!"};
        // Create a simple weapon for the NPC's inventory.
        String[] components = {"Steel"};
        Weapon npcWeapon = new Weapon(40, 4, "Club", components, 15);
        ArrayList<Item> npcItems = new ArrayList<>();
        npcItems.add(npcWeapon);
        Inventory npcInventory = new Inventory(npcItems, 50);
        
        // Create an aggressive NPC with 15 damage.
        npc = new NPC("Goblin", 80, true, dialogue, 15, npcInventory);
        
        // Create a simple Player.
        String[] playerComponents = {"Iron"};
        Weapon playerWeapon = new Weapon(50, 5, "Sword", playerComponents, 20);
        ArrayList<Item> playerItems = new ArrayList<>();
        playerItems.add(playerWeapon);
        Inventory playerInventory = new Inventory(playerItems, 100);
        player = new Player("Hero", 100, 3, playerInventory);
        
        // Redirect System.out to capture output.
        System.setOut(new PrintStream(outContent));
    }
    
    @After
    public void tearDown() {
        System.setOut(originalOut);
    }
    
    @Test
    public void testAttackPlayerWhenAggressive() {
        int initialHp = player.getHp();
        npc.attackPlayer(player);
        // Expect player's hp to reduce by npc's damage (15).
        assertEquals(initialHp - 15, player.getHp());
        
        String output = outContent.toString().trim();
        assertTrue(output.contains("Goblin attacks Hero for 15 damage"));
    }
    
    @Test
    public void testAttackPlayerWhenNotAggressive() {
        // Turn off aggression.
        npc.setAgression(false);
        int initialHp = player.getHp();
        outContent.reset();
        npc.attackPlayer(player);
        // Player's hp should remain unchanged.
        assertEquals(initialHp, player.getHp());
        String output = outContent.toString().trim();
        // Since aggression is false, no attack message should be printed.
        assertFalse(output.contains("attacks"));
    }
    
    @Test
    public void testGetAndSetAggression() {
        // Initially, aggression should be true.
        assertTrue(npc.getAggresion());
        npc.setAgression(false);
        assertFalse(npc.getAggresion());
    }
}
