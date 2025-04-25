package models;
import java.util.HashMap;

public class Connections {
    private HashMap<Integer, Room> rooms; // Map of room IDs to Room objects
    private HashMap<Integer, HashMap<String, Integer>> roomConnections; // Map of room IDs to possible directions
    private HashMap<String, Integer> connectionsMap;

    public Connections() {
        rooms = new HashMap<>();
        roomConnections = new HashMap<>();
        connectionsMap = new HashMap<>();
    }

    public void addRoom(int roomId, Room room) {
        rooms.put(roomId, room);
        roomConnections.put(roomId, new HashMap<>());
    }

    public void removeRoom(int roomId) {
        rooms.remove(roomId);
        roomConnections.remove(roomId);
    }

    public void connectRooms(int fromRoomId, int toRoomId, String direction) {
        roomConnections
            .computeIfAbsent(fromRoomId, k -> new HashMap<String,Integer>())
            .put(direction, toRoomId);
    }


    /*public boolean canMove(int currentRoomId, String direction, String playerKey) {
        if (!roomConnections.get(currentRoomId).containsKey(direction)) {
            return false; // No valid direction to move
        }

        int nextRoomId = roomConnections.get(currentRoomId).get(direction);
        Room nextRoom = rooms.get(nextRoomId);

        // Check if the next room is accessible based on the player's key
        return nextRoom.isAccessible(playerKey);  // Pass player's key to the room for access check
    }*/

    public Integer getNextRoom(int currentRoomId, String direction) {
        return roomConnections.get(currentRoomId).get(direction);
    }
    
    public void setConnection(String key, Integer value) {
        connectionsMap.put(key, value);
    }
    
    public Integer getConnection(String key) {
        return connectionsMap.get(key);
    }
    
    /*public Set<String> getAllKeys() {
        return connectionsMap.keySet();
    }*/
}


//This is an example off what the code should look like from the rooms setting sup new room keys and defining the directions 
/*
 * // you can move within that room
 * 
 * public class Room { public static void main(String[] args) { Connections
 * connections = new Connections();
 * 
 * // Add rooms connections.addRoom(1, new Room(1, "Entrance", false, null));
 * connections.addRoom(2, new Room(2, "Hallway", true, "goldKey"));
 * 
 * // Connect rooms connections.connectRooms(1, 2, "north");
 * 
 * // Player with no key tries to move north boolean canMoveWithoutKey =
 * connections.canMove(1, "north", null);
 * System.out.println("Can move without key? " + canMoveWithoutKey); // false
 * 
 * // Player with the gold key moves north boolean canMoveWithKey =
 * connections.canMove(1, "north", "goldKey");
 * System.out.println("Can move with goldKey? " + canMoveWithKey); // true
 * 
 * // Get the next room ID if (canMoveWithKey) { int nextRoomId =
 * connections.getNextRoom(1, "north"); System.out.println("Next room ID: " +
 * nextRoomId); // 2 } } }
 */