package modelsTest;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import models.Player;
import models.Inventory;
import models.Item;
import models.Weapon;

public class PlayerTest {
    private Player player;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    
    @Before
    public void setUp() {
        // Create a simple weapon for the player's inventory.
        String[] components = {"Iron"};
        Weapon weapon = new Weapon(50, 5, "Dagger", components, 20, "Trusty dagger in the back pocket");
        ArrayList<Item> items = new ArrayList<>();
        items.add(weapon);
        Inventory inventory = new Inventory(items, 100);
        
        // Create a Player with 3 skill points.
        player = new Player("Hero", 100, 3, inventory);
        
        // Redirect System.out to capture output for testing move() and useSkillPoints().
        System.setOut(new PrintStream(outContent));
    }
    
    @After
    public void tearDown() {
        // Restore System.out after each test.
        System.setOut(originalOut);
    }
    
    @Test
    public void testMoveOutput() {
        player.move("North");
        String output = outContent.toString().trim();
        assertTrue(output.contains("Hero moves North to a new location."));
    }
    
    @Test
    public void testUseSkillPoints() throws NoSuchFieldException, IllegalAccessException {
        // Use reflection to access the private skillPoints field.
        Field field = Player.class.getDeclaredField("skillPoints");
        field.setAccessible(true);
        int initialSkillPoints = field.getInt(player);
        
        player.useSkillPoints();
        // After using one skill point, expect one less.
        int afterUse = field.getInt(player);
        assertEquals(initialSkillPoints - 1, afterUse);
        
        // Now set skillPoints to 0 and verify the appropriate message is printed.
        field.setInt(player, 0);
        outContent.reset();
        player.useSkillPoints();
        String output = outContent.toString().trim();
        assertTrue(output.contains("has no skill points left"));
    }
    
    @Test
    public void testInheritedProperties() {
        // Verify that Player inherits properties from Character.
        assertEquals("Hero", player.getName());
        assertEquals(100, player.getHp());
    }
}
