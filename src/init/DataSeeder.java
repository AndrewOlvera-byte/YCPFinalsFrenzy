
package init;

import models.*;
import orm.OrmManager;

public class DataSeeder {

    public static void seed(OrmManager orm) {
        try {
            if (orm.find(Player.class, 1) == null) {
            	Inventory inv = orm.find(Inventory.class, 1);
            	if (inv == null) {
            	    inv = new Inventory();
            	    orm.save(inv);
            	}

                Player player = new Player();
                player.setName("Hero");
                player.setHp(100);
                player.setInventory(inv);
                player.setCharDescription("This is you!","You");
                orm.save(player);
                System.out.println("🌱 Seeding database...");
            } else {
                System.out.println("Player already exists. Skipping.");
            }

            // Seed Weapon without setting ID (auto-increment)
            boolean weaponExists = orm.findAll(Weapon.class).stream()
                .anyMatch(w -> "Dagger".equalsIgnoreCase(w.getName()));
            if (!weaponExists) {
                Weapon weapon = new Weapon();
                weapon.setName("Dagger");
                weapon.setAttackDmg(40);
                weapon.setDescription("A basic dagger","dagger");
                weapon.setComponents(new String[] {});
                orm.save(weapon);
            } else {
                System.out.println("Weapon 'Dagger' already exists. Skipping.");
            }
            
            Room startingRoom = new Room();
            startingRoom.setRoomName("Starting Room");
            startingRoom.setDescription("This is the starting room of the game.", "starting room");
            orm.save(startingRoom);

        } catch (Exception e) {
            System.err.println("Seeding failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
