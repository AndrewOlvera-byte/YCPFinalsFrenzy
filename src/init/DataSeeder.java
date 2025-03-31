package init;

import GameEngine.GameEngine;
import models.*;
import orm.OrmManager;
import orm.TableCreator;
import utils.CSVLoader;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DataSeeder {

    public static void createTables(OrmManager orm) {
        try {
            TableCreator.createTable(orm.getConnection(), Room.class);
            TableCreator.createTable(orm.getConnection(), models.Character.class);
            TableCreator.createTable(orm.getConnection(), Item.class);
            TableCreator.createTable(orm.getConnection(), Inventory.class);
            TableCreator.createTable(orm.getConnection(), Connections.class);
            TableCreator.createTable(orm.getConnection(), RoomCharacterLink.class);
            TableCreator.createTable(orm.getConnection(), InventoryItemLink.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dropTables(OrmManager orm) {
        String[] tables = {
            "inventory_items", "room_characters", "connections",
            "inventories", "items", "characters", "rooms"
        };
        try (Statement stmt = orm.getConnection().createStatement()) {
            for (String table : tables) {
                try {
                    stmt.executeUpdate("DROP TABLE " + table);
                    System.out.println("Dropped table: " + table);
                } catch (SQLException e) {
                    if (!"42Y55".equals(e.getSQLState())) {
                        throw e;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void seedAll(OrmManager orm, ArrayList<Room> rooms, GameEngine gameEngine) throws Exception {
        createTables(orm);

        if (!orm.findAll(Room.class).isEmpty()) {
            System.out.println("Data already exists. Skipping seeding.");
            rooms.addAll(orm.findAll(Room.class));
            return;
        }

        seedFromCSV(orm, rooms, gameEngine);
    }

    private static void seedFromCSV(OrmManager orm, ArrayList<Room> rooms, GameEngine gameEngine) throws Exception {
        CSVLoader.loadAndSaveCSV("rooms.csv", Room.class, orm, rooms);
        CSVLoader.loadAndSaveCSV("characters.csv", models.Character.class, orm, null);
        CSVLoader.loadAndSaveCSV("players.csv", Player.class, orm, null);
        CSVLoader.loadAndSaveCSV("items.csv", Item.class, orm, null);
        CSVLoader.loadAndSaveCSV("weapons.csv", Weapon.class, orm, null);
        CSVLoader.loadAndSaveCSV("inventories.csv", Inventory.class, orm, null);
        CSVLoader.loadAndSaveCSV("connections.csv", Connections.class, orm, null);
        CSVLoader.loadAndSaveCSV("room_character_links.csv", RoomCharacterLink.class, orm, null);
        CSVLoader.loadAndSaveCSV("inventory_item_links.csv", InventoryItemLink.class, orm, null);

        // Load player into game engine
        List<Player> players = orm.findAll(Player.class);
        if (!players.isEmpty()) {
            gameEngine.setPlayer(players.get(0));
        } else {
            System.err.println("No player loaded from CSV.");
        }
    }
}
