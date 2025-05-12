// File: junit/models/WeaponTest.java
package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class WeaponTest {
    private Weapon weapon;

    @BeforeEach
    void setUp() {
        weapon = new Weapon(5, 1, "Sword", new String[]{"Blade", "Handle"}, 80, "A sharp sword", "Sword");
    }

    @Test
    void testWeaponInitialization() {
        assertEquals("Sword", weapon.getName());
        assertEquals(5, weapon.getValue());
        assertEquals(1, weapon.getWeight());
        assertEquals(80, weapon.getAttackDmg());
    }

    @Test
    void testComponents() {
        List<String> components = weapon.getComponents();
        assertEquals(2, components.size());
        assertTrue(components.contains("Blade"));
        assertTrue(components.contains("Handle"));
    }

    @Test
    void testAddComponent() {
        weapon.addComponent("Gem");
        List<String> components = weapon.getComponents();
        assertEquals(3, components.size());
        assertTrue(components.contains("Gem"));
    }

    @Test
    void testDescription() {
        assertEquals("A sharp sword", weapon.getDescription());
        weapon.getDescription(); // First call sets examined to true
        assertEquals("Sword", weapon.getDescription()); // Second call returns short description
    }
}
