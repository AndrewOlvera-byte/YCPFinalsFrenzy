package models;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import GameEngine.GameEngine;

public class CompanionManager {
	private GameEngine engine;
	
	public CompanionManager(GameEngine gameEngine) {
		this.engine = gameEngine;
	}
	
	public String examineCompanion(String name) {
		Room currentRoom   = engine.getRooms().get(engine.getCurrentRoomNum());
		if (engine.getPlayer().getPlayerCompanion().name.equals(name)) {
			return "<b>" +engine.getPlayer().getPlayerCompanion().getCharDescription() + "</b>";
		}
		
		for(int i = 0 ; i < currentRoom.getCompanionContainer().size(); i++) {
			if (currentRoom.getCompanionContainer().get(i).name.equals(name)) {
				return "<b>" +engine.getPlayer().getPlayerCompanion().getCharDescription() + "</b>";
			}
		}
		return "<b>This is not a valid companion</b>";
	}
    public int RoomCompanionNameToID(String name) {
        Room room = engine.getRooms().get(engine.getCurrentRoomNum());
        for (int i = 0; i < room.getCompanionContainerSize(); i++) {
            if (name.equalsIgnoreCase(room.getCompanionName(i))) {
                return i;
            }
        }
        return -1;
    }
    
    public int playerCompanionNameToID(String name) {
    	return 1;
    }
	
    // Helper method to get the room_id from the database for the current room
    private int getCurrentRoomDbId() {
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        String roomName = currentRoom.getRoomName();
        int roomId = -1;
        
        try (Connection conn = DerbyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT room_id FROM ROOM WHERE room_name = ?")) {
            ps.setString(1, roomName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                roomId = rs.getInt("room_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get room ID: " + e.getMessage(), e);
        }
        
        return roomId;
    }
    
	public String chooseCompanion(int companionNum) {
		if (companionNum < 0) {
            return "\n<b>choose what Companion?</b>";
        }
        
        Room room = engine.getRooms().get(engine.getCurrentRoomNum());
        Companion companion = room.getCompanion(companionNum);
        String companionName = companion.getName();
        
        // Check database first to see if the player already has this item
        try (Connection conn = DerbyDatabase.getConnection()) {
            // Get the actual item_id from the database
            int databaseCompanionId = -1;
            
            try (PreparedStatement psGetCompanionId = conn.prepareStatement(
                     "SELECT companion_id FROM COMPANION WHERE name = ?")) {
                psGetCompanionId.setString(1, companionName);
                ResultSet rsCompanionId = psGetCompanionId.executeQuery();
                if (rsCompanionId.next()) {
                	databaseCompanionId = rsCompanionId.getInt("companion_id");
                }
            }
            
            // If we couldn't find the item ID in the database, use the itemNum + 1 as fallback
            if (databaseCompanionId == -1) {
            	databaseCompanionId = companionNum + 1;
            }
            
            try (PreparedStatement check = conn.prepareStatement(
                     "SELECT 1 FROM PLAYER_COMPANION WHERE player_id = ? AND companion_id = ?")) {
                check.setInt(1, 1);
                check.setInt(2, databaseCompanionId);
                if (check.executeQuery().next()) {
                    // Already in inventory, don't make any changes
                    return "<b>\nYou already have a " + companionName + ".</b>";
                }
            }
            
            // Only proceed with changes if the item is not already in inventory
            // In-memory move
            room.removeCompanion(companionNum);
            engine.getPlayer().setPlayerCompanion(companion);

            // Get the actual room ID from the database
            int roomId = getCurrentRoomDbId();
            
            // DB update: remove from ROOM_INVENTORY, add to PLAYER_INVENTORY
            // 1) DELETE from ROOM_INVENTORY - check if it exists first
            boolean companionExistsInRoom = false;
            try (PreparedStatement checkRoom = conn.prepareStatement(
                     "SELECT 1 FROM COMPANION_ROOM WHERE room_id = ? AND companion_id = ?")) {
                checkRoom.setInt(1, roomId);
                checkRoom.setInt(2, databaseCompanionId);
                companionExistsInRoom = checkRoom.executeQuery().next();
            }
            
            if (companionExistsInRoom) {
                try (PreparedStatement del = conn.prepareStatement(
                         "DELETE FROM COMPANION_ROOM WHERE room_id = ? AND companion_id = ?")) {
                    del.setInt(1, roomId);
                    del.setInt(2, databaseCompanionId);
                    del.executeUpdate();
                }
            }
            
            // 2) INSERT into PLAYER_INVENTORY
            try (PreparedStatement ins = conn.prepareStatement(
                     "INSERT INTO PLAYER_COMPANION(player_id, companion_id) VALUES(1, ?)")) {
                ins.setInt(1, databaseCompanionId);
                ins.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Companion DB update failed", e);
        }

        return "<b>\n" + companionName + " was chosen.</b>";
    }
	
	public String shooCompanion(int companionNum) {
		if (companionNum < 0) {
            return "\n<b>Invalid companion selection.</b>";
        }
        // In-memory move
        Companion companion = engine.getPlayer().getPlayerCompanion();
        String companionName = companion.getName();
        Room room = engine.getRooms().get(engine.getCurrentRoomNum());
        engine.getPlayer().dropCompanion();
        room.addCompanion(companion);

        // DB update: remove from PLAYER_INVENTORY, add to ROOM_INVENTORY
        try (Connection conn = DerbyDatabase.getConnection()) {
            // Get the actual item_id from the database
            int databaseCompanionId = -1;
            
            try (PreparedStatement psGetItemId = conn.prepareStatement(
                     "SELECT companion_id FROM COMPANION WHERE name = ?")) {
                psGetItemId.setString(1, companionName);
                ResultSet rsItemId = psGetItemId.executeQuery();
                if (rsItemId.next()) {
                	databaseCompanionId = rsItemId.getInt("companion_id");
                }
            }
            
            // If we couldn't find the item ID in the database, use the itemNum + 1 as fallback
            if (databaseCompanionId == -1) {
            	databaseCompanionId = companionNum + 1;
            }
            
            // 1) DELETE from PLAYER_INVENTORY
            try (PreparedStatement del = conn.prepareStatement(
                     "DELETE FROM PLAYER_COMPANION WHERE player_id = 1 AND companion_id = ?"
                 )) {
                del.setInt(1, databaseCompanionId);
                del.executeUpdate();
            }
            
            // Get the actual room ID from the database
            int roomId = getCurrentRoomDbId();
            
            // 2) Check if the item already exists in the room's inventory
            boolean companionExistsInRoom = false;
            try (PreparedStatement checkRoom = conn.prepareStatement(
                     "SELECT 1 FROM COMPANION_ROOM WHERE room_id = ? AND companion_id = ?")) {
                checkRoom.setInt(1, roomId);
                checkRoom.setInt(2, databaseCompanionId);
                companionExistsInRoom = checkRoom.executeQuery().next();
            }
            
            // Only insert if it doesn't already exist
            if (!companionExistsInRoom) {
                try (PreparedStatement ins = conn.prepareStatement(
                         "INSERT INTO COMPANION_ROOM(room_id, companion_id) VALUES(?, ?)"
                     )) {
                    ins.setInt(1, roomId);
                    ins.setInt(2, databaseCompanionId);
                    ins.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Companion DB update failed", e);
        }

        return "\n<b>" + companionName + " was shooed away.</b>";
	}
}
	