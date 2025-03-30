package init;

import orm.OrmManager;
import orm.TableCreator;
import models.*;
import models.Character;

public class DatabaseInitializer {  
    public static void initialize(OrmManager orm) {
        try {
            try {
                TableCreator.createTable(orm.getConnection(), Player.class);
            } catch (Exception e) {
                if (!e.getMessage().contains("already exists")) throw e;
                System.out.println("Table already exists, skipping: " + e.getMessage());
            }

            try {
                TableCreator.createTable(orm.getConnection(), Item.class);
            } catch (Exception e) {
                if (!e.getMessage().contains("already exists")) throw e;
                System.out.println("Table already exists, skipping: " + e.getMessage());
            }

            try {
                TableCreator.createTable(orm.getConnection(), Weapon.class);
            } catch (Exception e) {
                if (!e.getMessage().contains("already exists")) throw e;
                System.out.println("Table already exists, skipping: " + e.getMessage());
            }

            try {
                TableCreator.createTable(orm.getConnection(), Inventory.class);
            } catch (Exception e) {
                if (!e.getMessage().contains("already exists")) throw e;
                System.out.println("Table already exists, skipping: " + e.getMessage());
            }

            try {
                TableCreator.createTable(orm.getConnection(), Room.class);
            } catch (Exception e) {
                if (!e.getMessage().contains("already exists")) throw e;
                System.out.println("Table already exists, skipping: " + e.getMessage());
            }

            try {
                TableCreator.createTable(orm.getConnection(), Character.class);
            } catch (Exception e) {
                if (!e.getMessage().contains("already exists")) throw e;
                System.out.println("Table already exists, skipping: " + e.getMessage());
            }

            try {
                TableCreator.createTable(orm.getConnection(), NPC.class);
            } catch (Exception e) {
                if (!e.getMessage().contains("already exists")) throw e;
                System.out.println("Table already exists, skipping: " + e.getMessage());
            }

            try {
                TableCreator.createTable(orm.getConnection(), Connections.class);
            } catch (Exception e) {
                if (!e.getMessage().contains("already exists")) throw e;
                System.out.println("Table already exists, skipping: " + e.getMessage());
            }

            // Add these if using them:
            try {
                TableCreator.createTable(orm.getConnection(), RoomCharacterLink.class);
            } catch (Exception e) {
                if (!e.getMessage().contains("already exists")) throw e;
                System.out.println("Table already exists, skipping: " + e.getMessage());
            }

            try {
                TableCreator.createTable(orm.getConnection(), InventoryItemLink.class);
            } catch (Exception e) {
                if (!e.getMessage().contains("already exists")) throw e;
                System.out.println("Table already exists, skipping: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to initialize tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
