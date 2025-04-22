package database;

import java.util.ArrayList;
import java.util.List;

import models.*;

public interface IDatabase {
    // Player methods
    public Player getPlayer();
    public void insertPlayer(Player player);
    public void updatePlayer(Player player);
    
    // Room methods
    public ArrayList<Room> getAllRooms();
    public Room getRoomByID(int roomId);
    public void insertRoom(Room room);
    
    // Item methods
    public void insertItem(Item item, int containerId, boolean isCharacterInventory);
    public ArrayList<Item> getItemsByContainerId(int containerId, boolean isCharacterInventory);
    
    // Character methods
    public ArrayList<models.Character> getCharactersByRoomId(int roomId);
    public void insertCharacter(models.Character character, int roomId);
    
    // Connection methods
    public Connections getConnectionsByRoomId(int roomId);
    public void insertConnections(Connections connections, int roomId);
    
    // Conversation methods
    public ConversationTree getConversationTreeByNpcId(int npcId);
    public void insertConversationTree(ConversationTree tree, int npcId);
    
    // Transaction method
    public<ResultType> ResultType executeTransaction(Transaction<ResultType> txn);
    
    // Utility methods
    public void createTables();
    public void loadInitialData();
    
    // Add the tablesExist method to the interface
    public boolean tablesExist();
} 