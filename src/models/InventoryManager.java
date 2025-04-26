package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import GameEngine.GameEngine;
import models.DerpyDatabase;

public class InventoryManager {
    private GameEngine engine;

    public InventoryManager(GameEngine engine) {
        this.engine = engine;
    }

    /** Pick up an item from the current room into the player’s inventory */
    public String pickupItem(int itemNum) {
        if (itemNum < 0) {
            return "\n<b>Pick up what?</b>";
        }
        // In-memory move
        Room room = engine.getRooms().get(engine.getCurrentRoomNum());
        Item item = room.getItem(itemNum);
        String itemName = item.getName();
        room.removeItem(itemNum);
        engine.getPlayer().addItem(item);

        // DB update: remove from ROOM_INVENTORY, add to PLAYER_INVENTORY
        try (Connection conn = DerpyDatabase.getConnection()) {
            // 1) DELETE from ROOM_INVENTORY
            try (PreparedStatement del = conn.prepareStatement(
                     "DELETE FROM ROOM_INVENTORY WHERE room_id = ? AND item_id = ?"
                 )) {
                del.setInt(1, engine.getCurrentRoomNum() + 1);
                del.setInt(2, itemNum + 1);
                del.executeUpdate();
            }
            // 2) INSERT into PLAYER_INVENTORY
         // --- before the INSERT ---
            try (PreparedStatement check = conn.prepareStatement(
                     "SELECT 1 FROM PLAYER_INVENTORY WHERE player_id = ? AND item_id = ?")) {
                check.setInt(1, 1);
                check.setInt(2, itemNum + 1);
                if (check.executeQuery().next()) {
                    // Already in inventory, skip the INSERT
                    return "<b>\nYou already have a " + itemName + ".</b>";
                }
            }

            // Now do the INSERT safely:
            try (PreparedStatement ins = conn.prepareStatement(
                     "INSERT INTO PLAYER_INVENTORY(player_id, item_id) VALUES(1, ?)")) {
                ins.setInt(1, itemNum + 1);
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
        try (Connection conn = DerpyDatabase.getConnection()) {
            // 1) DELETE from PLAYER_INVENTORY
            try (PreparedStatement del = conn.prepareStatement(
                     "DELETE FROM PLAYER_INVENTORY WHERE player_id = 1 AND item_id = ?"
                 )) {
                del.setInt(1, itemNum + 1);
                del.executeUpdate();
            }
            // 2) INSERT into ROOM_INVENTORY
            try (PreparedStatement ins = conn.prepareStatement(
                     "INSERT INTO ROOM_INVENTORY(room_id, item_id) VALUES(?, ?)"
                 )) {
                ins.setInt(1, engine.getCurrentRoomNum() + 1);
                ins.setInt(2, itemNum + 1);
                ins.executeUpdate();
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

    /** Find the index of an item in the player’s inventory by name */
    public int CharItemNameToID(String name) {
        for (int i = 0; i < engine.getPlayer().getInventorySize(); i++) {
            if (name.equalsIgnoreCase(engine.getPlayer().getItemName(i))) {
                return i;
            }
        }
        return -1;
    }

    /** Examine the player’s item (long then short description) */
    public String examineItemName(int itemNum) {
        if (itemNum < 0 || itemNum >= engine.getPlayer().getInventorySize()) {
            return "\n<b>Invalid item selection.</b>";
        }
        Item item = engine.getPlayer().getItem(itemNum);
        return "\n" + item.getDescription();
    }

    /** Use a potion or utility item from the player’s inventory */
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
        try (Connection conn = DerpyDatabase.getConnection();
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
}
