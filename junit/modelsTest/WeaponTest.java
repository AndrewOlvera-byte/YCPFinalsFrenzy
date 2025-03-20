package modelsTest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import models.Weapon;

public class WeaponTest {
    private Weapon weapon;

    @Before
    public void setUp() {
        // Create a weapon with specific attributes.
        String[] components = {"Steel", "Wood"};
        weapon = new Weapon(100, 10, "Sword", components, 50);
    }

    @Test
    public void testGetAttackDmg() {
        // Verify that getAttackDmg() returns the correct damage.
        assertEquals(50, weapon.getAttackDmg());
    }
    
    @Test
    public void testInheritedProperties() {
        // Check inherited properties from Item.
        assertEquals("Sword", weapon.getName());
        assertEquals(10, weapon.getWeight());
        assertEquals(100, weapon.getValue());
        assertArrayEquals(new String[]{"Steel", "Wood"}, weapon.getComponents());
    }
}
