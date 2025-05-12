package models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import GameEngine.GameEngine;

public class GameStateManager {
    /** Call this at GameEngine.start() before loadData() */
    public static void loadState(GameEngine engine) {
        // We're now loading state for each player individually
        // This method may not be needed in the new persistence model
        // as players load their state individually through PlayerLoadManager
        // but we'll keep basic functionality for compatibility
        
        try (Connection conn = DerbyDatabase.getConnection()) {
            // Iterate through all loaded players and load their state
            for (Player player : engine.getPlayers()) {
                try (PreparedStatement ps = conn.prepareStatement(
                     "SELECT current_room, player_hp, damage_multi, running_message, " +
                     "skill_points, attack_boost, defense_boost " +
                     "FROM GAME_STATE WHERE player_id = ?")) {
                    ps.setInt(1, player.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            // Store state in player object
                            player.setCurrentRoomNum(rs.getInt("current_room") - 1);
                            player.setHp(rs.getInt("player_hp"));
                            player.setdamageMulti(rs.getDouble("damage_multi"));
                            
                            // Skip skill points, not used in the game anymore
                            // player.setSkillPoints(rs.getInt("skill_points"));
                            
                            if (rs.getObject("attack_boost") != null) {
                                player.setAttackBoost(rs.getInt("attack_boost"));
                            }
                            if (rs.getObject("defense_boost") != null) {
                                player.setdefenseBoost(rs.getInt("defense_boost"));
                            }
                            
                            String runningMessage = rs.getString("running_message");
                            if (runningMessage != null) {
                                player.setRunningMessage(runningMessage);
                            }
                            
                            // Load player's inventory
                            loadPlayerInventory(conn, player);
                            
                            // Load equipped armor
                            loadPlayerEquipment(conn, player);
                            
                            // Load player's companion
                            loadPlayerCompanion(conn, player);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // Log error but don't crash the game
            System.err.println("Failed to load game state: " + e.getMessage());
        }
    }

    /** Call this after every turn or action that changes state */
    public static void saveState(GameEngine engine) {
        // Save state for each player individually
        for (Player player : engine.getPlayers()) {
            try (Connection conn = DerbyDatabase.getConnection()) {
                // Get the necessary player data
                int playerId = player.getId();
                int userId = player.getUserId();
                int currentRoom = engine.getCurrentRoomNum() + 1; // Database uses 1-indexed rooms
                
                // Update player's room number from engine's current room
                player.setCurrentRoomNum(engine.getCurrentRoomNum());
                
                // Update player's running message from engine
                player.setRunningMessage(engine.getRunningMessage());
                
                // Check if state exists for this player
                boolean exists = false;
                try (PreparedStatement check = conn.prepareStatement(
                         "SELECT 1 FROM GAME_STATE WHERE player_id = ?")) {
                    check.setInt(1, playerId);
                    try (ResultSet rs = check.executeQuery()) {
                        exists = rs.next();
                    }
                }
                
                conn.setAutoCommit(false); // Begin transaction
                
                try {
                    // First update the PLAYER table to keep HP and stats in sync
                    String updatePlayerSql = 
                        "UPDATE PLAYER SET " +
                        "hp = ?, " +
                        "damage_multi = ?, " +
                        "attack_boost = ?, " +
                        "defense_boost = ?, " +
                        "last_updated = CURRENT_TIMESTAMP " +
                        "WHERE player_id = ?";
                    
                    try (PreparedStatement psPlayer = conn.prepareStatement(updatePlayerSql)) {
                        psPlayer.setInt(1, player.getHp());
                        psPlayer.setDouble(2, player.getdamageMulti());
                        psPlayer.setInt(3, player.getAttackBoost());
                        psPlayer.setInt(4, player.getdefenseBoost());
                        psPlayer.setInt(5, playerId);
                        psPlayer.executeUpdate();
                        
                        System.out.println("Updated PLAYER table - HP: " + player.getHp() + 
                                          ", Damage Multi: " + player.getdamageMulti() +
                                          ", Attack Boost: " + player.getAttackBoost() +
                                          ", Defense Boost: " + player.getdefenseBoost());
                    }
                    
                    if (exists) {
                        // Update existing state
                        String updateSql =
                            "UPDATE GAME_STATE " +
                            "   SET current_room = ?, " +
                            "       player_hp = ?, " +
                            "       damage_multi = ?, " +
                            "       running_message = ?, " +
                            "       attack_boost = ?, " +
                            "       defense_boost = ?, " +
                            "       last_saved = CURRENT_TIMESTAMP " +
                            " WHERE player_id = ?";
                        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                            ps.setInt(1, currentRoom);
                            ps.setInt(2, player.getHp());
                            ps.setDouble(3, player.getdamageMulti());
                            ps.setString(4, engine.getRunningMessage());
                            ps.setInt(5, player.getAttackBoost());
                            ps.setInt(6, player.getdefenseBoost());
                            ps.setInt(7, playerId);
                            ps.executeUpdate();
                        }
                    } else {
                        // Insert new state record - need to ensure user_id is set in player
                        String insertSql =
                            "INSERT INTO GAME_STATE(" +
                            "    user_id, player_id, current_room, player_hp, " +
                            "    damage_multi, running_message, attack_boost, " +
                            "    defense_boost, last_saved, save_name" +
                            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
                        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                            ps.setInt(1, userId);
                            ps.setInt(2, playerId);
                            ps.setInt(3, currentRoom);
                            ps.setInt(4, player.getHp());
                            ps.setDouble(5, player.getdamageMulti());
                            ps.setString(6, engine.getRunningMessage());
                            ps.setInt(7, player.getAttackBoost());
                            ps.setInt(8, player.getdefenseBoost());
                            ps.setString(9, "Autosave");
                            ps.executeUpdate();
                        }
                    }
                    
                    // Save player's inventory - clear and re-save
                    savePlayerInventory(conn, player);
                    
                    // Save player's companion
                    savePlayerCompanion(conn, player);
                    
                    conn.commit(); // Commit transaction
                } catch (SQLException e) {
                    conn.rollback(); // Roll back on error
                    throw e;
                } finally {
                    conn.setAutoCommit(true); // Return to auto-commit mode
                }
            } catch (SQLException e) {
                // Log error but don't crash the game
                System.err.println("Failed to save player state for player " + player.getId() + ": " + e.getMessage());
            }
        }
    }
    
    // Helper method to load player's inventory from the database
    private static void loadPlayerInventory(Connection conn, Player player) throws SQLException {
        // First try to load from player_items (new schema)
        String sqlNewSchema = "SELECT i.item_id, i.name, i.value, i.weight, i.long_description, " +
                     "i.short_description, i.type, i.healing, i.damage_multi, i.attack_damage, " +
                     "i.attack_boost, i.defense_boost, i.slot, i.disassemblable " +
                     "FROM player_items pi " +
                     "JOIN ITEM i ON pi.item_id = i.item_id " +
                     "WHERE pi.player_id = ?";
        
        // Fallback to old schema if needed
        String sqlOldSchema = "SELECT i.item_id, i.name, i.value, i.weight, i.long_description, " +
                     "i.short_description, i.type, i.healing, i.damage_multi, i.attack_damage, " +
                     "i.attack_boost, i.defense_boost, i.slot, i.disassemblable " +
                     "FROM PLAYER_INVENTORY pi " +
                     "JOIN ITEM i ON pi.item_id = i.item_id " +
                     "WHERE pi.player_id = ? " +
                     "AND NOT EXISTS (SELECT 1 FROM player_items newpi WHERE newpi.player_id = pi.player_id AND newpi.item_id = pi.item_id)";
        
        // Try the new schema first
        boolean foundItems = false;
        try (PreparedStatement ps = conn.prepareStatement(sqlNewSchema)) {
            ps.setInt(1, player.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    foundItems = true;
                    loadItemFromResultSet(rs, player);
                }
            }
        } catch (SQLException e) {
            // Table might not exist yet, continue to try old schema
            System.err.println("Warning: Could not load from player_items: " + e.getMessage());
        }
        
        // If nothing found or error occurred, try the old schema
        if (!foundItems) {
            try (PreparedStatement ps = conn.prepareStatement(sqlOldSchema)) {
                ps.setInt(1, player.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        loadItemFromResultSet(rs, player);
                    }
                }
            } catch (SQLException e) {
                // If old schema also fails, it's a real error
                throw e;
            }
        }
    }
    
    // Helper method to load an item from result set
    private static void loadItemFromResultSet(ResultSet rs, Player player) throws SQLException {
        // Get all item details from result set
        String name = rs.getString("name");
        int value = rs.getInt("value");
        int weight = rs.getInt("weight");
        String longDesc = rs.getString("long_description");
        String shortDesc = rs.getString("short_description");
        String type = rs.getString("type");
        int healing = rs.getInt("healing");
        double dmgMulti = rs.getDouble("damage_multi");
        int attackDmg = rs.getInt("attack_damage");
        int attackBoost = rs.getInt("attack_boost");
        int defenseBoost = rs.getInt("defense_boost");
        String slot = rs.getString("slot");
        boolean disassemblable = rs.getBoolean("disassemblable");
        
        // Using empty components array - load separately if needed
        String[] components = new String[0];
        
        // Create the appropriate item type
        Item item;
        if ("WEAPON".equals(type)) {
            item = new Weapon(value, weight, name, components, attackDmg, longDesc, shortDesc);
        } else if ("ARMOR".equals(type)) {
            // Convert string values to the appropriate types
            ArmorSlot armorSlot = (slot != null && !slot.isEmpty()) ? ArmorSlot.valueOf(slot) : ArmorSlot.ACCESSORY;
            item = new Armor(value, weight, name, components, longDesc, shortDesc, 
                    healing, attackBoost, defenseBoost, armorSlot);
        } else if ("UTILITY".equals(type)) {
            item = new Utility(value, weight, name, components, longDesc, shortDesc, healing, dmgMulti);
            // No need to set disassemblable, not supported in the Utility class
        } else {
            item = new Item(value, weight, name, components, longDesc, shortDesc);
        }
        
        // Add item to inventory
        player.getInventory().addItem(item);
    }
    
    // Helper method to save player inventory to the database
    private static void savePlayerInventory(Connection conn, Player player) throws SQLException {
        // First, delete current inventory from both tables
        try (PreparedStatement deleteNew = conn.prepareStatement(
                "DELETE FROM player_items WHERE player_id = ?");
             PreparedStatement deleteOld = conn.prepareStatement(
                "DELETE FROM PLAYER_INVENTORY WHERE player_id = ?")) {
            
            deleteNew.setInt(1, player.getId());
            deleteNew.executeUpdate();
            
            deleteOld.setInt(1, player.getId());
            deleteOld.executeUpdate();
        } catch (SQLException e) {
            // The player_items table might not exist yet
            // Just continue with old schema
            try (PreparedStatement delete = conn.prepareStatement(
                    "DELETE FROM PLAYER_INVENTORY WHERE player_id = ?")) {
                delete.setInt(1, player.getId());
                delete.executeUpdate();
            }
        }
        
        // Then insert current items into both tables
        String insertNewSql = "INSERT INTO player_items (player_id, item_id) VALUES (?, ?)";
        String insertOldSql = "INSERT INTO PLAYER_INVENTORY (player_id, item_id) VALUES (?, ?)";
        
        try (PreparedStatement insertNew = conn.prepareStatement(insertNewSql);
             PreparedStatement insertOld = conn.prepareStatement(insertOldSql)) {
            
            for (int i = 0; i < player.getInventorySize(); i++) {
                Item item = player.getItem(i);
                // Need to get the item ID from the database or create a new item
                int itemId = getOrCreateItemId(conn, item);
                
                try {
                    // Insert into new schema
                    insertNew.setInt(1, player.getId());
                    insertNew.setInt(2, itemId);
                    insertNew.addBatch();
                } catch (SQLException e) {
                    // The player_items table might not exist yet
                    System.err.println("Warning: Could not insert into player_items: " + e.getMessage());
                }
                
                // Always insert into old schema for backward compatibility
                insertOld.setInt(1, player.getId());
                insertOld.setInt(2, itemId);
                insertOld.addBatch();
            }
            
            try {
                insertNew.executeBatch();
            } catch (SQLException e) {
                // Ignore if player_items doesn't exist
                System.err.println("Warning: Batch insert to player_items failed: " + e.getMessage());
            }
            
            insertOld.executeBatch();
        }
    }
    
    // Helper method to get an existing item ID or create a new item
    private static int getOrCreateItemId(Connection conn, Item item) throws SQLException {
        // First try to find the item by name
        String findSql = "SELECT item_id FROM ITEM WHERE name = ?";
        try (PreparedStatement find = conn.prepareStatement(findSql)) {
            find.setString(1, item.getName());
            try (ResultSet rs = find.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("item_id");
                }
            }
        }
        
        // If not found, create a new item
        // Get the next available item_id
        int newItemId = 1;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(item_id) + 1 AS next_id FROM ITEM")) {
            if (rs.next()) {
                newItemId = rs.getInt("next_id");
                if (rs.wasNull()) {
                    newItemId = 1;
                }
            }
        }
        
        // Insert the new item
        String insertSql = "INSERT INTO ITEM (item_id, name, value, weight, long_description, " +
                          "short_description, type, healing, damage_multi, attack_damage, " +
                          "attack_boost, defense_boost, slot, disassemblable) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
            insert.setInt(1, newItemId);
            insert.setString(2, item.getName());
            insert.setInt(3, item.getValue());
            insert.setInt(4, item.getWeight());
            insert.setString(5, item.getDescription());
            insert.setString(6, item.getShortDescription());
            
            // Default type is regular item
            insert.setString(7, "ITEM");
            insert.setInt(8, 0); // healing
            insert.setDouble(9, 1.0); // damage_multi
            insert.setInt(10, 0); // attack_damage
            insert.setInt(11, 0); // attack_boost
            insert.setInt(12, 0); // defense_boost
            insert.setString(13, ""); // slot
            insert.setBoolean(14, false); // disassemblable
            
            insert.executeUpdate();
        }
        
        return newItemId;
    }
    
    // Helper method to load player's equipped armor
    public static void loadPlayerEquipment(Connection conn, Player player) throws SQLException {
        System.out.println("Loading equipment for player ID: " + player.getId());
        
        String sql = "SELECT pe.slot, i.* " +
                     "FROM PLAYER_EQUIPMENT pe " +
                     "JOIN ITEM i ON pe.armor_id = i.item_id " +
                     "WHERE pe.player_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, player.getId());
            
            int equipCount = 0;
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    equipCount++;
                    String slot = rs.getString("slot");
                    String name = rs.getString("name");
                    int value = rs.getInt("value");
                    int weight = rs.getInt("weight");
                    String longDesc = rs.getString("long_description");
                    String shortDesc = rs.getString("short_description");
                    int healing = rs.getInt("healing");
                    double attackBoost = rs.getDouble("attack_boost");
                    int defenseBoost = rs.getInt("defense_boost");
                    
                    System.out.println("Loading equipped item: " + name + " in slot " + slot);
                    
                    // Create armor and equip it
                    ArmorSlot armorSlot = ArmorSlot.valueOf(slot);
                    Armor armor = new Armor(value, weight, name, new String[0], longDesc, shortDesc, 
                                     healing, attackBoost, defenseBoost, armorSlot);
                    player.equip(armorSlot, armor);
                }
            }
            System.out.println("Loaded " + equipCount + " equipped items for player");
        }
    }
    
    // Helper method to load player's companion
    private static void loadPlayerCompanion(Connection conn, Player player) throws SQLException {
        String sql = "SELECT c.* " +
                     "FROM PLAYER_COMPANION pc " +
                     "JOIN COMPANION c ON pc.companion_id = c.companion_id " +
                     "WHERE pc.player_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, player.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int companionId = rs.getInt("companion_id");
                    String name = rs.getString("name");
                    int hp = rs.getInt("hp");
                    boolean aggression = rs.getBoolean("aggression");
                    int damage = rs.getInt("damage");
                    String longDesc = rs.getString("long_description");
                    String shortDesc = rs.getString("short_description");
                    boolean isCompanion = rs.getBoolean("companion");
                    
                    // Create companion
                    Companion companion = new Companion(name, hp, aggression, new String[0], damage,
                                        new Inventory(new ArrayList<>(), 100),
                                        longDesc, shortDesc, isCompanion);
                    
                    // Load companion's inventory
                    loadCompanionInventory(conn, companionId, companion);
                    
                    // Set player's companion
                    player.setPlayerCompanion(companion);
                }
            }
        }
    }
    
    // Helper method to load companion's inventory
    private static void loadCompanionInventory(Connection conn, int companionId, Companion companion) throws SQLException {
        String sql = "SELECT i.* " +
                     "FROM COMPANION_INVENTORY ci " +
                     "JOIN ITEM i ON ci.item_id = i.item_id " +
                     "WHERE ci.companion_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, companionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    int value = rs.getInt("value");
                    int weight = rs.getInt("weight");
                    String longDesc = rs.getString("long_description");
                    String shortDesc = rs.getString("short_description");
                    
                    // Create item and add to companion's inventory
                    Item item = new Item(value, weight, name, new String[0], longDesc, shortDesc);
                    companion.getInventory().addItem(item);
                }
            }
        }
    }
    
    // Helper method to save player's companion
    private static void savePlayerCompanion(Connection conn, Player player) throws SQLException {
        Companion companion = player.getPlayerCompanion();
        if (companion == null) {
            // Remove any existing companion association
            try (PreparedStatement delete = conn.prepareStatement(
                    "DELETE FROM PLAYER_COMPANION WHERE player_id = ?")) {
                delete.setInt(1, player.getId());
                delete.executeUpdate();
            }
            return;
        }
        
        // First, try to find if this companion exists in the database
        int companionId = -1;
        try (PreparedStatement find = conn.prepareStatement(
                "SELECT companion_id FROM COMPANION WHERE name = ?")) {
            find.setString(1, companion.getName());
            try (ResultSet rs = find.executeQuery()) {
                if (rs.next()) {
                    companionId = rs.getInt("companion_id");
                }
            }
        }
        
        // If not found, create a new companion
        if (companionId == -1) {
            // Get the next available companion_id
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT MAX(companion_id) + 1 AS next_id FROM COMPANION")) {
                if (rs.next()) {
                    companionId = rs.getInt("next_id");
                    if (rs.wasNull()) {
                        companionId = 1;
                    }
                }
            }
            
            // Insert new companion with basic info
            String insertSql = "INSERT INTO COMPANION (companion_id, name, hp, companion, room_num) " +
                              "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                insert.setInt(1, companionId);
                insert.setString(2, companion.getName());
                insert.setInt(3, companion.getHp());
                insert.setBoolean(4, true); // is a companion
                insert.setInt(5, 0); // Default room number
                insert.executeUpdate();
            }
        } else {
            // Update existing companion's health
            String updateSql = "UPDATE COMPANION SET hp = ? WHERE companion_id = ?";
            try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                update.setInt(1, companion.getHp());
                update.setInt(2, companionId);
                update.executeUpdate();
            }
        }
        
        // Now link companion to player
        // First delete any existing link
        try (PreparedStatement delete = conn.prepareStatement(
                "DELETE FROM PLAYER_COMPANION WHERE player_id = ?")) {
            delete.setInt(1, player.getId());
            delete.executeUpdate();
        }
        
        // Then create new link
        try (PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO PLAYER_COMPANION (player_id, companion_id) VALUES (?, ?)")) {
            insert.setInt(1, player.getId());
            insert.setInt(2, companionId);
            insert.executeUpdate();
        }
        
        // Save companion's inventory
        saveCompanionInventory(conn, companionId, companion);
    }
    
    // Helper method to save companion's inventory
    private static void saveCompanionInventory(Connection conn, int companionId, Companion companion) throws SQLException {
        // First, delete current inventory
        try (PreparedStatement delete = conn.prepareStatement(
                "DELETE FROM COMPANION_INVENTORY WHERE companion_id = ?")) {
            delete.setInt(1, companionId);
            delete.executeUpdate();
        }
        
        // Then insert current items
        if (companion.getInventory() != null && companion.getInventorySize() > 0) {
            String insertSql = "INSERT INTO COMPANION_INVENTORY (companion_id, item_id) VALUES (?, ?)";
            try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                for (int i = 0; i < companion.getInventorySize(); i++) {
                    Item item = companion.getItem(i);
                    int itemId = getOrCreateItemId(conn, item);
                    
                    insert.setInt(1, companionId);
                    insert.setInt(2, itemId);
                    insert.addBatch();
                }
                insert.executeBatch();
            }
        }
    }
    
    /**
     * When server restarts, reinitialize rooms from CSV files
     * @param engine The game engine
     */
    public static void reinitializeRoomsFromCSV(GameEngine engine) {
        // Delete room-related data from database
        try (Connection conn = DerbyDatabase.getConnection()) {
            // First, backup player companion inventories
            Map<Integer, List<Integer>> playerCompanionItems = new HashMap<>();
            
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT pc.player_id, pc.companion_id, ci.item_id " +
                    "FROM PLAYER_COMPANION pc " +
                    "JOIN COMPANION_INVENTORY ci ON pc.companion_id = ci.companion_id")) {
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int companionId = rs.getInt("companion_id");
                        int itemId = rs.getInt("item_id");
                        
                        if (!playerCompanionItems.containsKey(companionId)) {
                            playerCompanionItems.put(companionId, new ArrayList<>());
                        }
                        playerCompanionItems.get(companionId).add(itemId);
                    }
                }
            }
            
            System.out.println("Backed up " + playerCompanionItems.size() + " player companion inventories");
            
            // Delete the junction tables
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM ROOM_INVENTORY");
                stmt.executeUpdate("DELETE FROM NPC_ROOM");
                stmt.executeUpdate("DELETE FROM COMPANION_ROOM");
                stmt.executeUpdate("DELETE FROM ROOM_CONNECTIONS");
                stmt.executeUpdate("DELETE FROM COMPANION_INVENTORY");
            }
            
            // Then delete the main room table
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM ROOM");
            }
            
            // Now reseed from CSV files
            DatabaseInitializer.seedRoomsFromCSV(conn);
            
            // Restore player companion inventories
            if (!playerCompanionItems.isEmpty()) {
                try (PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO COMPANION_INVENTORY (companion_id, item_id) VALUES (?, ?)")) {
                    
                    for (Map.Entry<Integer, List<Integer>> entry : playerCompanionItems.entrySet()) {
                        int companionId = entry.getKey();
                        List<Integer> itemIds = entry.getValue();
                        
                        for (Integer itemId : itemIds) {
                            insert.setInt(1, companionId);
                            insert.setInt(2, itemId);
                            insert.addBatch();
                        }
                    }
                    
                    int[] results = insert.executeBatch();
                    System.out.println("Restored " + results.length + " companion inventory items");
                }
            }
            
            // Reload rooms from the freshly seeded database
            try {
                // The engine doesn't have a getRoomManager method
                // Instead, tell the engine to reload its data
                engine.loadData();
            } catch (Exception e) {
                System.err.println("Warning: Failed to reload room data: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Failed to reinitialize rooms from CSV: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error reinitializing rooms: " + e.getMessage());
        }
    }
}
