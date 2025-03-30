package init;

import models.*;
import models.Character;
import orm.OrmManager;

import java.util.ArrayList;
import java.util.List;

public class DataSeeder {

    public static void seed(OrmManager orm) {
        try {
            // Seed Player and Inventory if not already present
            if (orm.find(Player.class, 1) == null) {
                Inventory inv = orm.find(Inventory.class, 1);
                if (inv == null) {
                    inv = new Inventory();
                    orm.save(inv);
                    System.out.println("🌱 Inventory seeded.");
                }

                Player player = new Player();
                player.setName("Hero");
                player.setHp(100);
                player.setInventory(inv);
                player.setCharDescription("This is you!", "You");
                orm.save(player);
                System.out.println("🌱 Player seeded.");
            } else {
                System.out.println("Player already exists. Skipping player seeding.");
            }

            // Seed Weapon if not already present
            boolean weaponExists = orm.findAll(Weapon.class).stream()
                .anyMatch(w -> "Dagger".equalsIgnoreCase(w.getName()));
            if (!weaponExists) {
                Weapon weapon = new Weapon();
                weapon.setName("Dagger");
                weapon.setAttackDmg(40);
                weapon.setDescription("A basic dagger", "dagger");
                weapon.setComponents(new String[] {});
                orm.save(weapon);
                System.out.println("🌱 Weapon seeded.");
            } else {
                System.out.println("Weapon 'Dagger' already exists. Skipping weapon seeding.");
            }

            // Seed Room if none exist
            List<Room> existingRooms = orm.findAll(Room.class);
            if (existingRooms.isEmpty()) {
                Room startingRoom = new Room("Starting Room", new Inventory(), new Connections(), new ArrayList<Character>(), "You are in the starting room of the game. It's bright and welcoming.", "Starting Room");
                orm.save(startingRoom);
                System.out.println("🌱 Room seeded: " + startingRoom.getRoomName());
            } else {
                System.out.println("Rooms already exist. Skipping room seeding.");
            }
        } catch (Exception e) {
            System.err.println("Seeding failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
