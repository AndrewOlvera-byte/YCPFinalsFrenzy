package GameEngine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
                        
                        // Create player
                        player = new Player(
                            rs.getString("name"),
                            rs.getInt("hp"),
                            rs.getInt("skill_points"),
                            inventory,
                            rs.getString("long_description"),
                            rs.getString("short_description"),
                            rs.getDouble("damage_multi"),
                            rs.getInt("attack_boost"),
                            rs.getInt("defense_boost")
                        );
                        player.setId(playerId);
                        player.setUserId(rs.getInt("user_id"));
                        
                        // Set player type from database
                        player.setPlayerType(rs.getString("player_type"));
                        
                        // Load companion if exists
                        Companion companion = loadPlayerCompanion(conn, playerId);
                        player.setPlayerCompanion(companion);
                        
                        // Load player game state
                        loadPlayerGameState(conn, player);
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
        String sql = "SELECT current_room, running_message FROM GAME_STATE WHERE player_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, player.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Store room number and running message in player object
                    player.setCurrentRoomNum(rs.getInt("current_room") - 1); // -1 to match array index
                    player.setRunningMessage(rs.getString("running_message"));
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
        // For now, just return null as companion loading needs more work
        // This will be implemented properly once we understand the Companion class better
        return null;
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
        String sql = "SELECT i.item_id, i.name, i.value, i.weight, i.long_description, " +
                     "i.short_description, i.type, i.healing, i.damage_multi, i.attack_damage, " +
                     "i.attack_boost, i.defense_boost, i.slot, i.disassemblable " +
                     "FROM PLAYER_INVENTORY pi " +
                     "JOIN ITEM i ON pi.item_id = i.item_id " +
                     "WHERE pi.player_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            
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
        
        // Add items to player inventory
        String sql = "INSERT INTO PLAYER_INVENTORY (player_id, item_id) VALUES (?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int itemId : starterItemIds) {
                stmt.setInt(1, playerId);
                stmt.setInt(2, itemId);
                stmt.executeUpdate();
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