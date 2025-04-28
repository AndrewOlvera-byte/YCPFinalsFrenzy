// File: junit/models/WeaponTest.java
package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WeaponTest {
    @Test
    public void testWeaponDamage() {
        Weapon w = new Weapon(5,1,"Sword",null,80,"L","S");
        assertEquals(80, w.getAttackDmg());
    }
}
