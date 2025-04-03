
package models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WeaponTest {
    @Test
    public void testWeaponProperties() {
        Weapon weapon = new Weapon(25, 4, "Sword", null, 80, "Long sword", "Sword.");

        assertEquals(80, weapon.getDamage());
        assertEquals("Sword", weapon.getName());
        assertEquals(25, weapon.getValue());
    }
}
