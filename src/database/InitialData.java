package database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import models.*;

/**
 * Class for loading initial data from CSV files.
 */
public class InitialData {
    /**
     * Loads player data from players.csv.
     * 
     * @return A list of Player objects
     * @throws IOException If there is an error reading the file
     */
    public static List<Player> getPlayers() throws IOException {
        List<Player> playerList = new ArrayList<Player>();
        ReadCSV readPlayers = new ReadCSV("players.csv");
        try {
            // Skip header row
            readPlayers.next();
            
            while (true) {
                List<String> tuple = readPlayers.next();
                if (tuple == null) {
                    break;
                }
                
                Iterator<String> i = tuple.iterator();
                String name = i.next();
                int hp = Integer.parseInt(i.next());
                int skillPoints = Integer.parseInt(i.next());
                double damageMulti = Double.parseDouble(i.next());
                String longDesc = i.next();
                String shortDesc = i.next();
                
                // Create empty inventory for now, items will be loaded separately
                ArrayList<Item> items = new ArrayList<>();
                Inventory inventory = new Inventory(items, 300); // Hardcoded capacity
                
                Player player = new Player(name, hp, skillPoints, inventory, longDesc, shortDesc, damageMulti);
                playerList.add(player);
            }
            return playerList;
        } finally {
            readPlayers.close();
        }
    }
    
    /**
     * Loads room data from rooms.csv.
     * 
     * @return A list of Room objects
     * @throws IOException If there is an error reading the file
     */
    public static List<Room> getRooms() throws IOException {
        List<Room> roomList = new ArrayList<Room>();
        ReadCSV readRooms = new ReadCSV("rooms.csv");
        try {
            // Skip header row
            readRooms.next();
            
            while (true) {
                List<String> tuple = readRooms.next();
                if (tuple == null) {
                    break;
                }
                
                Iterator<String> i = tuple.iterator();
                String name = i.next();
                String requiredKey = i.next();
                if (requiredKey.equals("null")) {
                    requiredKey = null;
                }
                String longDesc = i.next();
                String shortDesc = i.next();
                
                // Create empty inventory and connections for now
                ArrayList<Item> items = new ArrayList<>();
                Inventory inventory = new Inventory(items, 300);
                Connections connections = new Connections();
                ArrayList<models.Character> characters = new ArrayList<>();
                
                Room room;
                if (requiredKey != null) {
                    room = new Room(name, inventory, connections, characters, requiredKey, longDesc, shortDesc);
                } else {
                    room = new Room(name, inventory, connections, characters, longDesc, shortDesc);
                }
                
                roomList.add(room);
            }
            return roomList;
        } finally {
            readRooms.close();
        }
    }
    
    /**
     * Loads item data from items.csv.
     * 
     * @return A list of Item objects
     * @throws IOException If there is an error reading the file
     */
    public static List<Item> getItems() throws IOException {
        List<Item> itemList = new ArrayList<Item>();
        ReadCSV readItems = new ReadCSV("items.csv");
        try {
            // Skip header row
            readItems.next();
            
            while (true) {
                List<String> tuple = readItems.next();
                if (tuple == null) {
                    break;
                }
                
                Iterator<String> i = tuple.iterator();
                int containerId = Integer.parseInt(i.next());
                boolean isCharacterInventory = Boolean.parseBoolean(i.next());
                String name = i.next();
                int value = Integer.parseInt(i.next());
                int weight = Integer.parseInt(i.next());
                String longDesc = i.next();
                String shortDesc = i.next();
                String itemType = i.next();
                
                Item item = null;
                
                if (itemType.equals("weapon")) {
                    int attackDmg = Integer.parseInt(i.next());
                    item = new Weapon(value, weight, name, new String[]{}, attackDmg, longDesc, shortDesc);
                } else if (itemType.equals("utility")) {
                    int healthRestore = Integer.parseInt(i.next());
                    double damageMultiplier = Double.parseDouble(i.next());
                    item = new Utility(value, weight, name, new String[]{}, longDesc, shortDesc, healthRestore, damageMultiplier);
                } else {
                    item = new Item(value, weight, name, new String[]{}, longDesc, shortDesc);
                }
                
                itemList.add(item);
            }
            return itemList;
        } finally {
            readItems.close();
        }
    }
    
    /**
     * Loads character data from characters.csv.
     * 
     * @return A list of Character objects
     * @throws IOException If there is an error reading the file
     */
    public static List<models.Character> getCharacters() throws IOException {
        List<models.Character> characterList = new ArrayList<models.Character>();
        ReadCSV readChars = new ReadCSV("characters.csv");
        try {
            // Skip header row
            readChars.next();
            
            while (true) {
                List<String> tuple = readChars.next();
                if (tuple == null) {
                    break;
                }
                
                Iterator<String> i = tuple.iterator();
                int roomId = Integer.parseInt(i.next());
                String name = i.next();
                int hp = Integer.parseInt(i.next());
                boolean isNPC = Boolean.parseBoolean(i.next());
                boolean isAggressive = Boolean.parseBoolean(i.next());
                int attackDmg = Integer.parseInt(i.next());
                String longDesc = i.next();
                String shortDesc = i.next();
                
                // Create empty inventory for now
                ArrayList<Item> items = new ArrayList<>();
                Inventory inventory = new Inventory(items, 300);
                
                models.Character character;
                if (isNPC) {
                    character = new NPC(name, hp, isAggressive, null, attackDmg, inventory, longDesc, shortDesc);
                } else {
                    character = new models.Character(name, hp, inventory, longDesc, shortDesc);
                }
                
                characterList.add(character);
            }
            return characterList;
        } finally {
            readChars.close();
        }
    }
    
    /**
     * Loads connection data from connections.csv.
     * 
     * @return A list of ConnectionData objects
     * @throws IOException If there is an error reading the file
     */
    public static List<ConnectionData> getConnections() throws IOException {
        List<ConnectionData> connectionList = new ArrayList<ConnectionData>();
        ReadCSV readConnections = new ReadCSV("connections.csv");
        try {
            // Skip header row
            readConnections.next();
            
            while (true) {
                List<String> tuple = readConnections.next();
                if (tuple == null) {
                    break;
                }
                
                Iterator<String> i = tuple.iterator();
                int roomId = Integer.parseInt(i.next());
                String direction = i.next();
                String connectedRoomStr = i.next();
                Integer connectedRoomId = connectedRoomStr.equals("null") ? null : Integer.parseInt(connectedRoomStr);
                
                ConnectionData connection = new ConnectionData(roomId, direction, connectedRoomId);
                connectionList.add(connection);
            }
            return connectionList;
        } finally {
            readConnections.close();
        }
    }
} 