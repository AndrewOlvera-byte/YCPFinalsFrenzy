package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import GameEngine.GameEngine;
import models.DerbyDatabase;
import models.Armor;
import models.ArmorSlot;
import models.Item;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


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
        if (itemNum >= room.getInventorySize()) {
            return "\n<b>Invalid item selection.</b>";
        }
        
        Item item = room.getItem(itemNum);
        String itemName = item.getName();
        
        // Get player ID (supporting multiplayer)
        Player player = engine.getPlayer();
        int playerId = player.getId();
        
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
            
            // Check in new player_items table first
            boolean alreadyInInventory = false;
            try (PreparedStatement check = conn.prepareStatement(
                     "SELECT 1 FROM player_items WHERE player_id = ? AND item_id = ?")) {
                check.setInt(1, playerId);
                check.setInt(2, databaseItemId);
                alreadyInInventory = check.executeQuery().next();
            }
            
            // If not found, check legacy PLAYER_INVENTORY table
            if (!alreadyInInventory) {
                try (PreparedStatement check = conn.prepareStatement(
                         "SELECT 1 FROM PLAYER_INVENTORY WHERE player_id = ? AND item_id = ?")) {
                    check.setInt(1, playerId);
                    check.setInt(2, databaseItemId);
                    alreadyInInventory = check.executeQuery().next();
                }
            }
            
            if (alreadyInInventory) {
                // Already in inventory, don't make any changes
                return "<b>\nYou already have a " + itemName + ".</b>";
            }
            
            // Only proceed with changes if the item is not already in inventory
            // In-memory move
            room.removeItem(itemNum);
            player.addItem(item);

            // DB update: remove from ROOM_INVENTORY, add to player_items
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
            
            // 2) INSERT into player_items (new schema)
            try (PreparedStatement ins = conn.prepareStatement(
                     "INSERT INTO player_items(player_id, item_id) VALUES(?, ?)")) {
                ins.setInt(1, playerId);
                ins.setInt(2, databaseItemId);
                ins.executeUpdate();
            }
            
            // 3) Also INSERT into PLAYER_INVENTORY for backward compatibility
            try (PreparedStatement ins = conn.prepareStatement(
                     "INSERT INTO PLAYER_INVENTORY(player_id, item_id) VALUES(?, ?)")) {
                ins.setInt(1, playerId);
                ins.setInt(2, databaseItemId);
                ins.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Inventory DB update failed", e);
        }

        // Notify quest system of collect
        engine.fireEvent("COLLECT", itemName, 1);

        return "<b>\n" + itemName + " was picked up.</b>";
    }
    
    public String pickupItemFromCompanion(int itemNum) {
        if (itemNum < 0) {
            return "\n<b>Pick up what?</b>";
        }
        
        Companion companion = engine.getPlayer().getPlayerCompanion();
        if (companion == null || itemNum >= companion.getInventorySize()) {
            return "\n<b>Invalid item selection.</b>";
        }
        
        Item item = companion.getItem(itemNum);
        String itemName = item.getName();
        
        // Get player id for multiplayer support
        Player player = engine.getPlayer();
        int playerId = player.getId();
        
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
            
            // Check in new player_items table first
            boolean alreadyInInventory = false;
            try (PreparedStatement check = conn.prepareStatement(
                     "SELECT 1 FROM player_items WHERE player_id = ? AND item_id = ?")) {
                check.setInt(1, playerId);
                check.setInt(2, databaseItemId);
                alreadyInInventory = check.executeQuery().next();
            }
            
            // If not found, check legacy PLAYER_INVENTORY table
            if (!alreadyInInventory) {
                try (PreparedStatement check = conn.prepareStatement(
                         "SELECT 1 FROM PLAYER_INVENTORY WHERE player_id = ? AND item_id = ?")) {
                    check.setInt(1, playerId);
                    check.setInt(2, databaseItemId);
                    alreadyInInventory = check.executeQuery().next();
                }
            }
            
            if (alreadyInInventory) {
                // Already in inventory, don't make any changes
                return "<b>\nYou already have a " + itemName + ".</b>";
            }
            
            // Only proceed with changes if the item is not already in inventory
            // In-memory move
            companion.removeItem(itemNum);
            player.addItem(item);

            // DB update: remove from COMPANION_INVENTORY, add to player_items
            // 1) DELETE from COMPANION_INVENTORY - check if it exists first
            boolean itemExistsInCompanion = false;
            try (PreparedStatement checkRoom = conn.prepareStatement(
                     "SELECT 1 FROM COMPANION_INVENTORY WHERE companion_id = ? AND item_id = ?")) {
                checkRoom.setInt(1, 1);
                checkRoom.setInt(2, databaseItemId);
                itemExistsInCompanion = checkRoom.executeQuery().next();
            }
            
            if (itemExistsInCompanion) {
                try (PreparedStatement del = conn.prepareStatement(
                         "DELETE FROM COMPANION_INVENTORY WHERE companion_id = ? AND item_id = ?")) {
                    del.setInt(1, 1);
                    del.setInt(2, databaseItemId);
                    del.executeUpdate();
                }
            }
            
            // 2) INSERT into player_items (new schema)
            try (PreparedStatement ins = conn.prepareStatement(
                     "INSERT INTO player_items(player_id, item_id) VALUES(?, ?)")) {
                ins.setInt(1, playerId);
                ins.setInt(2, databaseItemId);
                ins.executeUpdate();
            }
            
            // 3) Also INSERT into PLAYER_INVENTORY for backward compatibility
            try (PreparedStatement ins = conn.prepareStatement(
                     "INSERT INTO PLAYER_INVENTORY(player_id, item_id) VALUES(?, ?)")) {
                ins.setInt(1, playerId);
                ins.setInt(2, databaseItemId);
                ins.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Inventory DB update failed", e);
        }
            
        return "<b>\n" + itemName + " was picked up.</b>";
    }

    /** Drop an item from the player into the current room */
    public String dropItem(int itemNum) {
        Player player = engine.getPlayer();
        int playerId = player.getId();
        
        if (itemNum < 0 || itemNum >= player.getInventorySize()) {
            return "\n<b>Invalid item selection.</b>";
        }
        
        // In-memory move
        Item item = player.getItem(itemNum);
        String itemName = item.getName();
        
        // DB update: remove from player_items & PLAYER_INVENTORY, add to ROOM_INVENTORY
        try (Connection conn = DerbyDatabase.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
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
            
            // If we couldn't find the item ID in the database, we need to create it
            if (databaseItemId == -1) {
                // Get the next available item_id
                try (PreparedStatement psMaxId = conn.prepareStatement(
                         "SELECT MAX(item_id) + 1 AS next_id FROM ITEM")) {
                    ResultSet rsMaxId = psMaxId.executeQuery();
                    if (rsMaxId.next()) {
                        databaseItemId = rsMaxId.getInt("next_id");
                        if (rsMaxId.wasNull()) {
                            databaseItemId = 1;
                        }
                    }
                }
                
                // Insert the new item into the ITEM table
                try (PreparedStatement psInsertItem = conn.prepareStatement(
                         "INSERT INTO ITEM (item_id, name, value, weight, long_description, short_description) " +
                         "VALUES (?, ?, ?, ?, ?, ?)")) {
                    psInsertItem.setInt(1, databaseItemId);
                    psInsertItem.setString(2, item.getName());
                    psInsertItem.setInt(3, item.getValue());
                    psInsertItem.setInt(4, item.getWeight());
                    psInsertItem.setString(5, item.getDescription());
                    psInsertItem.setString(6, item.getShortDescription());
                    psInsertItem.executeUpdate();
                }
            }
            
            // Verify the room exists in the database
            int roomId = engine.getCurrentRoomNum() + 1;
            boolean roomExists = false;
            try (PreparedStatement psCheckRoom = conn.prepareStatement(
                     "SELECT 1 FROM ROOM WHERE room_id = ?")) {
                psCheckRoom.setInt(1, roomId);
                ResultSet rsRoom = psCheckRoom.executeQuery();
                roomExists = rsRoom.next();
            }
            
            if (!roomExists) {
                // Create the room if it doesn't exist
                try (PreparedStatement psInsertRoom = conn.prepareStatement(
                         "INSERT INTO ROOM (room_id, room_name, long_description, short_description) " +
                         "VALUES (?, ?, ?, ?)")) {
                    psInsertRoom.setInt(1, roomId);
                    psInsertRoom.setString(2, "Room " + roomId);
                    psInsertRoom.setString(3, "A room in the game");
                    psInsertRoom.setString(4, "Room " + roomId);
                    psInsertRoom.executeUpdate();
                }
            }
            
            // 1) DELETE from player_items (new schema)
            try (PreparedStatement del = conn.prepareStatement(
                     "DELETE FROM player_items WHERE player_id = ? AND item_id = ?")) {
                del.setInt(1, playerId);
                del.setInt(2, databaseItemId);
                del.executeUpdate();
            }
            
            // 2) DELETE from PLAYER_INVENTORY (old schema for backward compatibility)
            try (PreparedStatement del = conn.prepareStatement(
                     "DELETE FROM PLAYER_INVENTORY WHERE player_id = ? AND item_id = ?")) {
                del.setInt(1, playerId);
                del.setInt(2, databaseItemId);
                del.executeUpdate();
            }
            
            // 3) Check if item is already in room inventory
            boolean itemInRoom = false;
            try (PreparedStatement psCheckRoomInv = conn.prepareStatement(
                     "SELECT 1 FROM ROOM_INVENTORY WHERE room_id = ? AND item_id = ?")) {
                psCheckRoomInv.setInt(1, roomId);
                psCheckRoomInv.setInt(2, databaseItemId);
                ResultSet rsRoomInv = psCheckRoomInv.executeQuery();
                itemInRoom = rsRoomInv.next();
            }
            
            // Only insert if not already in room
            if (!itemInRoom) {
                try (PreparedStatement ins = conn.prepareStatement(
                         "INSERT INTO ROOM_INVENTORY(room_id, item_id) VALUES(?, ?)")) {
                    ins.setInt(1, roomId);
                    ins.setInt(2, databaseItemId);
                    ins.executeUpdate();
                }
            }
            
            // Only remove from memory and add to room if all DB operations succeed
            player.removeItem(itemNum);
            Room room = engine.getRooms().get(engine.getCurrentRoomNum());
            room.addItem(item);
            
            conn.commit(); // Commit all changes
            
        } catch (SQLException e) {
            throw new RuntimeException("Inventory DB update failed", e);
        }

        return "<b>\n" + itemName + " was dropped.</b>";
    }
    
    /** Give an item from player inventory to companion */
    public String giveItemToCompanion(int itemNum) {
        if (itemNum < 0 || itemNum >= engine.getPlayer().getInventorySize()) {
            return "\n<b>Invalid item selection.</b>";
        }
        
        // Check if there is a companion
        if (engine.getPlayer().getPlayerCompanion() == null) {
            return "\n<b>You don't have a companion to give items to.</b>";
        }
        
        // In-memory move
        Item item = engine.getPlayer().getItem(itemNum);
        String itemName = item.getName();
        engine.getPlayer().removeItem(itemNum);
        Companion companion = engine.getPlayer().getPlayerCompanion();
        companion.addItem(item);
        
        Player player = engine.getPlayer();
        int playerId = player.getId();

        // DB update: remove from player tables, add to COMPANION_INVENTORY
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
            
            // 1) DELETE from player_items (new schema)
            try (PreparedStatement del = conn.prepareStatement(
                     "DELETE FROM player_items WHERE player_id = ? AND item_id = ?")) {
                del.setInt(1, playerId);
                del.setInt(2, databaseItemId);
                del.executeUpdate();
            } catch (SQLException e) {
                // Ignore if table doesn't exist
                System.err.println("Warning: Could not delete from player_items: " + e.getMessage());
            }
            
            // 2) DELETE from PLAYER_INVENTORY (old schema for backward compatibility)
            try (PreparedStatement del = conn.prepareStatement(
                     "DELETE FROM PLAYER_INVENTORY WHERE player_id = ? AND item_id = ?")) {
                del.setInt(1, playerId);
                del.setInt(2, databaseItemId);
                del.executeUpdate();
            }
            
            // 3) INSERT into COMPANION_INVENTORY
            try (PreparedStatement ins = conn.prepareStatement(
                     "INSERT INTO COMPANION_INVENTORY(companion_id, item_id) VALUES(?, ?)")) {
                ins.setInt(1, 1); // Hardcoded companion ID
                ins.setInt(2, databaseItemId);
                ins.executeUpdate();
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Inventory DB update failed", e);
        }

        return "<b>\n" + itemName + " was given to " + companion.getName() + ".</b>";
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
    
    public int CompanionItemNameToID(String name) {
        Companion companion = engine.getPlayer().getPlayerCompanion();
        for (int i = 0; i < companion.getInventorySize(); i++) {
            if (name.equalsIgnoreCase(companion.getItemName(i))) {
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

        // Get player ID for multiplayer support
        Player player = engine.getPlayer();
        int playerId = player.getId();

        // In-memory apply
        player.setHp(player.getHp() + heal);
        if (multi != 0) {
            player.setdamageMulti(multi + player.getdamageMulti());
        }
        player.removeItem(itemNum);

        // Lookup item in database
        int databaseItemId = -1;
        try (Connection conn = DerbyDatabase.getConnection()) {
            try (PreparedStatement psGetItemId = conn.prepareStatement(
                     "SELECT item_id FROM ITEM WHERE name = ?")) {
                psGetItemId.setString(1, raw.getName());
                ResultSet rsItemId = psGetItemId.executeQuery();
                if (rsItemId.next()) {
                    databaseItemId = rsItemId.getInt("item_id");
                }
            }
            
            // If we couldn't find the item ID in the database, use the itemNum + 1 as fallback
            if (databaseItemId == -1) {
                databaseItemId = itemNum + 1;
            }
            
            // Delete from both inventory tables
            try {
                // Delete from new schema table
                try (PreparedStatement del = conn.prepareStatement(
                     "DELETE FROM player_items WHERE player_id = ? AND item_id = ?")) {
                    del.setInt(1, playerId);
                    del.setInt(2, databaseItemId);
                    del.executeUpdate();
                }
            } catch (SQLException e) {
                // If table doesn't exist yet, log and continue
                System.err.println("Warning: Could not delete from player_items: " + e.getMessage());
            }
            
            // Delete from old schema table
            try (PreparedStatement del = conn.prepareStatement(
                 "DELETE FROM PLAYER_INVENTORY WHERE player_id = ? AND item_id = ?")) {
                del.setInt(1, playerId);
                del.setInt(2, databaseItemId);
                del.executeUpdate();
            }
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
    public String equipArmor(int itemNum) {
        // 1) bounds check
        if (itemNum < 0 || itemNum >= engine.getPlayer().getInventorySize()) {
            return "\n<b>Invalid item selection.</b>";
        }
        // 2) type check
        Item raw = engine.getPlayer().getItem(itemNum);
        if (!(raw instanceof Armor)) {
            return "\n<b>You can't equip that!</b>";
        }
        Armor armor = (Armor) raw;

        // 3) decide slot
        ArmorSlot slot = armor.slot();

        // Get player ID for multiplayer support
        Player player = engine.getPlayer();
        int playerId = player.getId();

        // 4) in-memory: equip
        player.equip(slot, armor);
        // ─── ADDED: remove it from in-memory inventory ───
        player.removeItem(itemNum);

        // 5) DB: delete old, insert new, **then** delete from inventory
        try (Connection conn = DerbyDatabase.getConnection();
             PreparedStatement del   = conn.prepareStatement(
                 "DELETE FROM PLAYER_EQUIPMENT WHERE player_id = ? AND slot = ?"
             );
             PreparedStatement ins   = conn.prepareStatement(
                 "INSERT INTO PLAYER_EQUIPMENT (player_id, slot, armor_id) VALUES (?,?,?)"
             );
             // ─── Delete from both inventory tables ───
             PreparedStatement delInvOld = conn.prepareStatement(
                 "DELETE FROM PLAYER_INVENTORY WHERE player_id = ? AND item_id = ?"
             );
             PreparedStatement delInvNew = conn.prepareStatement(
                 "DELETE FROM player_items WHERE player_id = ? AND item_id = ?"
             )
        ) {
            conn.setAutoCommit(false);

            // lookup DB item_id
            int databaseItemId = -1;
            try (PreparedStatement psGetItemId = conn.prepareStatement(
                     "SELECT item_id FROM ITEM WHERE name = ?"
                 )) {
                psGetItemId.setString(1, armor.getName());
                try (ResultSet rs = psGetItemId.executeQuery()) {
                    if (rs.next()) databaseItemId = rs.getInt("item_id");
                }
            }
            if (databaseItemId == -1) {
                databaseItemId = itemNum + 1;
            }

            // Delete existing equipment in that slot
            del.setInt(1, playerId);
            del.setString(2, slot.name());
            del.executeUpdate();

            // Insert new equipment
            ins.setInt(1, playerId);
            ins.setString(2, slot.name());
            ins.setInt(3, databaseItemId);
            ins.executeUpdate();

            // Delete from both inventory tables
            try {
                // Delete from new schema table
                delInvNew.setInt(1, playerId);
                delInvNew.setInt(2, databaseItemId);
                delInvNew.executeUpdate();
            } catch (SQLException e) {
                // If table doesn't exist yet, log and continue
                System.err.println("Warning: Could not delete from player_items: " + e.getMessage());
            }
            
            // Always delete from old schema table
            delInvOld.setInt(1, playerId);
            delInvOld.setInt(2, databaseItemId);
            delInvOld.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update DB after equipping armor", e);
        }

        return "\n<b>" + armor.getName() + " equipped to "
             + slot.name().toLowerCase() + " slot.</b>";
    }
    public String unequipArmor(String slot) {
        // 0) convert the incoming string to an ArmorSlot, safely
        ArmorSlot armorSlot;
        try {
            armorSlot = ArmorSlot.valueOf(slot.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return "\n<b>Invalid slot: " + slot + ".</b>";
        }

        // Get player for multiplayer support
        Player player = engine.getPlayer();
        int playerId = player.getId();

        // 1) check there is something to unequip
        Armor equipped = player.getEquippedArmor(armorSlot);
        if (equipped == null) {
            return "\n<b>No armor is equipped in the " 
                 + armorSlot.name().toLowerCase() + " slot.</b>";
        }

        // 2) in-memory: remove from equipment and add back to inventory
        player.unequip(armorSlot);
        player.addItem(equipped);

        // 3) DB: delete from PLAYER_EQUIPMENT, insert back into both inventory tables
        try (Connection conn = DerbyDatabase.getConnection();
             PreparedStatement delEq  = conn.prepareStatement(
                 "DELETE FROM PLAYER_EQUIPMENT WHERE player_id = ? AND slot = ?"
             );
             PreparedStatement insInvOld = conn.prepareStatement(
                 "INSERT INTO PLAYER_INVENTORY (player_id, item_id) VALUES (?,?)"
             );
             PreparedStatement insInvNew = conn.prepareStatement(
                 "INSERT INTO player_items (player_id, item_id) VALUES (?,?)"
             )) {
            conn.setAutoCommit(false);

            // look up the real item_id
            int databaseItemId = -1;
            try (PreparedStatement ps = conn.prepareStatement(
                       "SELECT item_id FROM ITEM WHERE name = ?"
                 )) {
                ps.setString(1, equipped.getName());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) databaseItemId = rs.getInt("item_id");
                }
            }
            if (databaseItemId == -1) {
                throw new RuntimeException("Could not resolve item_id for " + equipped.getName());
            }

            // delete from equipment table
            delEq.setInt(1, playerId);
            delEq.setString(2, armorSlot.name());
            delEq.executeUpdate();

            // Try to insert into new schema table first
            try {
                insInvNew.setInt(1, playerId);
                insInvNew.setInt(2, databaseItemId);
                insInvNew.executeUpdate();
            } catch (SQLException e) {
                // If table doesn't exist yet, log and continue
                System.err.println("Warning: Could not insert into player_items: " + e.getMessage());
            }

            // Always insert into old schema for backward compatibility
            insInvOld.setInt(1, playerId);
            insInvOld.setInt(2, databaseItemId);
            insInvOld.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update DB after unequipping armor", e);
        }

        return "\n<b>" + equipped.getName() 
             + " has been unequipped from " 
             + armorSlot.name().toLowerCase() 
             + " slot and returned to your inventory.</b>";
    }



}
