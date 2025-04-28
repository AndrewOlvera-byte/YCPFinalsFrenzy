package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import GameEngine.GameEngine;
import models.DerbyDatabase;

public class InventoryManager {
    private GameEngine engine;

    public InventoryManager(GameEngine engine) {
        this.engine = engine;
    }

    /** Pick up an item from the current room into the player's inventory */
    public String pickupItem(int itemNum) {
        if (itemNum < 0) {
            return "\n<b>Pick up what?</b>";
        }
        
        Room room = engine.getRooms().get(engine.getCurrentRoomNum());
        Item item = room.getItem(itemNum);
        String itemName = item.getName();
        
        // Check database first to see if the player already has this item
        try (Connection conn = DerbyDatabase.getConnection()) {
            // Get the actual item_id from the database
            int databaseItemId = -1;
            
            try (PreparedStatement psGetItemId = conn.prepareStatement(
                     "SELECT item_id FROM ITEM WHERE name = ?")) {
                psGetItemId.setString(1, itemName);
                ResultSet rsItemId = psGetItemId.executeQuery();
                if (rsItemId.next()) {
                    databaseItemId = rsItemId.getInt("item_id");
                }
            }
            
            // If we couldn't find the item ID in the database, use the itemNum + 1 as fallback
            if (databaseItemId == -1) {
                databaseItemId = itemNum + 1;
            }
            
            try (PreparedStatement check = conn.prepareStatement(
                     "SELECT 1 FROM PLAYER_INVENTORY WHERE player_id = ? AND item_id = ?")) {
                check.setInt(1, 1);
                check.setInt(2, databaseItemId);
                if (check.executeQuery().next()) {
                    // Already in inventory, don't make any changes
                    return "<b>\nYou already have a " + itemName + ".</b>";
                }
            }
            
            // Only proceed with changes if the item is not already in inventory
            // In-memory move
            room.removeItem(itemNum);
            engine.getPlayer().addItem(item);

            // DB update: remove from ROOM_INVENTORY, add to PLAYER_INVENTORY
            // 1) DELETE from ROOM_INVENTORY - check if it exists first
            boolean itemExistsInRoom = false;
            try (PreparedStatement checkRoom = conn.prepareStatement(
                     "SELECT 1 FROM ROOM_INVENTORY WHERE room_id = ? AND item_id = ?")) {
                checkRoom.setInt(1, engine.getCurrentRoomNum() + 1);
                checkRoom.setInt(2, databaseItemId);
                itemExistsInRoom = checkRoom.executeQuery().next();
            }
            
            if (itemExistsInRoom) {
                try (PreparedStatement del = conn.prepareStatement(
                         "DELETE FROM ROOM_INVENTORY WHERE room_id = ? AND item_id = ?")) {
                    del.setInt(1, engine.getCurrentRoomNum() + 1);
                    del.setInt(2, databaseItemId);
                    del.executeUpdate();
                }
            }
            
            // 2) INSERT into PLAYER_INVENTORY
            try (PreparedStatement ins = conn.prepareStatement(
                     "INSERT INTO PLAYER_INVENTORY(player_id, item_id) VALUES(1, ?)")) {
                ins.setInt(1, databaseItemId);
                ins.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Inventory DB update failed", e);
        }

        return "<b>\n" + itemName + " was picked up.</b>";
    }

    /** Drop an item from the player into the current room */
    public String dropItem(int itemNum) {
        if (itemNum < 0 || itemNum >= engine.getPlayer().getInventorySize()) {
            return "\n<b>Invalid item selection.</b>";
        }
        // In-memory move
        Item item = engine.getPlayer().getItem(itemNum);
        String itemName = item.getName();
        engine.getPlayer().removeItem(itemNum);
        Room room = engine.getRooms().get(engine.getCurrentRoomNum());
        room.addItem(item);

        // DB update: remove from PLAYER_INVENTORY, add to ROOM_INVENTORY
        try (Connection conn = DerbyDatabase.getConnection()) {
            // Get the actual item_id from the database
            int databaseItemId = -1;
            
            try (PreparedStatement psGetItemId = conn.prepareStatement(
                     "SELECT item_id FROM ITEM WHERE name = ?")) {
                psGetItemId.setString(1, itemName);
                ResultSet rsItemId = psGetItemId.executeQuery();
                if (rsItemId.next()) {
                    databaseItemId = rsItemId.getInt("item_id");
                }
            }
            
            // If we couldn't find the item ID in the database, use the itemNum + 1 as fallback
            if (databaseItemId == -1) {
                databaseItemId = itemNum + 1;
            }
            
            // 1) DELETE from PLAYER_INVENTORY
            try (PreparedStatement del = conn.prepareStatement(
                     "DELETE FROM PLAYER_INVENTORY WHERE player_id = 1 AND item_id = ?"
                 )) {
                del.setInt(1, databaseItemId);
                del.executeUpdate();
            }
            
            // 2) Check if the item already exists in the room's inventory
            boolean itemExistsInRoom = false;
            try (PreparedStatement checkRoom = conn.prepareStatement(
                     "SELECT 1 FROM ROOM_INVENTORY WHERE room_id = ? AND item_id = ?")) {
                checkRoom.setInt(1, engine.getCurrentRoomNum() + 1);
                checkRoom.setInt(2, databaseItemId);
                itemExistsInRoom = checkRoom.executeQuery().next();
            }
            
            // Only insert if it doesn't already exist
            if (!itemExistsInRoom) {
                try (PreparedStatement ins = conn.prepareStatement(
                         "INSERT INTO ROOM_INVENTORY(room_id, item_id) VALUES(?, ?)"
                     )) {
                    ins.setInt(1, engine.getCurrentRoomNum() + 1);
                    ins.setInt(2, databaseItemId);
                    ins.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Inventory DB update failed", e);
        }

        return "\n<b>" + itemName + " was dropped.</b>";
    }

    /** Find the index of an item in the current room by name */
    public int RoomItemNameToID(String name) {
        Room room = engine.getRooms().get(engine.getCurrentRoomNum());
        for (int i = 0; i < room.getInventorySize(); i++) {
            if (name.equalsIgnoreCase(room.getItemName(i))) {
                return i;
            }
        }
        return -1;
    }

    /** Find the index of an item in the player's inventory by name */
    public int CharItemNameToID(String name) {
        for (int i = 0; i < engine.getPlayer().getInventorySize(); i++) {
            if (name.equalsIgnoreCase(engine.getPlayer().getItemName(i))) {
                return i;
            }
        }
        return -1;
    }

    /** Examine the player's item (long then short description) */
    public String examineItemName(int itemNum) {
        if (itemNum < 0 || itemNum >= engine.getPlayer().getInventorySize()) {
            return "\n<b>Invalid item selection.</b>";
        }
        Item item = engine.getPlayer().getItem(itemNum);
        return "\n" + item.getDescription();
    }

    /** Use a potion or utility item from the player's inventory */
    public String usePotion(int itemNum) {
        if (itemNum < 0 || itemNum >= engine.getPlayer().getInventorySize()) {
            return "\n<b>Invalid item selection.</b>";
        }
        Item raw = engine.getPlayer().getItem(itemNum);
        if (!(raw instanceof Utility)) {
            return "<b>\nYou can't drink or apply that!</b>";
        }
        Utility potion = (Utility) raw;
        double multi = potion.getDamageMulti();
        int heal    = potion.getHealing();

        // In-memory apply
        engine.getPlayer().setHp(engine.getPlayer().getHp() + heal);
        if (multi != 0) {
            engine.getPlayer().setdamageMulti(multi + engine.getPlayer().getdamageMulti());
        }
        engine.getPlayer().removeItem(itemNum);

        // DB update: remove from PLAYER_INVENTORY
        try (Connection conn = DerbyDatabase.getConnection();
             PreparedStatement del = conn.prepareStatement(
                 "DELETE FROM PLAYER_INVENTORY WHERE player_id = 1 AND item_id = ?"
             )) {
            del.setInt(1, itemNum + 1);
            del.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update DB after using potion", e);
        }

        return "\n<b>" + raw.getName() + " was applied.</b>";
    }
    
    public String select() {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = DerbyDatabase.getConnection()) {
            // Retrieve the database name from the connection URL
            String dbURL = conn.getMetaData().getURL(); // Get the connection URL
            sb.append("Database URL: ").append(dbURL).append("\n");
            // Optionally, extract the database name from the URL if it follows the format jdbc:derby:/path/to/database/dbname
            int dbNameStartIndex = dbURL.lastIndexOf("/") + 1;
            String dbName = dbURL.substring(dbNameStartIndex);
            sb.append("Database Name: ").append(dbName).append("\n");
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving database name", e);
        }
        return sb.toString();
    }


}
