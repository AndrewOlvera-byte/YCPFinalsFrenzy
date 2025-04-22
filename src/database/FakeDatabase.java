package database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.*;

/**
 * A fake database implementation that stores all data in memory.
 * Useful for testing without connecting to a real database.
 */
public class FakeDatabase implements IDatabase {
    // In-memory data storage
    private Player player;
    private ArrayList<Room> rooms;
    private HashMap<Integer, ArrayList<Item>> containerToItems;
    private HashMap<Integer, Connections> roomToConnections;
    private HashMap<Integer, ArrayList<models.Character>> roomToCharacters;
    private HashMap<Integer, ConversationTree> npcToConversation;
    
    public FakeDatabase() {
        rooms = new ArrayList<>();
        containerToItems = new HashMap<>();
        roomToConnections = new HashMap<>();
        roomToCharacters = new HashMap<>();
        npcToConversation = new HashMap<>();
    }
    
    @Override
    public <ResultType> ResultType executeTransaction(Transaction<ResultType> txn) {
        return txn.execute(this);
    }
    
    @Override
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public void insertPlayer(Player player) {
        this.player = player;
        
        // Store player's inventory items
        ArrayList<Item> playerItems = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            playerItems.add(player.getItem(i));
        }
        containerToItems.put(0, playerItems); // Player ID is always 0 in fake DB
    }
    
    @Override
    public void updatePlayer(Player player) {
        this.player = player;
        
        // Update player's inventory
        ArrayList<Item> playerItems = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            playerItems.add(player.getItem(i));
        }
        containerToItems.put(0, playerItems);
    }
    
    @Override
    public ArrayList<Room> getAllRooms() {
        return rooms;
    }
    
    @Override
    public Room getRoomByID(int roomId) {
        if (roomId >= 0 && roomId < rooms.size()) {
            return rooms.get(roomId);
        }
        return null;
    }
    
    @Override
    public void insertRoom(Room room) {
        int roomId = rooms.size();
        rooms.add(room);
        
        // Store room's inventory
        ArrayList<Item> roomItems = new ArrayList<>();
        for (int i = 0; i < room.getInventorySize(); i++) {
            roomItems.add(room.getItem(i));
        }
        containerToItems.put(roomId + 1, roomItems); // Room IDs start at 1
        
        // Store room's connections - note: we need to access connections through getConnection methods
        Connections connections = new Connections();
        if (room.getConnectedRoom("North") != -1) connections.setConnection("North", room.getConnectedRoom("North"));
        if (room.getConnectedRoom("South") != -1) connections.setConnection("South", room.getConnectedRoom("South"));
        if (room.getConnectedRoom("East") != -1) connections.setConnection("East", room.getConnectedRoom("East"));
        if (room.getConnectedRoom("West") != -1) connections.setConnection("West", room.getConnectedRoom("West"));
        if (room.getConnectedRoom("Shuttle") != -1) connections.setConnection("Shuttle", room.getConnectedRoom("Shuttle"));
        roomToConnections.put(roomId, connections);
        
        // Store room's characters
        ArrayList<models.Character> characters = new ArrayList<>();
        for (int i = 0; i < room.getCharacterContainerSize(); i++) {
            characters.add(room.getCharacter(i));
        }
        roomToCharacters.put(roomId, characters);
    }
    
    @Override
    public void insertItem(Item item, int containerId, boolean isCharacterInventory) {
        ArrayList<Item> items = containerToItems.get(containerId);
        if (items == null) {
            items = new ArrayList<>();
            containerToItems.put(containerId, items);
        }
        items.add(item);
    }
    
    @Override
    public ArrayList<Item> getItemsByContainerId(int containerId, boolean isCharacterInventory) {
        ArrayList<Item> items = containerToItems.get(containerId);
        if (items == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(items); // Return a copy to prevent modifying the original
    }
    
    @Override
    public ArrayList<models.Character> getCharactersByRoomId(int roomId) {
        ArrayList<models.Character> characters = roomToCharacters.get(roomId);
        if (characters == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(characters);
    }
    
    @Override
    public void insertCharacter(models.Character character, int roomId) {
        ArrayList<models.Character> characters = roomToCharacters.get(roomId);
        if (characters == null) {
            characters = new ArrayList<>();
            roomToCharacters.put(roomId, characters);
        }
        characters.add(character);
    }
    
    @Override
    public Connections getConnectionsByRoomId(int roomId) {
        return roomToConnections.get(roomId);
    }
    
    @Override
    public void insertConnections(Connections connections, int roomId) {
        roomToConnections.put(roomId, connections);
    }
    
    @Override
    public ConversationTree getConversationTreeByNpcId(int npcId) {
        return npcToConversation.get(npcId);
    }
    
    @Override
    public void insertConversationTree(ConversationTree tree, int npcId) {
        npcToConversation.put(npcId, tree);
    }
    
    @Override
    public void createTables() {
        // No tables to create in memory database
    }
    
    @Override
    public void loadInitialData() {
        try {
            // Load initial data from CSV files
            List<Player> playerList = InitialData.getPlayers();
            List<Room> roomList = InitialData.getRooms();
            
            // Insert player
            if (!playerList.isEmpty()) {
                insertPlayer(playerList.get(0));
            }
            
            // Insert rooms
            for (Room room : roomList) {
                insertRoom(room);
            }
        } catch (Exception e) {
            throw new PersistenceException("Error loading initial data", e);
        }
    }
} 