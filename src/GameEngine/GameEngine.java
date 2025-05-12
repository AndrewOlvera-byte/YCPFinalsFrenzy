package GameEngine;

import java.util.*;
import models.*;
import models.Character;  // your NPC base
import java.sql.*;
import models.CraftingManager;
import java.sql.Connection;
import java.sql.Statement;
import models.DatabaseMigration;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class GameEngine {
	
    private static final boolean USE_FAKE_DB = false;
    
    // Add singleton instance
    private static GameEngine instance;
    
    /**
     * Get the singleton instance of the GameEngine
     * @return GameEngine instance
     */
    public static GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }
    
    private String[] tables = {"conversation_edges", "conversation_nodes", "GAME_STATE", "PLAYER_INVENTORY", "NPC_INVENTORY",
    		"ROOM_INVENTORY", "NPC_ROOM", "ROOM_CONNECTIONS", "ITEM_COMPONENT", "COMPANION_ROOM", "player_quests", "quest_definition", "PLAYER_COMPANION", "COMPANION_INVENTORY", "COMPANION", "NPC", "ROOM", "ITEM", "PLAYER", "users"};

    // Change from single player to ArrayList of players
    private ArrayList<Player> players = new ArrayList<>();
    private boolean isRunning = false;
    private int currentRoomNum;
    private ArrayList<Room> rooms = new ArrayList<>();
    private String runningMessage = "";
    private String error = "";
    private GameInputHandler inputHandler;
    
    // Managers
    private RoomManager roomManager;
    private PlayerManager playerManager;
    private CombatManager combatManager;
    private InventoryManager inventoryManager;
    private UIManager uiManager;
    private ConversationManager conversationManager;
    private CompanionManager companionManager;
    private CraftingManager craftingManager;
    private QuestManager questManager;

    
    // CSV-fake loader
    private FakeGameDatabase fakeDb = new FakeGameDatabase();

    public GameEngine() {
        this.inputHandler = new GameInputHandler(this);
        this.roomManager = new RoomManager(this);
        this.playerManager = new PlayerManager(this);
        this.combatManager = new CombatManager(this);
        this.inventoryManager = new InventoryManager(this);
        this.uiManager = new UIManager(this);
        this.conversationManager = new ConversationManager(this);
        this.companionManager = new CompanionManager(this);
        this.craftingManager = new CraftingManager(this);
        this.questManager = new QuestManager();
        this.questManager.loadAll();
        this.questManager = new QuestManager();
        this.questManager.loadAll();
    }

    /** Called once to seed/initialize, then loadData. */
    public void start() {
        if (!USE_FAKE_DB) {
            // First run database migration if needed
            DatabaseMigration.migrateToNewSchema();
            
            // Initialize database if needed
            DatabaseInitializer.initialize();
            
            // ALWAYS reinitialize rooms from CSV files on server start
            // This ensures rooms are in their original state for a true MMO experience
            // where all players see the same original room state with all items
            reinitializeRoomsFromCSV();
            System.out.println("Rooms reinitialized from CSV files for MMO functionality");
        }
        loadData();
        // Auto-trigger ON_ENTER quests upon spawning into the starting room
        String qMsg = questManager.checkAndAccept(this, models.QuestDefinition.Trigger.ON_ENTER, getCurrentRoomName());
        if (qMsg != null) {
            appendMessage(qMsg);
        }
        this.isRunning = true;
    }
    
    /**
     * Reinitialize rooms from CSV files
     * This ensures the map is in its original state on server restart
     * while preserving player data
     */
    private void reinitializeRoomsFromCSV() {
        try (Connection conn = DerbyDatabase.getConnection()) {
            System.out.println("Starting room reinitialization from CSV files...");
            
            // STEP 1: Backup all dynamic state that needs to be preserved
            System.out.println("Backing up dynamic state before reinitialization...");
            
            // 1.1: Backup player-companion relationships and companion inventories
            Map<Integer, Map<String, Object>> playerCompanions = new HashMap<>();
            
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT pc.player_id, pc.companion_id, c.name AS companion_name " +
                    "FROM PLAYER_COMPANION pc " +
                    "JOIN COMPANION c ON pc.companion_id = c.companion_id")) {
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int playerId = rs.getInt("player_id");
                        int companionId = rs.getInt("companion_id");
                        String companionName = rs.getString("companion_name");
                        
                        Map<String, Object> companionInfo = new HashMap<>();
                        companionInfo.put("id", companionId);
                        companionInfo.put("name", companionName);
                        
                        // Get companion inventory items
                        List<Integer> itemIds = new ArrayList<>();
                        try (PreparedStatement psItems = conn.prepareStatement(
                                "SELECT item_id FROM COMPANION_INVENTORY WHERE companion_id = ?")) {
                            psItems.setInt(1, companionId);
                            try (ResultSet rsItems = psItems.executeQuery()) {
                                while (rsItems.next()) {
                                    itemIds.add(rsItems.getInt("item_id"));
                                }
                            }
                        }
                        companionInfo.put("items", itemIds);
                        
                        playerCompanions.put(playerId, companionInfo);
                    }
                }
            }
            
            System.out.println("Backed up " + playerCompanions.size() + " player-companion relationships");
            
            // 1.2: Backup custom room connections that should be preserved
            Map<Integer, Map<String, Integer>> customRoomConnections = new HashMap<>();
            
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT from_room_id, direction, to_room_id FROM ROOM_CONNECTIONS")) {
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int fromRoomId = rs.getInt("from_room_id");
                        String direction = rs.getString("direction");
                        int toRoomId = rs.getInt("to_room_id");
                        
                        customRoomConnections
                            .computeIfAbsent(fromRoomId, k -> new HashMap<>())
                            .put(direction, toRoomId);
                    }
                }
            }
            
            System.out.println("Backed up " + customRoomConnections.size() + " rooms with custom connections");
            
            // 1.3: Backup character container state (NPCs in rooms)
            Map<Integer, List<Integer>> roomNpcs = new HashMap<>();
            
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT room_id, npc_id FROM NPC_ROOM")) {
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int roomId = rs.getInt("room_id");
                        int npcId = rs.getInt("npc_id");
                        
                        roomNpcs.computeIfAbsent(roomId, k -> new ArrayList<>())
                               .add(npcId);
                    }
                }
            }
            
            System.out.println("Backed up " + roomNpcs.size() + " rooms with NPCs");
            
            // STEP 2: Delete junction tables (many-to-many relationships)
            // Important: Must delete in correct order to avoid foreign key constraint violations
            String[] junctionTables = {
                "PLAYER_COMPANION",     // Must delete this first to break references to COMPANION
                "COMPANION_INVENTORY",  // Then delete COMPANION references
                "COMPANION_ROOM",       // Then delete COMPANION references
                "ROOM_INVENTORY",       // Then delete ROOM references
                "NPC_ROOM",             // Then delete NPC references
                "NPC_INVENTORY",        // Then delete NPC references
                "ROOM_CONNECTIONS"      // Then delete ROOM connections
            };
            
            try (Statement stmt = conn.createStatement()) {
                for (String table : junctionTables) {
                    try {
                        int deleted = stmt.executeUpdate("DELETE FROM " + table);
                        System.out.println("Deleted " + deleted + " rows from " + table);
                    } catch (SQLException e) {
                        System.err.println("Warning: Failed to delete from " + table + ": " + e.getMessage());
                        // Continue with other tables rather than failing the entire process
                    }
                }
                
                // Then delete from entity tables (in correct order to respect foreign keys)
                String[] entityTables = {
                    "COMPANION", "NPC", "ROOM"
                };
                
                for (String table : entityTables) {
                    try {
                        int deleted = stmt.executeUpdate("DELETE FROM " + table);
                        System.out.println("Deleted " + deleted + " rows from " + table);
                    } catch (SQLException e) {
                        System.err.println("Warning: Failed to delete from " + table + ": " + e.getMessage());
                        // Continue with other tables rather than failing the entire process
                        
                        // If we hit a constraint violation, attempt to identify the constraint
                        if (e.getMessage().contains("constraint violation")) {
                            try (ResultSet rs = stmt.executeQuery(
                                "SELECT CONSTRAINTNAME, TABLENAME FROM SYS.SYSCONSTRAINTS C " +
                                "JOIN SYS.SYSTABLES T ON C.TABLEID = T.TABLEID " +
                                "WHERE TABLENAME = '" + table + "'")) {
                                while (rs.next()) {
                                    System.out.println("Constraint on " + table + ": " + rs.getString("CONSTRAINTNAME"));
                                }
                            } catch (Exception e2) {
                                // Ignore
                            }
                        }
                    }
                }
            }
            
            // STEP 3: Reseed from CSV files
            System.out.println("Reseeding room data from CSV files...");
            DatabaseInitializer.seedRoomsFromCSV(conn);
            
            // STEP 4: Restore dynamic state
            System.out.println("Restoring preserved state...");
            
            // 4.1: Find and restore player companions with their inventories
            for (Map.Entry<Integer, Map<String, Object>> entry : playerCompanions.entrySet()) {
                int playerId = entry.getKey();
                Map<String, Object> companionInfo = entry.getValue();
                String companionName = (String) companionInfo.get("name");
                
                // Find the new companion ID (may have changed after CSV reload)
                int newCompanionId = -1;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT companion_id FROM COMPANION WHERE name = ?")) {
                    ps.setString(1, companionName);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            newCompanionId = rs.getInt("companion_id");
                        }
                    }
                }
                
                if (newCompanionId > 0) {
                    // Create player-companion link
                    try (PreparedStatement insert = conn.prepareStatement(
                            "INSERT INTO PLAYER_COMPANION (player_id, companion_id) VALUES (?, ?)")) {
                        insert.setInt(1, playerId);
                        insert.setInt(2, newCompanionId);
                        insert.executeUpdate();
                    }
                    
                    // Restore companion inventory items
                    @SuppressWarnings("unchecked")
                    List<Integer> itemIds = (List<Integer>) companionInfo.get("items");
                    if (itemIds != null && !itemIds.isEmpty()) {
                        try (PreparedStatement insert = conn.prepareStatement(
                                "INSERT INTO COMPANION_INVENTORY (companion_id, item_id) VALUES (?, ?)")) {
                            for (Integer itemId : itemIds) {
                                insert.setInt(1, newCompanionId);
                                insert.setInt(2, itemId);
                                insert.addBatch();
                            }
                            int[] results = insert.executeBatch();
                            System.out.println("Restored " + results.length + " items to companion " + companionName);
                        }
                    }
                }
            }
            
            // 4.2: Overlay custom room connections on top of standard ones from CSV
            for (Map.Entry<Integer, Map<String, Integer>> entry : customRoomConnections.entrySet()) {
                int fromRoomId = entry.getKey();
                Map<String, Integer> connections = entry.getValue();
                
                for (Map.Entry<String, Integer> connection : connections.entrySet()) {
                    String direction = connection.getKey();
                    int toRoomId = connection.getValue();
                    
                    // First check if a connection already exists in this direction from CSV data
                    boolean connectionExists = false;
                    try (PreparedStatement check = conn.prepareStatement(
                            "SELECT 1 FROM ROOM_CONNECTIONS WHERE from_room_id = ? AND direction = ?")) {
                        check.setInt(1, fromRoomId);
                        check.setString(2, direction);
                        connectionExists = check.executeQuery().next();
                    }
                    
                    // If no connection exists or this is a custom connection that should override,
                    // either insert or update the connection
                    if (!connectionExists) {
                        try (PreparedStatement insert = conn.prepareStatement(
                                "INSERT INTO ROOM_CONNECTIONS (from_room_id, direction, to_room_id) VALUES (?, ?, ?)")) {
                            insert.setInt(1, fromRoomId);
                            insert.setString(2, direction);
                            insert.setInt(3, toRoomId);
                            insert.executeUpdate();
                        }
                    }
                }
            }
            
            // 4.3: Restore NPCs to character containers
            for (Map.Entry<Integer, List<Integer>> entry : roomNpcs.entrySet()) {
                int roomId = entry.getKey();
                List<Integer> npcIds = entry.getValue();
                
                // For each NPC that was in this room, find its new ID after CSV reload
                for (Integer oldNpcId : npcIds) {
                    // We need to find the NPC by its properties since IDs might have changed
                    // This is simplified - in a real implementation you might need to match by name
                    // or other properties to find the corresponding NPC after reload
                    try (PreparedStatement insert = conn.prepareStatement(
                            "INSERT INTO NPC_ROOM (npc_id, room_id) VALUES (?, ?)")) {
                        insert.setInt(1, oldNpcId); // Assuming NPC IDs remain stable after reload
                        insert.setInt(2, roomId);
                        insert.executeUpdate();
                    } catch (SQLException e) {
                        // If this fails, the NPC might not exist after reload
                        System.out.println("Could not restore NPC " + oldNpcId + " to room " + roomId);
                    }
                }
            }
            
            // Verify that room data was loaded correctly
            try (Statement stmt = conn.createStatement()) {
                // Check room count
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ROOM")) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("Loaded " + count + " rooms from CSV");
                    }
                }
                
                // Check NPC count
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM NPC")) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("Loaded " + count + " NPCs from CSV");
                    }
                }
                
                // Check companion count
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM COMPANION")) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("Loaded " + count + " companions from CSV");
                    }
                }
                
                // Check room inventory
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ROOM_INVENTORY")) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("Loaded " + count + " items into room inventory from CSV");
                    }
                }
                
                // Check connections
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ROOM_CONNECTIONS")) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("Loaded " + count + " room connections from CSV");
                    }
                }
                
                // Check NPCs in rooms
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM NPC_ROOM")) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("Loaded " + count + " NPCs in rooms from CSV");
                    }
                }
                
                // Check restored player companions
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM PLAYER_COMPANION")) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("Restored " + count + " player-companion relationships");
                    }
                }
                
                // Check companion inventory
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM COMPANION_INVENTORY")) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("Found " + count + " items in companion inventory after restoration");
                    }
                }
            }
            
            System.out.println("Room reinitialization from CSV completed successfully with state preservation");
        } catch (Exception e) {
            System.err.println("Failed to reinitialize rooms from CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ensure player companions are properly added to the correct room
     * This resolves any issues where companions might not be in rooms after server restart
     */
    private void syncPlayerCompanionsWithRooms() {
        if (players.isEmpty()) {
            return;
        }
        
        System.out.println("Synchronizing player companions with room containers...");
        int syncCount = 0;
        
        for (Player player : players) {
            Companion companion = player.getPlayerCompanion();
            if (companion == null) {
                continue;
            }
            
            // The companion should be in the same room as the player
            int playerRoomNum = player.getCurrentRoomNum();
            
            // Skip if invalid room number
            if (playerRoomNum < 0 || playerRoomNum >= rooms.size()) {
                System.out.println("Player " + player.getName() + " has invalid room number: " + playerRoomNum);
                continue;
            }
            
            Room playerRoom = rooms.get(playerRoomNum);
            
            // Check if companion is already in the room
            boolean companionInRoom = false;
            for (Companion existingCompanion : playerRoom.getCompanionContainer()) {
                if (existingCompanion.getName().equals(companion.getName())) {
                    companionInRoom = true;
                    break;
                }
            }
            
            // If not in room, add the companion to the room
            if (!companionInRoom) {
                playerRoom.addCompanion(companion);
                syncCount++;
                
                // Also update the database
                try (Connection conn = DerbyDatabase.getConnection()) {
                    // First find the companion ID
                    int companionId = -1;
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT c.companion_id FROM COMPANION c " +
                            "JOIN PLAYER_COMPANION pc ON c.companion_id = pc.companion_id " +
                            "WHERE pc.player_id = ?")) {
                        ps.setInt(1, player.getId());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                companionId = rs.getInt("companion_id");
                            }
                        }
                    }
                    
                    if (companionId > 0) {
                        // Check if already in COMPANION_ROOM
                        boolean alreadyInRoom = false;
                        try (PreparedStatement ps = conn.prepareStatement(
                                "SELECT 1 FROM COMPANION_ROOM WHERE companion_id = ? AND room_id = ?")) {
                            ps.setInt(1, companionId);
                            ps.setInt(2, playerRoomNum + 1); // Adjust for 1-based room IDs in DB
                            alreadyInRoom = ps.executeQuery().next();
                        }
                        
                        // Insert if not already in room
                        if (!alreadyInRoom) {
                            try (PreparedStatement ps = conn.prepareStatement(
                                    "INSERT INTO COMPANION_ROOM (companion_id, room_id) VALUES (?, ?)")) {
                                ps.setInt(1, companionId);
                                ps.setInt(2, playerRoomNum + 1); // Adjust for 1-based room IDs in DB
                                ps.executeUpdate();
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error updating companion room in database: " + e.getMessage());
                }
            }
        }
        
        System.out.println("Synchronized " + syncCount + " companions to their player's rooms");
    }

    /** Load rooms + player either from Derby or from CSV (FakeGameDatabase). */
    public void loadData() {
        if (USE_FAKE_DB) {
            // 1) Rooms + NPCs
            this.rooms.clear();
            this.rooms.addAll(fakeDb.loadAllRooms());

            // 2) Player + inventory
            Player player = fakeDb.loadPlayer();
            if (players.isEmpty()) {
                players.add(player);
            } else {
                players.set(0, player);
            }
            this.currentRoomNum = 0;  // start in room #1

            // 3) Conversations can remain empty or implement a fake loader later
            this.conversationManager.setConversations(new HashMap<>());
        }
        else {
            // real Derby path
            
            // For MMO functionality, we need to load rooms directly from the DB
            // which was just reinitialized from CSV files in start()
            roomManager.loadRooms();
            
            // Load conversations into NPCs
            conversationManager.loadConversations();
            for (Room room : getRooms()) {
                for (Character npc : room.getCharacterContainer()) {
                    ConversationTree tree = conversationManager.getConversation(npc.getName());
                    System.out.println("DEBUG: NPC '" + npc.getName() + "' has conversationTree? " + (tree != null));
                    if (tree != null) {
                        if (npc instanceof NPC) {
                            ((NPC) npc).addConversationTree(tree);
                        }
                    }
                }
            }
            
            // Load player data - this doesn't affect room state
            playerManager.loadPlayer();
            
            // Ensure player equipment is properly loaded
            try (Connection conn = DerbyDatabase.getConnection()) {
                for (Player player : players) {
                    GameStateManager.loadPlayerEquipment(conn, player);
                }
            } catch (SQLException e) {
                System.err.println("Failed to load player equipment: " + e.getMessage());
            }
            
            if (!players.isEmpty()) {
                GameStateManager.loadState(this);
                
                // Update currentRoomNum based on player's room
                if (getPlayer() != null && getPlayer().getCurrentRoomNum() >= 0) {
                    this.currentRoomNum = getPlayer().getCurrentRoomNum();
                } else {
                    this.currentRoomNum = 0; // Default to room 0 if player room is not set
                }
                
                // Ensure companions are in the correct rooms
                syncPlayerCompanionsWithRooms();
            }
        }
    }
    
    /** Like start(), but only load rooms & conversations so we can inject our own Player. */
    public void startWithoutPlayer() {
        if (!USE_FAKE_DB) {
            DatabaseInitializer.initialize();
            
            // Also reinitialize rooms from CSV in this path
            reinitializeRoomsFromCSV();
            System.out.println("Rooms reinitialized from CSV for startWithoutPlayer");
        }
        // 1) Rooms
        roomManager.loadRooms();
        // 2) Conversations
        conversationManager.loadConversations();
        // Attach conversation trees to NPCs when starting without player
        for (Room room : getRooms()) {
            for (Character npcChar : room.getCharacterContainer()) {
                if (npcChar instanceof NPC) {
                    NPC npc = (NPC) npcChar;
                    ConversationTree tree = conversationManager.getConversation(npc.getName());
                    if (tree != null) {
                        npc.addConversationTree(tree);
                    }
                }
            }
        }
        this.currentRoomNum = 0;
        this.isRunning = true;
    }

    /** After startWithoutPlayer(), load exactly the class they picked. */
    public void loadPlayerOfClass(String className) {
    	try {
    		new PlayerManager(this).loadPlayerByType(className);
    	       } catch (SQLException e) {
    	           throw new RuntimeException("Failed to load player of class: " + className, e);
    	       }
    }
    
    // Getters and setters for managers to access GameEngine state
    // For backward compatibility, get the first player by default
    public Player getPlayer() {
        if (players.isEmpty()) {
            return null;
        }
        return players.get(0);
    }
    
    // Get player by ID
    public Player getPlayerById(int playerId) {
        if (playerId >= 0 && playerId < players.size()) {
            return players.get(playerId);
        }
        return null;
    }
    
    // Set the player at the specified index
    public void setPlayer(Player player) {
        if (players.isEmpty()) {
            players.add(player);
        } else {
            players.set(0, player);
        }
    }
    
    // Add a player and return its index
    public int addPlayer(Player player) {
        // Check if player with this database ID already exists
        int existingIndex = findPlayerIndexById(player.getId());
        
        if (existingIndex >= 0) {
            // Player already exists, update it instead of adding new
            players.set(existingIndex, player);
            return existingIndex;
        } else {
            // Add new player
            players.add(player);
            return players.size() - 1;
        }
    }
    
    // Find player by database ID
    public int findPlayerIndexById(int databaseId) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId() == databaseId) {
                return i;
            }
        }
        return -1; // not found
    }
    
    /**
     * Get a player by database ID rather than array index
     * @param dbId The database ID of the player
     * @return The player with the matching ID, or null if not found
     */
    public Player getPlayerByDatabaseId(int dbId) {
        for (Player player : players) {
            if (player.getId() == dbId) {
                return player;
            }
        }
        return null;
    }
    
    public ArrayList<Player> getPlayers() {
        return players;
    }
    
    public ArrayList<Room> getRooms() {
        return rooms;
    }
    
    public int getCurrentRoomNum() {
        return this.currentRoomNum;
    }
    
    public void setCurrentRoomNum(int roomNum) {
        this.currentRoomNum = roomNum;
    }
    
    public String getRunningMessage() {
        return runningMessage;
    }
    
    public void setRunningMessage(String message) {
        this.runningMessage = message;
    }
    
    public void appendMessage(String message) { 
        this.runningMessage += message;
    }
    
    // Core navigation method - delegates to RoomManager
    public String updateCurrentRoom(String direction) {
        return roomManager.updateCurrentRoom(direction);
    }
    
    // Helper methods that delegate to managers
    public int getMapOutput(String direction) {
        return roomManager.getMapOutput(direction);
    }
    
    public String getCurrentRoomName() {
        return roomManager.getCurrentRoomName();
    }
    
    public String getRoomName(int roomNum) {
        return roomManager.getRoomName(roomNum);
    }
    
    // Movement commands - delegate to RoomManager
    public String getGo(String noun) {
        // Move to the next room and capture message
        String message = roomManager.getGo(noun);
        // Auto-accept any ON_ENTER quests for this room
        String roomName = getCurrentRoomName();
        String qMsg = questManager.checkAndAccept(this, QuestDefinition.Trigger.ON_ENTER, roomName);
        if (qMsg != null) {
            message += qMsg;
        }
        return message;
    }
    
    public String getOnShuttle() {
        return roomManager.getOnShuttle();
    }
    
    //Companion Commands
    public String chooseCompanion(int companionID) {
    	return companionManager.chooseCompanion(companionID);
    }
    
    public String shooCompanion(int companionID) {
    	return companionManager.shooCompanion(companionID);
    }
     
    // Combat commands - delegate to CombatManager
    public String playerAttackChar(int itemNum, int characterNum) {
        return combatManager.playerAttackChar(itemNum, characterNum);
    }
    
    // Inventory and item commands - delegate to InventoryManager
    public String pickupItem(int itemNum) {
        return inventoryManager.pickupItem(itemNum);
    }
    
    public String pickupItemFromCompanion(int itemNum) {
        return inventoryManager.pickupItemFromCompanion(itemNum);
    }
    
    public String dropItem(int itemNum) {
        return inventoryManager.dropItem(itemNum);
    }
    
    public String giveItemToCompanion(int itemNum) {
        return inventoryManager.giveItemToCompanion(itemNum);
    }
    
    public String usePotion(int itemNum) {
        return inventoryManager.usePotion(itemNum);
    }
    public String equipArmor(int itemNum) {
        return inventoryManager.equipArmor(itemNum);
    }
    public String unequipArmor(String name) {
        return inventoryManager.unequipArmor(name);
    }
    public String examineItemName(int itemNum) {
        return inventoryManager.examineItemName(itemNum);
    }
    
    // Helper methods to convert names to IDs - delegate to appropriate managers
    public int RoomItemNameToID(String name) {
        return inventoryManager.RoomItemNameToID(name);
    }
    
    public int CompanionItemNameToID(String name) {
        return inventoryManager.CompanionItemNameToID(name);
    }
    
    public int CharItemNameToID(String name) {
        return inventoryManager.CharItemNameToID(name);
    }
    
    public int CharNameToID(String name) {
        return roomManager.CharNameToID(name);
    }
    
    // Examination commands
    public String examineCharacter(int charNum) {
        return roomManager.examineCharacter(charNum);
    }
    
    public String examineCompanion(String name) {
    	return companionManager.examineCompanion(name);
    }
    
    public int RoomCompanionNameToID(String name) {
        return companionManager.RoomCompanionNameToID(name);
    }
    
    public int playerCompanionNameToID(String name) {
    	return companionManager.playerCompanionNameToID(name);
    }
    
    public int companionNameToID(String name) {
    	return roomManager.CompanionNameToID(name);
    }
    
    public String getExamine(String noun) {
        String message = "";
        int itemNum = CharItemNameToID(noun);
        int charNum = CharNameToID(noun);
        int companionNum = companionNameToID(noun);
        
        if(noun.toLowerCase().equals("room")) {
            message = "\n" + roomManager.getCurrentRoom().getRoomDescription();
        }
        if(charNum >= 0) {
            message = examineCharacter(charNum);
        }
        if(itemNum >= 0) {
            message = examineItemName(itemNum);
        }
        if(companionNum >= 0) {
        	message = examineCompanion(noun);
        }
        // Trigger any quests tied to examining this noun
        String qMsg = questManager.checkAndAccept(this,
                          QuestDefinition.Trigger.ON_EXAMINE,
                          noun);
        if (qMsg != null) {
            message += qMsg;
        }
        return message;
    }
    
    // NPC interaction methods - delegate to RoomManager
    public String talkToNPC(int characterNum) {
        // Delegate to roomManager then handle any ON_TALK quests
        String msg = roomManager.talkToNPC(characterNum);
        // Auto-accept any ON_TALK quests for this NPC
        String npcName = rooms.get(currentRoomNum).getCharacterName(characterNum);
        String qMsg = questManager.checkAndAccept(this, QuestDefinition.Trigger.ON_TALK, npcName);
        if (qMsg != null) {
            msg += qMsg;
        }
        return msg;
    }
    
    public String[] getResponseOptions(int characterNum) {
        return roomManager.getResponseOptions(characterNum);
    }
    
    public String interactWithNPC(String choice, int characterNum) {
        return roomManager.interactWithNPC(choice, characterNum);
    }
    
    public boolean reachedFinalNode() {
        return true;
    }
    
    // Help command
    public String getHelp() {
        return uiManager.getHelp();
    }
    
    // Input processing - delegates to InputHandler
    public boolean processInput(String input) {
        return inputHandler.processInput(input);
    }
    
    // Updated to support player_id
    public boolean processInput(String input, int playerId) {
        return inputHandler.processInput(input, playerId);
    }
    
    // UI/Display methods - delegate to UIManager
    public String getCurrentRoomItems() {
        return uiManager.getCurrentRoomItems();
    }
    
    public String getPlayerInventoryString() {
        return uiManager.getPlayerInventoryString();
    }
    
    public String getPlayerInfo() {
        return uiManager.getPlayerInfo();
    }
    
    public String getRoomCharactersInfo() {
        return uiManager.getRoomCharactersInfo();
    }
    
    public String getRoomConnectionOutput() {
        return uiManager.getRoomConnectionOutput();
    }
    
    public String getCurrentRoomImage() {
        return uiManager.getCurrentRoomImage();
    }
    
    public String getRoomItemsOverlay() {
        return uiManager.getRoomItemsOverlay();
    }
    
    public String getRoomCharactersOverlay() {
        return uiManager.getRoomCharactersOverlay();
    }
    
    public String getCurrentRoomNumber() {
        return String.valueOf(currentRoomNum + 1);
    }
    
    // conversation manager if needed
    public ConversationManager getConversationManager() {
        return conversationManager;
    }
    
    
    // Main display method for constructing Response
    public Response display() {
        Player player = getPlayer();
        // if the player is dead, return a special "game over" response
        if (player != null && player.getHp() <= 0) {
            Response resp = new Response();
            resp.setGameOver(true);
            // relative to your webapp root—adjust if needed
            resp.setGameOverImage("/images/GameOver.png");
            return resp;
        }
        // otherwise, fall back to the normal UIManager flow
        return uiManager.display();
    }
    
    // Display for a specific player
    public Response display(int playerId) {
        Player player = getPlayerById(playerId);
        // if the player is dead, return a special "game over" response
        if (player != null && player.getHp() <= 0) {
            Response resp = new Response();
            resp.setGameOver(true);
            // relative to your webapp root—adjust if needed
            resp.setGameOverImage("/images/GameOver.png");
            return resp;
        }
        
        // Use player-specific room number if available
        int roomToDisplay = player.getCurrentRoomNum() >= 0 ? 
                           player.getCurrentRoomNum() : this.currentRoomNum;
        
        // Temporarily set the current room for UI methods
        int originalRoomNum = this.currentRoomNum;
        this.currentRoomNum = roomToDisplay;
        
        // Use player-specific running message if available
        String originalMessage = this.runningMessage;
        if (player.getRunningMessage() != null && !player.getRunningMessage().isEmpty()) {
            this.runningMessage = player.getRunningMessage();
        }
        
        // Get the response from UIManager
        Response response = uiManager.display(playerId);
        
        // Restore original state
        this.currentRoomNum = originalRoomNum;
        this.runningMessage = originalMessage;
        
        return response;
    }
    
    /**
     * Save the state of all companions in the game
     * This ensures companions in rooms and with players have their state persisted
     */
    private void saveAllCompanionsState() {
        System.out.println("Saving state of all companions...");
        try (Connection conn = DerbyDatabase.getConnection()) {
            int savedCompanions = 0;
            
            // First save player companions
            for (Player player : players) {
                Companion companion = player.getPlayerCompanion();
                if (companion != null) {
                    // Use the PlayerLoadManager's savePlayerCompanion method
                    try {
                        new PlayerLoadManager().savePlayerCompanion(conn, player);
                        savedCompanions++;
                    } catch (SQLException e) {
                        System.err.println("Failed to save player companion for " + player.getName() + ": " + e.getMessage());
                    }
                }
            }
            
            // Then save all companions in rooms
            for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++) {
                Room room = rooms.get(roomIndex);
                int roomId = roomIndex + 1; // Convert to 1-based room ID for DB
                
                for (Companion companion : room.getCompanionContainer()) {
                    // Skip companions that are already saved as player companions
                    boolean isPlayerCompanion = false;
                    for (Player player : players) {
                        if (player.getPlayerCompanion() != null && 
                            player.getPlayerCompanion().getName().equals(companion.getName())) {
                            isPlayerCompanion = true;
                            break;
                        }
                    }
                    
                    if (!isPlayerCompanion) {
                        // Find or create companion record
                        int companionId = -1;
                        try (PreparedStatement ps = conn.prepareStatement(
                                "SELECT companion_id FROM COMPANION WHERE name = ?")) {
                            ps.setString(1, companion.getName());
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    companionId = rs.getInt("companion_id");
                                }
                            }
                        }
                        
                        if (companionId == -1) {
                            // Create new companion record
                            try (PreparedStatement ps = conn.prepareStatement(
                                    "INSERT INTO COMPANION (companion_id, name, hp, aggression, damage, " +
                                    "long_description, short_description, companion, room_num) " +
                                    "VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?)", 
                                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                                ps.setString(1, companion.getName());
                                ps.setInt(2, companion.getHp());
                                ps.setBoolean(3, companion.getAggresion());
                                ps.setInt(4, companion.getAttack());
                                ps.setString(5, companion.getCharDescription());
                                ps.setString(6, companion.getCharDescription()); // Short description fallback
                                ps.setBoolean(7, true); // Is companion
                                ps.setInt(8, roomId);
                                ps.executeUpdate();
                                
                                try (ResultSet rs = ps.getGeneratedKeys()) {
                                    if (rs.next()) {
                                        companionId = rs.getInt(1);
                                    }
                                }
                            }
                        } else {
                            // Update existing companion
                            try (PreparedStatement ps = conn.prepareStatement(
                                    "UPDATE COMPANION SET hp = ?, aggression = ?, damage = ? " +
                                    "WHERE companion_id = ?")) {
                                ps.setInt(1, companion.getHp());
                                ps.setBoolean(2, companion.getAggresion());
                                ps.setInt(3, companion.getAttack());
                                ps.setInt(4, companionId);
                                ps.executeUpdate();
                            }
                        }
                        
                        if (companionId > 0) {
                            // Ensure companion-room relationship
                            boolean relationshipExists = false;
                            try (PreparedStatement ps = conn.prepareStatement(
                                    "SELECT 1 FROM COMPANION_ROOM WHERE companion_id = ? AND room_id = ?")) {
                                ps.setInt(1, companionId);
                                ps.setInt(2, roomId);
                                relationshipExists = ps.executeQuery().next();
                            }
                            
                            if (!relationshipExists) {
                                try (PreparedStatement ps = conn.prepareStatement(
                                        "INSERT INTO COMPANION_ROOM (companion_id, room_id) VALUES (?, ?)")) {
                                    ps.setInt(1, companionId);
                                    ps.setInt(2, roomId);
                                    ps.executeUpdate();
                                }
                            }
                            
                            // Save companion inventory
                            try (PreparedStatement delete = conn.prepareStatement(
                                    "DELETE FROM COMPANION_INVENTORY WHERE companion_id = ?")) {
                                delete.setInt(1, companionId);
                                delete.executeUpdate();
                            }
                            
                            if (companion.getInventory() != null && companion.getInventorySize() > 0) {
                                for (int i = 0; i < companion.getInventorySize(); i++) {
                                    Item item = companion.getItem(i);
                                    
                                    // Get or create item ID
                                    int itemId = -1;
                                    try (PreparedStatement ps = conn.prepareStatement(
                                            "SELECT item_id FROM ITEM WHERE name = ?")) {
                                        ps.setString(1, item.getName());
                                        try (ResultSet rs = ps.executeQuery()) {
                                            if (rs.next()) {
                                                itemId = rs.getInt("item_id");
                                            }
                                        }
                                    }
                                    
                                    if (itemId == -1) {
                                        // Create new item
                                        try (PreparedStatement ps = conn.prepareStatement(
                                                "INSERT INTO ITEM (item_id, name, value, weight, long_description, short_description) " +
                                                "VALUES (DEFAULT, ?, ?, ?, ?, ?)",
                                                PreparedStatement.RETURN_GENERATED_KEYS)) {
                                            ps.setString(1, item.getName());
                                            ps.setInt(2, item.getValue());
                                            ps.setInt(3, item.getWeight());
                                            ps.setString(4, item.getDescription());
                                            ps.setString(5, item.getShortDescription());
                                            ps.executeUpdate();
                                            
                                            try (ResultSet rs = ps.getGeneratedKeys()) {
                                                if (rs.next()) {
                                                    itemId = rs.getInt(1);
                                                }
                                            }
                                        }
                                    }
                                    
                                    if (itemId > 0) {
                                        // Add to companion inventory
                                        try (PreparedStatement ps = conn.prepareStatement(
                                                "INSERT INTO COMPANION_INVENTORY (companion_id, item_id) VALUES (?, ?)")) {
                                            ps.setInt(1, companionId);
                                            ps.setInt(2, itemId);
                                            ps.executeUpdate();
                                        }
                                    }
                                }
                            }
                            
                            savedCompanions++;
                        }
                    }
                }
            }
            
            System.out.println("Saved " + savedCompanions + " companions state");
            
        } catch (SQLException e) {
            System.err.println("Failed to save all companions state: " + e.getMessage());
        }
    }
    
    /**
     * Save all players' state to the database
     * This is called periodically to ensure player progress is not lost
     */
    public void saveAllPlayersState() {
        if (players.isEmpty()) {
            return;
        }
        
        for (Player player : players) {
            if (player.getId() > 0) {
                new PlayerLoadManager().savePlayerState(player);
            }
        }
        
        // Also save all companions state
        saveAllCompanionsState();
    }
    
    //Companion methods in gameEngine
    
    public String reset() {
    		DerbyDatabase.reset(tables);
       		DatabaseInitializer.initialize();
       		loadData();
       		this.currentRoomNum = 0;
       		return "\n<b>Game was restarted to original state </b>";
    }
       public void initialize() {
    	   DatabaseInitializer.initialize();
    	   loadData();
    }

    // Disassemble command wrapper
    public String disassembleItem(String itemName) {
        return craftingManager.disassembleItem(itemName);
    }

    // Combine two components into a new item
    public String combineItems(String compA, String compB) {
        return craftingManager.combineItems(compA, compB);
    }
    
    public boolean getRunning()
    {
    	return this.isRunning;
    }

    // Quest commands
    /**
     * Accept a quest for the current player (if not already accepted or completed)
     * @param questId the quest ID to accept
     * @return a message describing the result
     */
    public String acceptQuest(int questId) {
        QuestDefinition def = questManager.get(questId);
        if (def == null) {
            return "Invalid quest ID";
        }
        
        // For backward compatibility, use the first player by default
        if (players.isEmpty()) {
            return "No player available";
        }
        
        Player currentPlayer = players.get(0);
        
        // Check if player already has this quest active or completed
        boolean alreadyHas = false;
        
        // Check active quests
        for (Quest q : currentPlayer.getActiveQuests()) {
            if (q.getDef().getId() == questId) {
                alreadyHas = true;
                break;
            }
        }
        
        // Check completed quests if not found in active
        if (!alreadyHas) {
            for (Quest q : currentPlayer.getCompletedQuests()) {
                if (q.getDef().getId() == questId) {
                    alreadyHas = true;
                    break;
                }
            }
        }
        
        // If player doesn't have the quest yet, add it
        if (!alreadyHas) {
            currentPlayer.acceptQuest(questId, questManager);
            
            // Persist in database
            try (Connection conn = DerbyDatabase.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                      "INSERT INTO player_quests (player_id, quest_id, status, progress) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, currentPlayer.getId());
                ps.setInt(2, questId);
                ps.setString(3, Quest.Status.IN_PROGRESS.name());
                ps.setInt(4, 0);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Failed to persist quest acceptance: " + e.getMessage());
            }
            
            return "Quest accepted: " + def.getName();
        } else {
            return "You already have or completed this quest";
        }
    }
    
    /**
     * Fire an event to progress quests for all players
     * @param type the event type (e.g., "KILL", "COLLECT")
     * @param name the target name
     * @param amount the amount to increment
     */
    public void fireEvent(String type, String name, int amount) {
        // Notify all players about this event for quest progression
        for (Player p : players) {
            p.onEvent(type, name, amount);
        }
    }

    public String increaseAttack() {
        if (players.isEmpty()) {
            return "No player available";
        }
        
        Player player = players.get(0);
        
        if (player.useSkillPoints(3)) {
            player.setAttackBoost(player.getAttackBoost() + 5);
            return "\n<b>Successfully increased attack by 5 points! New attack boost: " 
                 + player.getAttackBoost() + "</b>";
        } else {
            return "\n<b>Not enough skill points! You have " 
                 + player.getSkillPoints() + " skill points.</b>";
        }
    }

    public String increaseDefense() {
        if (players.isEmpty()) {
            return "No player available";
        }
        
        Player player = players.get(0);
        
        if (player.useSkillPoints(3)) {
            player.setdefenseBoost(player.getdefenseBoost() + 5);
            return "\n<b>Successfully increased defense by 5 points! New defense boost: " 
                 + player.getdefenseBoost() + "</b>";
        } else {
            return "\n<b>Not enough skill points! You have " 
                 + player.getSkillPoints() + " skill points.</b>";
        }
    }

    /**
     * Callback to RoomManager to get the list index for a given room_id.
     */
    public Integer getRoomIndex(int roomId) {
        return roomManager.getRoomIndex(roomId);
    }

}