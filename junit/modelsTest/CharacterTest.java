package modelsTest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import models.Character;
import models.Inventory;
import models.Item;
import models.Weapon;

import java.util.ArrayList;

public class CharacterTest {
    private Character character;
    private Weapon weapon;
    
    @Before
    public void setUp() {
        // Create a weapon for the character's inventory.
        String[] components = {"Steel"};
        weapon = new Weapon(50, 5, "Sword", components, 30);
        ArrayList<Item> items = new ArrayList<>();
        items.add(weapon);
        Inventory inventory = new Inventory(items, 100);
        
        character = new Character("TestChar", 100, inventory);
    }
    
    @Test
    public void testGetNameAndHp() {
        assertEquals("TestChar", character.getName());
        assertEquals(100, character.getHp());
        
        character.setHp(80);
        assertEquals(80, character.getHp());
    }
    
    @Test
    public void testGetAttackDmg() {
        // Using the weapon at index 0 in the inventory.
        int dmg = character.getAttackDmg(0);
        assertEquals(30, dmg);
    }
    
    @Test
    public void testJustAttackedFlag() {
        // Initially false.
        assertFalse(character.getJustAttacked());
        character.setJustAttacked(true);
        assertTrue(character.getJustAttacked());
    }
    
    @Test
    public void testDropAllItems() {
        // Ensure there is one item.
        assertEquals(1, character.getInventorySize());
        ArrayList<Item> dropped = character.dropAllItems();
        // After dropping, inventory should be empty.
        assertEquals(0, character.getInventorySize());
        // Dropped items list should contain the previously held weapon.
        assertEquals(1, dropped.size());
        assertEquals("Sword", dropped.get(0).getName());
    }
    
    @Test
    public void testAddAndRemoveItem() {
        // Inventory already has one item.
        assertEquals(1, character.getInventorySize());
        
        // Create another weapon.
        String[] components = {"Iron"};
        Weapon newWeapon = new Weapon(40, 4, "Dagger", components, 20);
        character.addItem(newWeapon);
        assertEquals(2, character.getInventorySize());
        assertEquals("Dagger", character.getItem(1).getName());
        
        // Remove the first item.
        character.removeItem(0);
        assertEquals(1, character.getInventorySize());
        // The remaining item should be "Dagger".
        assertEquals("Dagger", character.getItem(0).getName());
    }
}
