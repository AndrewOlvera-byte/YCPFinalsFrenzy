package modelsTest;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import models.Inventory;
import models.Item;
import models.Weapon;

public class InventoryTest {
    private Inventory inventory;
    private Item item1;
    private Item item2;
    
    @Before
    public void setUp() {
        // Initialize an empty inventory with a max weight of 200.
        inventory = new Inventory(new ArrayList<Item>(), 200);
        
        // Create two items.
        String[] components1 = {"Iron"};
        item1 = new Weapon(50, 10, "Sword", components1, 25);
        
        String[] components2 = {"Wood"};
        item2 = new Weapon(30, 5, "Club", components2, 15);
    }
    
    @Test
    public void testListItems() {
        // Initially, the inventory is empty.
        assertEquals("Inventory is empty.", inventory.listItems());
        inventory.addItem(item1);
        inventory.addItem(item2);
        String expected = "Sword, Club";
        assertEquals(expected, inventory.listItems());
    }
    
    @Test
    public void testGetAndSetMaxCurrentWeight() {
        // Test getters.
        assertEquals(200, inventory.getMaxWeight());
        assertEquals(0, inventory.getCurrentWeight());
        
        // Change maxWeight and currentWeight.
        inventory.setMaxWeight(300);
        inventory.setCurrentWeight(50);
        
        assertEquals(300, inventory.getMaxWeight());
        assertEquals(50, inventory.getCurrentWeight());
    }
    
    @Test
    public void testGetInventoryList() {
        // Verify that getInventory returns a copy of the inventory list.
        inventory.addItem(item1);
        ArrayList<Item> invList = inventory.getInventory();
        assertEquals(1, invList.size());
        // Modify the returned list and ensure the internal inventory size remains unchanged.
        invList.clear();
        assertEquals(1, inventory.getSize());
    }
}
