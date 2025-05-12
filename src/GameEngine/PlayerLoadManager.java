package GameEngine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import models.Armor;
import models.ArmorSlot;
import models.Companion;
import models.DerbyDatabase;
import models.Inventory;
import models.Item;
import models.Player;

public class PlayerLoadManager {
    
    private DerbyDatabase db;
    
    public PlayerLoadManager() {
        this.db = new DerbyDatabase();
    }
    
    /**
     * Gets up to 3 save slots for a user
     * @param userId The user ID
     * @return List of GameState objects representing save slots
     */
    public List<GameState> getSaveSlots(int userId) {
        List<GameState> saveSlots = new ArrayList<>();
        
        try (Connection conn = db.getConnection()) {
            String sql = "SELECT state_id, player_id, current_room, player_hp, save_name " +
                         "FROM GAME_STATE WHERE user_id = ? ORDER BY last_saved DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next() && saveSlots.size() < 3) {
                        GameState state = new GameState();
                        state.setStateId(rs.getInt("state_id"));
                        state.setPlayerId(rs.getInt("player_id"));
                        state.setCurrentRoom(rs.getInt("current_room"));
                        state.setPlayerHp(rs.getInt("player_hp"));
                        state.setSaveName(rs.getString("save_name"));
                        
                        saveSlots.add(state);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // If less than 3 save slots exist, add empty ones to reach 3
        while (saveSlots.size() < 3) {
            saveSlots.add(null);
        }
        
        return saveSlots;
    }
    
    /**
     * Loads a player from the database by ID
     * @param playerId The player ID to load
     * @return The loaded Player object with an empty companion
     */
    public Player loadPlayer(int playerId) {
        Player player = null;
        
        try (Connection conn = db.getConnection()) {
            // Log the load attempt
            System.out.println("Loading player with ID: " + playerId);
            
            // Check for player stats in GAME_STATE first as it's more up-to-date
            boolean hasGameState = false;
            String gameStateSql = "SELECT gs.player_hp, gs.damage_multi, gs.attack_boost, gs.defense_boost, gs.current_room " +
                           "FROM GAME_STATE gs WHERE gs.player_id = ?";
            
            int playerHp = 0;
            double damageMulti = 1.0;
            int attackBoost = 0;
            int defenseBoost = 0;
            int currentRoom = 0;
            
            try (PreparedStatement psGameState = conn.prepareStatement(gameStateSql)) {
                psGameState.setInt(1, playerId);
                try (ResultSet gameStateRs = psGameState.executeQuery()) {
                    if (gameStateRs.next()) {
                        hasGameState = true;
                        playerHp = gameStateRs.getInt("player_hp");
                        damageMulti = gameStateRs.getDouble("damage_multi");
                        
                        if (gameStateRs.getObject("attack_boost") != null) {
                            attackBoost = gameStateRs.getInt("attack_boost");
                        }
                        if (gameStateRs.getObject("defense_boost") != null) {
                            defenseBoost = gameStateRs.getInt("defense_boost");
                        }
                        
                        currentRoom = gameStateRs.getInt("current_room");
                        
                        System.out.println("Found game state data - HP: " + playerHp + 
                                         ", DMG Multi: " + damageMulti + 
                                         ", Attack: " + attackBoost + 
                                         ", Defense: " + defenseBoost);
                    }
                }
            }
            
            // Load basic player info
            String sql = "SELECT p.player_id, p.user_id, p.name, p.hp, p.skill_points, p.damage_multi, " +
                         "p.long_description, p.short_description, p.player_type, " +
                         "p.attack_boost, p.defense_boost " +
                         "FROM PLAYER p WHERE p.player_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, playerId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Create inventory for player
                        Inventory inventory = new Inventory(new ArrayList<>(), 100); // Default max weight of 100
                        loadPlayerInventory(conn, playerId, inventory);
                        
                        // If we have game state data, use it instead of the PLAYER table data
                        // as it's more up-to-date
                        int hp = hasGameState ? playerHp : rs.getInt("hp");
                        double dmg = hasGameState ? damageMulti : rs.getDouble("damage_multi");
                        int atk = hasGameState ? attackBoost : rs.getInt("attack_boost");
                        int def = hasGameState ? defenseBoost : rs.getInt("defense_boost");
                        
                        // Create player with either game state or base values
                        player = new Player(
                            rs.getString("name"),
                            hp,
                            rs.getInt("skill_points"),
                            inventory,
                            rs.getString("long_description"),
                            rs.getString("short_description"),
                            dmg,
                            atk,
                            def
                        );
                        player.setId(playerId);
                        player.setUserId(rs.getInt("user_id"));
                        
                        // Set player type from database
                        player.setPlayerType(rs.getString("player_type"));
                        
                        // Load companion if exists
                        Companion companion = loadPlayerCompanion(conn, playerId);
                        player.setPlayerCompanion(companion);
                        
                        // If we have game state, set current room directly
                        if (hasGameState) {
                            player.setCurrentRoomNum(currentRoom - 1); // Adjust for 0-based indexing
                        } else {
                            // Otherwise load from game state, which loads other data too
                            loadPlayerGameState(conn, player);
                        }
                        
                        System.out.println("Player loaded with final HP: " + player.getHp() + 
                                         ", Attack: " + player.getAttackBoost() + 
                                         ", Defense: " + player.getdefenseBoost());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return player;
    }
    
    /**
     * Load player game state from database
     * @param conn Database connection
     * @param player Player to update with game state
     */
    private void loadPlayerGameState(Connection conn, Player player) throws SQLException {
        String sql = "SELECT current_room, player_hp, damage_multi, attack_boost, defense_boost, running_message FROM GAME_STATE WHERE player_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, player.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Store room number and running message in player object
                    player.setCurrentRoomNum(rs.getInt("current_room") - 1); // -1 to match array index
                    
                    // Update player stats from game state
                    player.setHp(rs.getInt("player_hp"));
                    player.setdamageMulti(rs.getDouble("damage_multi"));
                    
                    // Check for null values before setting
                    if (rs.getObject("attack_boost") != null) {
                        player.setAttackBoost(rs.getInt("attack_boost"));
                    }
                    if (rs.getObject("defense_boost") != null) {
                        player.setdefenseBoost(rs.getInt("defense_boost"));
                    }
                    
                    player.setRunningMessage(rs.getString("running_message"));
                    
                    // Log loaded stats to confirm they're being applied
                    System.out.println("Loaded player stats from game state - HP: " + player.getHp() + 
                                      ", Damage Multi: " + player.getdamageMulti() +
                                      ", Attack Boost: " + player.getAttackBoost() +
                                      ", Defense Boost: " + player.getdefenseBoost());
                }
            }
        }
    }
    
    /**
     * Load player's companion if one exists
     * @param conn Database connection
     * @param playerId Player ID
     * @return Companion object or null if none exists
     */
    private Companion loadPlayerCompanion(Connection conn, int playerId) throws SQLException {
        String sql = "SELECT c.* " +
                     "FROM PLAYER_COMPANION pc " +
                     "JOIN COMPANION c ON pc.companion_id = c.companion_id " +
                     "WHERE pc.player_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            
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
                    
                    // Create companion with empty inventory first
                    Companion companion = new Companion(name, hp, aggression, new String[0], damage,
                                    new Inventory(new ArrayList<>(), 100),
                                    longDesc, shortDesc, isCompanion);
                    
                    // Load companion's inventory
                    loadCompanionInventory(conn, companionId, companion);
                    
                    return companion;
                }
            }
        }
        return null;
    }
    
    /**
     * Load companion's inventory
     */
    private void loadCompanionInventory(Connection conn, int companionId, Companion companion) throws SQLException {
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
    
    /**
     * Creates a new player for a user
     * @param userId User ID
     * @param name Player name
     * @param playerClass Player class
     * @param description Player description
     * @return The newly created Player object
     */
    public Player createNewPlayer(int userId, String name, String playerClass, String description) {
        Player player = null;
        int playerId = -1;
        
        try (Connection conn = db.getConnection()) {
            // First create the player record
            String sql = "INSERT INTO PLAYER (user_id, name, hp, skill_points, damage_multi, " +
                         "long_description, short_description, player_type, attack_boost, defense_boost) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, userId);
                stmt.setString(2, name);
                
                // Set stats based on player class
                int hp = 100;
                int skillPoints = 10;
                double damageMulti = 1.0;
                int attackBoost = 0;
                int defenseBoost = 0;
                
                if ("Warrior".equalsIgnoreCase(playerClass)) {
                    hp = 120;
                    attackBoost = 5;
                } else if ("Mage".equalsIgnoreCase(playerClass)) {
                    hp = 80;
                    damageMulti = 1.3;
                    skillPoints = 15;
                } else if ("Rogue".equalsIgnoreCase(playerClass)) {
                    hp = 90;
                    attackBoost = 3;
                    defenseBoost = 2;
                }
                
                stmt.setInt(3, hp);
                stmt.setInt(4, skillPoints);
                stmt.setDouble(5, damageMulti);
                stmt.setString(6, description);
                stmt.setString(7, "A " + playerClass); // Short description
                stmt.setString(8, playerClass.toUpperCase());
                stmt.setInt(9, attackBoost);
                stmt.setInt(10, defenseBoost);
                
                stmt.executeUpdate();
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        playerId = generatedKeys.getInt(1);
                    }
                }
            }
            
            if (playerId > 0) {
                // Create inventory
                Inventory inventory = new Inventory(new ArrayList<>(), 100); // Default max weight
                
                // Add starter items based on class
                addStarterItems(conn, playerId, playerClass);
                
                // Load inventory with items we just added
                loadPlayerInventory(conn, playerId, inventory);
                
                // Create player object
                player = new Player(
                    name,
                    getPlayerHpByClass(playerClass),
                    getPlayerSkillPointsByClass(playerClass),
                    inventory,
                    description,
                    "A " + playerClass,
                    getPlayerDamageMultiByClass(playerClass),
                    getPlayerAttackBoostByClass(playerClass),
                    getPlayerDefenseBoostByClass(playerClass)
                );
                player.setId(playerId);
                player.setUserId(userId);
                
                // Set player type
                player.setPlayerType(playerClass.toUpperCase());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return player;
    }
    
    /**
     * Saves a player to a specific save slot
     * @param userId User ID
     * @param playerId Player ID
     * @param slotNumber Save slot number (1-3)
     * @param saveName Name for the save
     * @return true if successful, false otherwise
     */
    public boolean savePlayerToSlot(int userId, int playerId, int currentRoom, int playerHp, int slotNumber, String saveName) {
        try (Connection conn = db.getConnection()) {
            // Check if slot already exists
            String checkSql = "SELECT state_id FROM GAME_STATE WHERE user_id = ? AND save_name = ?";
            boolean slotExists = false;
            int stateId = -1;
            
            try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, "Slot " + slotNumber);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        slotExists = true;
                        stateId = rs.getInt("state_id");
                    }
                }
            }
            
            conn.setAutoCommit(false); // Start transaction
            
            if (slotExists) {
                // Update existing slot
                String updateSql = "UPDATE GAME_STATE SET player_id = ?, current_room = ?, " +
                                 "player_hp = ?, last_saved = CURRENT_TIMESTAMP, save_name = ? " +
                                 "WHERE state_id = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setInt(1, playerId);
                    stmt.setInt(2, currentRoom);
                    stmt.setInt(3, playerHp);
                    stmt.setString(4, saveName);
                    stmt.setInt(5, stateId);
                    
                    stmt.executeUpdate();
                }
            } else {
                // Create new slot
                String insertSql = "INSERT INTO GAME_STATE (user_id, player_id, current_room, " +
                                 "player_hp, damage_multi, running_message, save_name) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?)";
                
                try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, playerId);
                    stmt.setInt(3, currentRoom);
                    stmt.setInt(4, playerHp);
                    stmt.setDouble(5, 1.0); // Default damage multiplier
                    stmt.setString(6, ""); // Empty running message
                    stmt.setString(7, saveName);
                    
                    stmt.executeUpdate();
                }
            }
            
            // Get the player object to save its inventory, equipment, and companion
            GameEngine gameEngine = GameEngine.getInstance();
            Player player = gameEngine.getPlayerByDatabaseId(playerId);
            
            if (player != null) {
                // Save player's inventory
                savePlayerInventory(conn, player);
                
                // Save player's equipment
                savePlayerEquipment(conn, player);
                
                // Save player's companion
                savePlayerCompanion(conn, player);
            }
            
            conn.commit(); // Commit transaction
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Helper method to load a player's inventory
     */
    private void loadPlayerInventory(Connection conn, int playerId, Inventory inventory) throws SQLException {
        // Use SQL that tries the new table first, then falls back to old table if needed
        String sql = "SELECT i.item_id, i.name, i.value, i.weight, i.long_description, " +
                     "i.short_description, i.type, i.healing, i.damage_multi, i.attack_damage, " +
                     "i.attack_boost, i.defense_boost, i.slot, i.disassemblable " +
                     "FROM player_items pi " +
                     "JOIN ITEM i ON pi.item_id = i.item_id " +
                     "WHERE pi.player_id = ? " +
                     "UNION " +
                     "SELECT i.item_id, i.name, i.value, i.weight, i.long_description, " +
                     "i.short_description, i.type, i.healing, i.damage_multi, i.attack_damage, " +
                     "i.attack_boost, i.defense_boost, i.slot, i.disassemblable " +
                     "FROM PLAYER_INVENTORY oldpi " +
                     "JOIN ITEM i ON oldpi.item_id = i.item_id " +
                     "WHERE oldpi.player_id = ? " +
                     "AND NOT EXISTS (SELECT 1 FROM player_items pi WHERE pi.player_id = oldpi.player_id AND pi.item_id = oldpi.item_id)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, playerId);  // Same playerId for both queries in the UNION
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Create item with constructor arguments
                    String name = rs.getString("name");
                    int value = rs.getInt("value");
                    int weight = rs.getInt("weight");
                    String longDesc = rs.getString("long_description");
                    String shortDesc = rs.getString("short_description");
                    
                    // Using empty components array for now
                    String[] components = new String[0];
                    
                    Item item = new Item(value, weight, name, components, longDesc, shortDesc);
                    
                    // Add item to inventory
                    inventory.addItem(item);
                }
            }
        }
    }
    
    /**
     * Helper method to add starter items to a player
     */
    private void addStarterItems(Connection conn, int playerId, String playerClass) throws SQLException {
        List<Integer> starterItemIds = new ArrayList<>();
        
        // Common items for all classes
        starterItemIds.add(1); // Health potion
        
        // Class-specific starter items
        if ("Warrior".equalsIgnoreCase(playerClass)) {
            starterItemIds.add(2); // Basic sword
            starterItemIds.add(5); // Simple armor
        } else if ("Mage".equalsIgnoreCase(playerClass)) {
            starterItemIds.add(3); // Magic staff
            starterItemIds.add(7); // Mage robe
        } else if ("Rogue".equalsIgnoreCase(playerClass)) {
            starterItemIds.add(4); // Dagger
            starterItemIds.add(6); // Leather armor
        }
        
        // Add items to player inventory (using player_items AND PLAYER_INVENTORY for backward compatibility)
        String sqlNewTable = "INSERT INTO player_items (player_id, item_id) VALUES (?, ?)";
        String sqlOldTable = "INSERT INTO PLAYER_INVENTORY (player_id, item_id) VALUES (?, ?)";
        
        try (PreparedStatement stmtNew = conn.prepareStatement(sqlNewTable);
             PreparedStatement stmtOld = conn.prepareStatement(sqlOldTable)) {
            
            for (int itemId : starterItemIds) {
                // Insert into new table
                stmtNew.setInt(1, playerId);
                stmtNew.setInt(2, itemId);
                stmtNew.executeUpdate();
                
                // Also insert into old table for backward compatibility
                stmtOld.setInt(1, playerId);
                stmtOld.setInt(2, itemId);
                stmtOld.executeUpdate();
            }
        }
    }
    
    // Helper methods for player stats by class
    private int getPlayerHpByClass(String playerClass) {
        if ("Warrior".equalsIgnoreCase(playerClass)) return 120;
        if ("Mage".equalsIgnoreCase(playerClass)) return 80;
        if ("Rogue".equalsIgnoreCase(playerClass)) return 90;
        return 100; // Default
    }
    
    private int getPlayerSkillPointsByClass(String playerClass) {
        if ("Mage".equalsIgnoreCase(playerClass)) return 15;
        return 10; // Default and other classes
    }
    
    private double getPlayerDamageMultiByClass(String playerClass) {
        if ("Mage".equalsIgnoreCase(playerClass)) return 1.3;
        return 1.0; // Default and other classes
    }
    
    private int getPlayerAttackBoostByClass(String playerClass) {
        if ("Warrior".equalsIgnoreCase(playerClass)) return 5;
        if ("Rogue".equalsIgnoreCase(playerClass)) return 3;
        return 0; // Default and other classes
    }
    
    private int getPlayerDefenseBoostByClass(String playerClass) {
        if ("Rogue".equalsIgnoreCase(playerClass)) return 2;
        return 0; // Default and other classes
    }
    
    /**
     * Gets the default player for a user, typically their most recently saved player
     * @param userId The user ID
     * @return The default Player object or null if none exists
     */
    public Player getDefaultPlayerForUser(Integer userId) {
        Player player = null;
        
        try (Connection conn = db.getConnection()) {
            // First try to get the most recently saved game state for this user
            String sql = "SELECT player_id FROM GAME_STATE WHERE user_id = ? " +
                         "ORDER BY last_saved DESC FETCH FIRST 1 ROWS ONLY";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int playerId = rs.getInt("player_id");
                        // Load this player using existing method
                        player = loadPlayer(playerId);
                    } else {
                        // No saved game, check if user has any player created
                        sql = "SELECT player_id FROM PLAYER WHERE user_id = ? " +
                              "ORDER BY player_id DESC FETCH FIRST 1 ROWS ONLY";
                        
                        try (PreparedStatement stmt2 = conn.prepareStatement(sql)) {
                            stmt2.setInt(1, userId);
                            
                            try (ResultSet rs2 = stmt2.executeQuery()) {
                                if (rs2.next()) {
                                    int playerId = rs2.getInt("player_id");
                                    // Load this player
                                    player = loadPlayer(playerId);
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return player;
    }
    
    /**
     * Save player's companion to the database
     * @param conn Database connection
     * @param player Player whose companion to save
     */
    public void savePlayerCompanion(Connection conn, Player player) throws SQLException {
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
            String insertSql = "INSERT INTO COMPANION (companion_id, name, hp, aggression, damage, " +
                              "long_description, short_description, companion, room_num) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                insert.setInt(1, companionId);
                insert.setString(2, companion.getName());
                insert.setInt(3, companion.getHp());
                insert.setBoolean(4, false); // not aggressive
                insert.setInt(5, companion.getAttack());
                insert.setString(6, companion.getCharDescription());
                insert.setString(7, companion.getCharDescription()); // Use getCharDescription() again as fallback, since Companion doesn't have getShortDescription()
                insert.setBoolean(8, true); // is a companion
                insert.setInt(9, -1); // Not in a room
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
    
    /**
     * Save companion's inventory to the database
     * @param conn Database connection
     * @param companionId Companion ID
     * @param companion Companion object
     */
    private void saveCompanionInventory(Connection conn, int companionId, Companion companion) throws SQLException {
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
                    
                    // Get or create item ID
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
     * Get or create item ID in the database
     */
    private int getOrCreateItemId(Connection conn, Item item) throws SQLException {
        // First try to find the item by name
        int itemId = -1;
        try (PreparedStatement find = conn.prepareStatement(
                "SELECT item_id FROM ITEM WHERE name = ?")) {
            find.setString(1, item.getName());
            try (ResultSet rs = find.executeQuery()) {
                if (rs.next()) {
                    itemId = rs.getInt("item_id");
                }
            }
        }
        
        // If not found, create a new item
        if (itemId == -1) {
            // Get the next available item_id
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT MAX(item_id) + 1 AS next_id FROM ITEM")) {
                if (rs.next()) {
                    itemId = rs.getInt("next_id");
                    if (rs.wasNull()) {
                        itemId = 1;
                    }
                }
            }
            
            // Insert new item
            String insertSql = "INSERT INTO ITEM (item_id, name, value, weight, long_description, short_description) " +
                              "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                insert.setInt(1, itemId);
                insert.setString(2, item.getName());
                insert.setInt(3, item.getValue());
                insert.setInt(4, item.getWeight());
                insert.setString(5, item.getDescription());
                insert.setString(6, item.getShortDescription());
                insert.executeUpdate();
            }
        }
        
        return itemId;
    }
    
    /**
     * Save player state to database
     * @param player Player to save
     * @return true if successful, false otherwise
     */
    public boolean savePlayerState(Player player) {
        if (player == null) {
            return false;
        }
        
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            
            // Check if player has a state record already
            boolean hasState = false;
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM GAME_STATE WHERE player_id = ?")) {
                ps.setInt(1, player.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    hasState = rs.next();
                }
            }
            
            // If state exists, update it
            if (hasState) {
                String updateSql = "UPDATE GAME_STATE SET current_room = ?, player_hp = ?, " +
                                 "damage_multi = ?, attack_boost = ?, defense_boost = ?, running_message = ?, " +
                                 "last_saved = CURRENT_TIMESTAMP " +
                                 "WHERE player_id = ?";
                
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, player.getCurrentRoomNum() + 1);
                    ps.setInt(2, player.getHp());
                    ps.setDouble(3, player.getdamageMulti());
                    ps.setInt(4, player.getAttackBoost());
                    ps.setInt(5, player.getdefenseBoost());
                    ps.setString(6, player.getRunningMessage());
                    ps.setInt(7, player.getId());
                    
                    ps.executeUpdate();
                }
            } else {
                // Otherwise insert new state record
                String insertSql = "INSERT INTO GAME_STATE (user_id, player_id, current_room, " +
                                 "player_hp, damage_multi, attack_boost, defense_boost, running_message, " +
                                 "last_saved, save_name) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
                
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, player.getUserId());
                    ps.setInt(2, player.getId());
                    ps.setInt(3, player.getCurrentRoomNum() + 1);
                    ps.setInt(4, player.getHp());
                    ps.setDouble(5, player.getdamageMulti());
                    ps.setInt(6, player.getAttackBoost());
                    ps.setInt(7, player.getdefenseBoost());
                    ps.setString(8, player.getRunningMessage());
                    ps.setString(9, "Autosave");
                    
                    ps.executeUpdate();
                }
            }
            
            // Save inventory items
            savePlayerInventory(conn, player);
            
            // Save equipment
            savePlayerEquipment(conn, player);
            
            // Save player's companion
            savePlayerCompanion(conn, player);
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Save player inventory to database
     * @param conn Database connection
     * @param player Player to save inventory for
     */
    private void savePlayerInventory(Connection conn, Player player) throws SQLException {
        // First, delete current inventory
        try (PreparedStatement delete = conn.prepareStatement(
                "DELETE FROM player_items WHERE player_id = ?");
             PreparedStatement deleteOld = conn.prepareStatement(
                "DELETE FROM PLAYER_INVENTORY WHERE player_id = ?")) {
            
            delete.setInt(1, player.getId());
            delete.executeUpdate();
            
            deleteOld.setInt(1, player.getId());
            deleteOld.executeUpdate();
        }
        
        // Then insert current items
        if (player.getInventorySize() > 0) {
            String insertSql = "INSERT INTO player_items (player_id, item_id) VALUES (?, ?)";
            String insertOldSql = "INSERT INTO PLAYER_INVENTORY (player_id, item_id) VALUES (?, ?)";
            
            try (PreparedStatement insert = conn.prepareStatement(insertSql);
                 PreparedStatement insertOld = conn.prepareStatement(insertOldSql)) {
                
                for (int i = 0; i < player.getInventorySize(); i++) {
                    Item item = player.getItem(i);
                    int itemId = getOrCreateItemId(conn, item);
                    
                    // Insert into new schema
                    insert.setInt(1, player.getId());
                    insert.setInt(2, itemId);
                    insert.addBatch();
                    
                    // Insert into old schema for compatibility
                    insertOld.setInt(1, player.getId());
                    insertOld.setInt(2, itemId);
                    insertOld.addBatch();
                }
                
                insert.executeBatch();
                insertOld.executeBatch();
            }
        }
    }
    
    /**
     * Save player equipment to database
     * @param conn Database connection
     * @param player Player to save equipment for
     */
    private void savePlayerEquipment(Connection conn, Player player) throws SQLException {
        // First, delete current equipment
        try (PreparedStatement delete = conn.prepareStatement(
                "DELETE FROM PLAYER_EQUIPMENT WHERE player_id = ?")) {
            delete.setInt(1, player.getId());
            delete.executeUpdate();
        }
        
        // Then insert current equipment
        for (ArmorSlot slot : ArmorSlot.values()) {
            Armor armor = player.getEquippedArmor(slot);
            if (armor != null) {
                // Get or create item ID
                int itemId = getOrCreateItemId(conn, armor);
                
                // Insert equipment record
                String insertSql = "INSERT INTO PLAYER_EQUIPMENT (player_id, armor_id, slot) VALUES (?, ?, ?)";
                try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                    insert.setInt(1, player.getId());
                    insert.setInt(2, itemId);
                    insert.setString(3, slot.name());
                    insert.executeUpdate();
                }
            }
        }
    }
    
    /**
     * Represents a game save state
     */
    public static class GameState {
        private int stateId;
        private int playerId;
        private int currentRoom;
        private int playerHp;
        private String saveName;
        
        public int getStateId() { return stateId; }
        public void setStateId(int stateId) { this.stateId = stateId; }
        
        public int getPlayerId() { return playerId; }
        public void setPlayerId(int playerId) { this.playerId = playerId; }
        
        public int getCurrentRoom() { return currentRoom; }
        public void setCurrentRoom(int currentRoom) { this.currentRoom = currentRoom; }
        
        public int getPlayerHp() { return playerHp; }
        public void setPlayerHp(int playerHp) { this.playerHp = playerHp; }
        
        public String getSaveName() { return saveName; }
        public void setSaveName(String saveName) { this.saveName = saveName; }
        
        public boolean isEmpty() {
            // A GameState is considered empty if it has no playerId or no valid player
            return playerId <= 0;
        }
    }
} 