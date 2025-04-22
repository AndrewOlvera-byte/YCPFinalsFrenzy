package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.*;

public class DerbyDatabase implements IDatabase {
    
    static {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (Exception e) {
            throw new IllegalStateException("Could not load Derby driver");
        }
    }
    
    private Connection connect() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:derby:gameDB;create=true");
        
        // Set auto-commit to false to allow for transactions
        conn.setAutoCommit(false);
        
        return conn;
    }
    
    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public<ResultType> ResultType executeTransaction(Transaction<ResultType> txn) {
        try {
            return txn.execute(this);
        } catch (Exception e) {
            e.printStackTrace();
            throw new PersistenceException("Error executing transaction", e);
        }
    }
    
    @Override
    public Player getPlayer() {
        return executeTransaction(new Transaction<Player>() {
            @Override
            public Player execute(IDatabase db) {
                Connection conn = null;
                PreparedStatement stmt = null;
                ResultSet resultSet = null;
                
                try {
                    conn = connect();
                    
                    stmt = conn.prepareStatement(
                        "select players.player_id, players.name, players.hp, players.skill_points, " +
                        "players.damage_multi, players.long_desc, players.short_desc " +
                        "from players"
                    );
                    
                    resultSet = stmt.executeQuery();
                    
                    // Get the player data
                    if (resultSet.next()) {
                        int playerId = resultSet.getInt(1);
                        String name = resultSet.getString(2);
                        int hp = resultSet.getInt(3);
                        int skillPoints = resultSet.getInt(4);
                        double damageMulti = resultSet.getDouble(5);
                        String longDesc = resultSet.getString(6);
                        String shortDesc = resultSet.getString(7);
                        
                        // Get the player's inventory
                        ArrayList<Item> items = getItemsByContainerId(playerId, true);
                        Inventory inventory = new Inventory(items, 300); // Hardcoded capacity for now
                        
                        Player player = new Player(name, hp, skillPoints, inventory, longDesc, shortDesc, damageMulti);
                        
                        conn.commit();
                        return player;
                    } else {
                        conn.commit();
                        return null;
                    }
                } catch (SQLException e) {
                    try {
                        conn.rollback();
                    } catch (SQLException e1) {
                        throw new PersistenceException("Failed to rollback transaction", e1);
                    }
                    throw new PersistenceException("Error getting player", e);
                } finally {
                    DBUtil.closeQuietly(resultSet);
                    DBUtil.closeQuietly(stmt);
                    closeConnection(conn);
                }
            }
        });
    }

    @Override
    public void insertPlayer(Player player) {
        executeTransaction(new Transaction<Void>() {
            @Override
            public Void execute(IDatabase db) {
                Connection conn = null;
                PreparedStatement stmt = null;
                
                try {
                    conn = connect();
                    
                    // Insert player
                    stmt = conn.prepareStatement(
                        "insert into players (name, hp, skill_points, damage_multi, long_desc, short_desc) " +
                        "values (?, ?, ?, ?, ?, ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS
                    );
                    
                    stmt.setString(1, player.getName());
                    stmt.setInt(2, player.getHp());
                    stmt.setInt(3, 0); // Assuming skillPoints default is 0
                    stmt.setDouble(4, player.getdamageMulti());
                    stmt.setString(5, "It's you! You know, you!"); // Default description
                    stmt.setString(6, "You."); // Default description
                    
                    stmt.executeUpdate();
                    
                    // Get the player ID to use for inventory
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int playerId = generatedKeys.getInt(1);
                        
                        // Insert player's inventory items
                        for (int i = 0; i < player.getInventory().getSize(); i++) {
                            db.insertItem(player.getItem(i), playerId, true);
                        }
                    }
                    
                    conn.commit();
                    return null;
                } catch (SQLException e) {
                    try {
                        conn.rollback();
                    } catch (SQLException e1) {
                        throw new PersistenceException("Failed to rollback transaction", e1);
                    }
                    throw new PersistenceException("Error inserting player", e);
                } finally {
                    DBUtil.closeQuietly(stmt);
                    closeConnection(conn);
                }
            }
        });
    }

    @Override
    public void updatePlayer(Player player) {
        executeTransaction(new Transaction<Void>() {
            @Override
            public Void execute(IDatabase db) {
                Connection conn = null;
                PreparedStatement stmt = null;
                ResultSet resultSet = null;
                
                try {
                    conn = connect();
                    
                    // Get player ID
                    stmt = conn.prepareStatement("select player_id from players");
                    resultSet = stmt.executeQuery();
                    
                    if (resultSet.next()) {
                        int playerId = resultSet.getInt(1);
                        
                        // Update player
                        stmt = conn.prepareStatement(
                            "update players set " +
                            "hp = ?, " +
                            "damage_multi = ? " +
                            "where player_id = ?"
                        );
                        
                        stmt.setInt(1, player.getHp());
                        stmt.setDouble(2, player.getdamageMulti());
                        stmt.setInt(3, playerId);
                        
                        stmt.executeUpdate();
                        
                        // Update inventory by removing all existing items and re-inserting current inventory
                        stmt = conn.prepareStatement("delete from items where container_id = ? and is_character_inventory = true");
                        stmt.setInt(1, playerId);
                        stmt.executeUpdate();
                        
                        // Insert current inventory
                        for (int i = 0; i < player.getInventory().getSize(); i++) {
                            db.insertItem(player.getItem(i), playerId, true);
                        }
                    }
                    
                    conn.commit();
                    return null;
                } catch (SQLException e) {
                    try {
                        conn.rollback();
                    } catch (SQLException e1) {
                        throw new PersistenceException("Failed to rollback transaction", e1);
                    }
                    throw new PersistenceException("Error updating player", e);
                } finally {
                    DBUtil.closeQuietly(resultSet);
                    DBUtil.closeQuietly(stmt);
                    closeConnection(conn);
                }
            }
        });
    }

    @Override
    public ArrayList<Room> getAllRooms() {
        return executeTransaction(new Transaction<ArrayList<Room>>() {
            @Override
            public ArrayList<Room> execute(IDatabase db) {
                Connection conn = null;
                PreparedStatement stmt = null;
                ResultSet resultSet = null;
                
                try {
                    conn = connect();
                    
                    ArrayList<Room> rooms = new ArrayList<>();
                    
                    stmt = conn.prepareStatement(
                        "select room_id, name, required_key, long_desc, short_desc from rooms"
                    );
                    
                    resultSet = stmt.executeQuery();
                    
                    while (resultSet.next()) {
                        int roomId = resultSet.getInt(1);
                        String name = resultSet.getString(2);
                        String requiredKey = resultSet.getString(3);
                        String longDesc = resultSet.getString(4);
                        String shortDesc = resultSet.getString(5);
                        
                        // Get room inventory
                        ArrayList<Item> items = getItemsByContainerId(roomId, false);
                        Inventory inventory = new Inventory(items, 300); // Hardcoded capacity
                        
                        // Get room connections
                        Connections connections = getConnectionsByRoomId(roomId);
                        
                        // Get characters in the room
                        ArrayList<models.Character> characters = getCharactersByRoomId(roomId);
                        
                        Room room;
                        if (requiredKey != null && !requiredKey.isEmpty()) {
                            room = new Room(name, inventory, connections, characters, requiredKey, longDesc, shortDesc);
                        } else {
                            room = new Room(name, inventory, connections, characters, longDesc, shortDesc);
                        }
                        
                        rooms.add(room);
                    }
                    
                    conn.commit();
                    return rooms;
                    
                } catch (SQLException e) {
                    try {
                        conn.rollback();
                    } catch (SQLException e1) {
                        throw new PersistenceException("Failed to rollback transaction", e1);
                    }
                    throw new PersistenceException("Error getting all rooms", e);
                } finally {
                    DBUtil.closeQuietly(resultSet);
                    DBUtil.closeQuietly(stmt);
                    closeConnection(conn);
                }
            }
        });
    }

    @Override
    public Room getRoomByID(int roomId) {
        // Implementation similar to getAllRooms but for a specific room
        return null; // Not implemented for brevity
    }

    @Override
    public void insertRoom(Room room) {
        // Similar implementation to insertPlayer
        // Not implemented for brevity
    }

    @Override
    public void insertItem(Item item, int containerId, boolean isCharacterInventory) {
        executeTransaction(new Transaction<Void>() {
            @Override
            public Void execute(IDatabase db) {
                Connection conn = null;
                PreparedStatement stmt = null;
                
                try {
                    conn = connect();
                    
                    // Determine item type
                    String itemType = "item";
                    if (item instanceof Weapon) {
                        itemType = "weapon";
                    } else if (item instanceof Utility) {
                        itemType = "utility";
                    }
                    
                    stmt = conn.prepareStatement(
                        "insert into items (name, value, weight, long_desc, short_desc, item_type, container_id, is_character_inventory) " +
                        "values (?, ?, ?, ?, ?, ?, ?, ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS
                    );
                    
                    stmt.setString(1, item.getName());
                    stmt.setInt(2, item.getValue());
                    stmt.setInt(3, item.getWeight());
                    stmt.setString(4, item.getDescription()); // Using description for both long/short
                    stmt.setString(5, item.getDescription());
                    stmt.setString(6, itemType);
                    stmt.setInt(7, containerId);
                    stmt.setBoolean(8, isCharacterInventory);
                    
                    stmt.executeUpdate();
                    
                    // Get the item ID
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int itemId = generatedKeys.getInt(1);
                        
                        // Insert additional properties based on item type
                        if (item instanceof Weapon) {
                            Weapon weapon = (Weapon) item;
                            PreparedStatement weaponStmt = conn.prepareStatement(
                                "insert into weapon_properties (item_id, attack_dmg) values (?, ?)"
                            );
                            weaponStmt.setInt(1, itemId);
                            weaponStmt.setInt(2, weapon.getAttackDmg());
                            weaponStmt.executeUpdate();
                            DBUtil.closeQuietly(weaponStmt);
                        } else if (item instanceof Utility) {
                            Utility utility = (Utility) item;
                            PreparedStatement utilityStmt = conn.prepareStatement(
                                "insert into utility_properties (item_id, health_restore, damage_multiplier) values (?, ?, ?)"
                            );
                            utilityStmt.setInt(1, itemId);
                            utilityStmt.setInt(2, utility.getHealthRestore());
                            utilityStmt.setDouble(3, utility.getDamageMultiplier());
                            utilityStmt.executeUpdate();
                            DBUtil.closeQuietly(utilityStmt);
                        }
                    }
                    
                    conn.commit();
                    return null;
                } catch (SQLException e) {
                    try {
                        conn.rollback();
                    } catch (SQLException e1) {
                        throw new PersistenceException("Failed to rollback transaction", e1);
                    }
                    throw new PersistenceException("Error inserting item", e);
                } finally {
                    DBUtil.closeQuietly(stmt);
                    closeConnection(conn);
                }
            }
        });
    }

    @Override
    public ArrayList<Item> getItemsByContainerId(int containerId, boolean isCharacterInventory) {
        return executeTransaction(new Transaction<ArrayList<Item>>() {
            @Override
            public ArrayList<Item> execute(IDatabase db) {
                Connection conn = null;
                PreparedStatement stmt = null;
                ResultSet resultSet = null;
                
                try {
                    conn = connect();
                    ArrayList<Item> items = new ArrayList<>();
                    
                    stmt = conn.prepareStatement(
                        "select i.item_id, i.name, i.value, i.weight, i.long_desc, i.short_desc, i.item_type " +
                        "from items i " +
                        "where i.container_id = ? and i.is_character_inventory = ?"
                    );
                    
                    stmt.setInt(1, containerId);
                    stmt.setBoolean(2, isCharacterInventory);
                    
                    resultSet = stmt.executeQuery();
                    
                    while (resultSet.next()) {
                        int itemId = resultSet.getInt(1);
                        String name = resultSet.getString(2);
                        int value = resultSet.getInt(3);
                        int weight = resultSet.getInt(4);
                        String longDesc = resultSet.getString(5);
                        String shortDesc = resultSet.getString(6);
                        String itemType = resultSet.getString(7);
                        
                        Item item = null;
                        
                        if (itemType.equals("weapon")) {
                            PreparedStatement weaponStmt = conn.prepareStatement(
                                "select attack_dmg from weapon_properties where item_id = ?"
                            );
                            weaponStmt.setInt(1, itemId);
                            ResultSet weaponResult = weaponStmt.executeQuery();
                            
                            if (weaponResult.next()) {
                                int attackDmg = weaponResult.getInt(1);
                                item = new Weapon(value, weight, name, new String[]{}, attackDmg, longDesc, shortDesc);
                            }
                            
                            DBUtil.closeQuietly(weaponResult);
                            DBUtil.closeQuietly(weaponStmt);
                        } else if (itemType.equals("utility")) {
                            PreparedStatement utilityStmt = conn.prepareStatement(
                                "select health_restore, damage_multiplier from utility_properties where item_id = ?"
                            );
                            utilityStmt.setInt(1, itemId);
                            ResultSet utilityResult = utilityStmt.executeQuery();
                            
                            if (utilityResult.next()) {
                                int healthRestore = utilityResult.getInt(1);
                                double damageMultiplier = utilityResult.getDouble(2);
                                item = new Utility(value, weight, name, new String[]{}, longDesc, shortDesc, healthRestore, damageMultiplier);
                            }
                            
                            DBUtil.closeQuietly(utilityResult);
                            DBUtil.closeQuietly(utilityStmt);
                        } else {
                            item = new Item(value, weight, name, new String[]{}, longDesc, shortDesc);
                        }
                        
                        if (item != null) {
                            items.add(item);
                        }
                    }
                    
                    conn.commit();
                    return items;
                } catch (SQLException e) {
                    try {
                        conn.rollback();
                    } catch (SQLException e1) {
                        throw new PersistenceException("Failed to rollback transaction", e1);
                    }
                    throw new PersistenceException("Error getting items by container", e);
                } finally {
                    DBUtil.closeQuietly(resultSet);
                    DBUtil.closeQuietly(stmt);
                    closeConnection(conn);
                }
            }
        });
    }

    @Override
    public ArrayList<models.Character> getCharactersByRoomId(int roomId) {
        // Implementation would be similar to other methods
        // Not fully implemented for brevity
        return new ArrayList<>();
    }

    @Override
    public void insertCharacter(models.Character character, int roomId) {
        // Not implemented for brevity
    }

    @Override
    public Connections getConnectionsByRoomId(int roomId) {
        return executeTransaction(new Transaction<Connections>() {
            @Override
            public Connections execute(IDatabase db) {
                Connection conn = null;
                PreparedStatement stmt = null;
                ResultSet resultSet = null;
                
                try {
                    conn = connect();
                    Connections connections = new Connections();
                    
                    stmt = conn.prepareStatement(
                        "select direction, connected_room_id from connections where room_id = ?"
                    );
                    stmt.setInt(1, roomId);
                    
                    resultSet = stmt.executeQuery();
                    
                    while (resultSet.next()) {
                        String direction = resultSet.getString(1);
                        int connectedRoomId = resultSet.getInt(2);
                        connections.setConnection(direction, connectedRoomId);
                    }
                    
                    conn.commit();
                    return connections;
                } catch (SQLException e) {
                    try {
                        conn.rollback();
                    } catch (SQLException e1) {
                        throw new PersistenceException("Failed to rollback transaction", e1);
                    }
                    throw new PersistenceException("Error getting connections by room ID", e);
                } finally {
                    DBUtil.closeQuietly(resultSet);
                    DBUtil.closeQuietly(stmt);
                    closeConnection(conn);
                }
            }
        });
    }

    @Override
    public void insertConnections(Connections connections, int roomId) {
        // Not implemented for brevity
    }

    @Override
    public ConversationTree getConversationTreeByNpcId(int npcId) {
        // Not implemented for brevity
        return null;
    }

    @Override
    public void insertConversationTree(ConversationTree tree, int npcId) {
        // Not implemented for brevity
    }

    @Override
    public void createTables() {
        executeTransaction(new Transaction<Void>() {
            @Override
            public Void execute(IDatabase db) {
                Connection conn = null;
                PreparedStatement stmt = null;
                
                try {
                    conn = connect();
                    
                    // Players table
                    stmt = conn.prepareStatement(
                        "create table players (" +
                        "   player_id integer primary key generated always as identity," +
                        "   name varchar(50)," +
                        "   hp integer," +
                        "   skill_points integer," +
                        "   damage_multi double," +
                        "   long_desc varchar(500)," +
                        "   short_desc varchar(500)" +
                        ")"
                    );
                    stmt.executeUpdate();
                    
                    // Rooms table
                    stmt = conn.prepareStatement(
                        "create table rooms (" +
                        "   room_id integer primary key generated always as identity," +
                        "   name varchar(100)," +
                        "   required_key varchar(50)," +
                        "   long_desc varchar(500)," +
                        "   short_desc varchar(500)" +
                        ")"
                    );
                    stmt.executeUpdate();
                    
                    // Items table
                    stmt = conn.prepareStatement(
                        "create table items (" +
                        "   item_id integer primary key generated always as identity," +
                        "   name varchar(50)," +
                        "   value integer," +
                        "   weight integer," +
                        "   long_desc varchar(500)," +
                        "   short_desc varchar(500)," +
                        "   item_type varchar(20)," +
                        "   container_id integer," +
                        "   is_character_inventory boolean" +
                        ")"
                    );
                    stmt.executeUpdate();
                    
                    // Weapon properties table
                    stmt = conn.prepareStatement(
                        "create table weapon_properties (" +
                        "   weapon_property_id integer primary key generated always as identity," +
                        "   item_id integer," +
                        "   attack_dmg integer," +
                        "   foreign key (item_id) references items (item_id)" +
                        ")"
                    );
                    stmt.executeUpdate();
                    
                    // Utility properties table
                    stmt = conn.prepareStatement(
                        "create table utility_properties (" +
                        "   utility_property_id integer primary key generated always as identity," +
                        "   item_id integer," +
                        "   health_restore integer," +
                        "   damage_multiplier double," +
                        "   foreign key (item_id) references items (item_id)" +
                        ")"
                    );
                    stmt.executeUpdate();
                    
                    // Characters table
                    stmt = conn.prepareStatement(
                        "create table characters (" +
                        "   character_id integer primary key generated always as identity," +
                        "   name varchar(50)," +
                        "   hp integer," +
                        "   is_npc boolean," +
                        "   is_aggressive boolean," +
                        "   attack_dmg integer," +
                        "   room_id integer," +
                        "   long_desc varchar(500)," +
                        "   short_desc varchar(500)," +
                        "   foreign key (room_id) references rooms (room_id)" +
                        ")"
                    );
                    stmt.executeUpdate();
                    
                    // Connections table
                    stmt = conn.prepareStatement(
                        "create table connections (" +
                        "   connection_id integer primary key generated always as identity," +
                        "   room_id integer," +
                        "   direction varchar(20)," +
                        "   connected_room_id integer," +
                        "   foreign key (room_id) references rooms (room_id)" +
                        ")"
                    );
                    stmt.executeUpdate();
                    
                    // Conversation nodes table
                    stmt = conn.prepareStatement(
                        "create table conversation_nodes (" +
                        "   node_id integer primary key generated always as identity," +
                        "   message varchar(500)," +
                        "   is_root boolean," +
                        "   drop_item boolean," +
                        "   item_to_drop integer," +
                        "   become_aggressive boolean," +
                        "   npc_id integer," +
                        "   foreign key (npc_id) references characters (character_id)" +
                        ")"
                    );
                    stmt.executeUpdate();
                    
                    // Conversation responses table
                    stmt = conn.prepareStatement(
                        "create table conversation_responses (" +
                        "   response_id integer primary key generated always as identity," +
                        "   parent_node_id integer," +
                        "   response_text varchar(500)," +
                        "   child_node_id integer," +
                        "   foreign key (parent_node_id) references conversation_nodes (node_id)," +
                        "   foreign key (child_node_id) references conversation_nodes (node_id)" +
                        ")"
                    );
                    stmt.executeUpdate();
                    
                    conn.commit();
                    return null;
                } catch (SQLException e) {
                    try {
                        conn.rollback();
                    } catch (SQLException e1) {
                        throw new PersistenceException("Failed to rollback transaction", e1);
                    }
                    throw new PersistenceException("Error creating tables", e);
                } finally {
                    DBUtil.closeQuietly(stmt);
                    closeConnection(conn);
                }
            }
        });
    }

    @Override
    public void loadInitialData() {
        executeTransaction(new Transaction<Void>() {
            @Override
            public Void execute(IDatabase db) {
                try {
                    // Load data from CSV files
                    List<Player> playerList = InitialData.getPlayers();
                    List<Room> roomList = InitialData.getRooms();
                    List<Item> itemList = InitialData.getItems();
                    List<models.Character> characterList = InitialData.getCharacters();
                    List<ConnectionData> connectionList = InitialData.getConnections();
                    
                    // Insert player(s)
                    for (Player player : playerList) {
                        db.insertPlayer(player);
                    }
                    
                    // Insert rooms
                    for (Room room : roomList) {
                        db.insertRoom(room);
                    }
                    
                    // Insert items, characters, and connections
                    // (would be implemented but omitted for brevity)
                    
                    return null;
                } catch (Exception e) {
                    throw new PersistenceException("Error loading initial data", e);
                }
            }
        });
    }
    
    public boolean tablesExist() {
        return executeTransaction(new Transaction<Boolean>() {
            @Override
            public Boolean execute(IDatabase db) {
                Connection conn = null;
                ResultSet rs = null;
                try {
                    conn = connect();
                    java.sql.DatabaseMetaData metadata = conn.getMetaData();
                    rs = metadata.getTables(null, "APP", "PLAYERS", null);
                    return rs.next(); // If PLAYERS table exists, assume all tables exist
                } catch (SQLException e) {
                    return false;
                } finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (SQLException e) {
                            // Ignore
                        }
                    }
                    closeConnection(conn);
                }
            }
        });
    }
} 