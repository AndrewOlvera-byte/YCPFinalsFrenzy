package database;

/**
 * Class for storing connection data loaded from CSV.
 */
public class ConnectionData {
    private int roomId;
    private String direction;
    private Integer connectedRoomId;
    
    public ConnectionData(int roomId, String direction, Integer connectedRoomId) {
        this.roomId = roomId;
        this.direction = direction;
        this.connectedRoomId = connectedRoomId;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public String getDirection() {
        return direction;
    }
    
    public Integer getConnectedRoomId() {
        return connectedRoomId;
    }
} 