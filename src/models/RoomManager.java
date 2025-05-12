package models;

import java.sql.*;
import java.util.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import GameEngine.GameEngine;
import models.DerbyDatabase;

public class RoomManager {
    private GameEngine engine;

    public RoomManager(GameEngine engine) {
        this.engine = engine;
    }

    // --- Loading from Derby ---

    public void loadRooms() {
        try (Connection conn = DerbyDatabase.getConnection()) {
            // 1) Load ROOM base rows
            PreparedStatement ps = conn.prepareStatement(
              "SELECT room_id, room_name, required_key, long_description, short_description " +
              "FROM ROOM ORDER BY room_id");
            ResultSet rs = ps.executeQuery();
            List<Room> roomList = new ArrayList<>();
            while (rs.next()) {
                roomList.add(new Room(
                    rs.getString("room_name"),
                    new Inventory(new ArrayList<>(), 300),
                    new Connections(),
                    new ArrayList<>(),
                    rs.getString("required_key"),
                    rs.getString("long_description"),
                    rs.getString("short_description"),
                    new ArrayList<>()
                ));
            }
            engine.getRooms().clear();
            engine.getRooms().addAll(roomList);

            // 2) Load ROOM_CONNECTIONS
            ps = conn.prepareStatement(
              "SELECT from_room_id, direction, to_room_id FROM ROOM_CONNECTIONS");
            rs = ps.executeQuery();
            while (rs.next()) {
                int from = rs.getInt("from_room_id") - 1; // zero-based index
                String dir = rs.getString("direction");
                int to   = rs.getInt("to_room_id") - 1;
                Connections con = engine.getRooms().get(from).getConnections();
                con.setConnection(dir, to);
            }

            // 3) Load ROOM_INVENTORY
            ps = conn.prepareStatement(
              "SELECT room_id, item_id FROM ROOM_INVENTORY");
            rs = ps.executeQuery();
            while (rs.next()) {
                int roomIdx = rs.getInt("room_id") - 1;
                int itemId  = rs.getInt("item_id");
                // fetch the ITEM row by itemId, build an Item object
                Item it = loadItemById(conn, itemId);
                engine.getRooms().get(roomIdx).addItem(it);
            }

            // 4) Load NPCs into rooms with their inventories
            PreparedStatement psNPC = conn.prepareStatement(
                "SELECT n.npc_id, n.name, n.hp, n.aggression, n.damage, n.long_description, n.short_description, nr.room_id " +
                "FROM NPC n JOIN NPC_ROOM nr ON n.npc_id = nr.npc_id"
            );
            ResultSet rsNPC = psNPC.executeQuery();

            while (rsNPC.next()) {
                int npcId = rsNPC.getInt("npc_id");
                
                // Load inventory for this NPC
                Inventory npcInventory = new Inventory(new ArrayList<>(), 100);
                PreparedStatement psInv = conn.prepareStatement("SELECT item_id FROM NPC_INVENTORY WHERE npc_id = ?");
                psInv.setInt(1, npcId);
                ResultSet rsInv = psInv.executeQuery();
                while (rsInv.next()) {
                    Item npcItem = loadItemById(conn, rsInv.getInt("item_id"));
                    npcInventory.addItem(npcItem);
                }

                NPC npc = new NPC(
                    rsNPC.getString("name"),
                    rsNPC.getInt("hp"),
                    rsNPC.getBoolean("aggression"),
                    new String[]{}, // dialogue placeholder
                    rsNPC.getInt("damage"),
                    npcInventory, // Use populated inventory
                    rsNPC.getString("long_description"),
                    rsNPC.getString("short_description")
                );

                int roomIdx = rsNPC.getInt("room_id") - 1;
                
                // Make sure the roomIdx is valid
                if (roomIdx >= 0 && roomIdx < engine.getRooms().size()) {
                    engine.getRooms().get(roomIdx).getCharacterContainer().add(npc);
                } else {
                    System.err.println("Warning: NPC " + npc.getName() + " references invalid room ID: " + (roomIdx + 1));
                }
            }

            // 5) Load Companions into rooms with their inventories
            PreparedStatement psCompanion = conn.prepareStatement(
                "SELECT c.companion_id, c.name, c.hp, c.aggression, c.damage, c.long_description, " +
                "c.short_description, c.companion, cr.room_id " +
                "FROM COMPANION c JOIN COMPANION_ROOM cr ON c.companion_id = cr.companion_id"
            );
            ResultSet rsCompanion = psCompanion.executeQuery();
            
            while (rsCompanion.next()) {
                int companionId = rsCompanion.getInt("companion_id");
                
                // Load inventory for this companion
                Inventory companionInventory = new Inventory(new ArrayList<>(), 100);
                PreparedStatement psInv = conn.prepareStatement("SELECT item_id FROM COMPANION_INVENTORY WHERE companion_id = ?");
                psInv.setInt(1, companionId);
                ResultSet rsInv = psInv.executeQuery();
                while (rsInv.next()) {
                    Item companionItem = loadItemById(conn, rsInv.getInt("item_id"));
                    companionInventory.addItem(companionItem);
                }

                Companion companion = new Companion(
                    rsCompanion.getString("name"),
                    rsCompanion.getInt("hp"),
                    rsCompanion.getBoolean("aggression"),
                    new String[]{}, // dialogue placeholder
                    rsCompanion.getInt("damage"),
                    companionInventory, // Use populated inventory
                    rsCompanion.getString("long_description"),
                    rsCompanion.getString("short_description"),
                    rsCompanion.getBoolean("companion")
                );

                int roomIdx = rsCompanion.getInt("room_id") - 1;
                
                // Make sure the roomIdx is valid
                if (roomIdx >= 0 && roomIdx < engine.getRooms().size()) {
                    engine.getRooms().get(roomIdx).getCompanionContainer().add(companion);
                    System.out.println("Added companion " + companion.getName() + " to room " + (roomIdx + 1));
                } else {
                    System.err.println("Warning: Companion " + companion.getName() + " references invalid room ID: " + (roomIdx + 1));
                }
            }
            
            // 6) Load player companions that aren't in rooms
            PreparedStatement psPlayerCompanions = conn.prepareStatement(
                "SELECT c.companion_id, c.name, c.hp, c.aggression, c.damage, c.long_description, " +
                "c.short_description, c.companion, pc.player_id " +
                "FROM COMPANION c JOIN PLAYER_COMPANION pc ON c.companion_id = pc.companion_id " +
                "LEFT JOIN COMPANION_ROOM cr ON c.companion_id = cr.companion_id " +
                "WHERE cr.companion_id IS NULL"
            );
            ResultSet rsPlayerCompanions = psPlayerCompanions.executeQuery();
            
            while (rsPlayerCompanions.next()) {
                int companionId = rsPlayerCompanions.getInt("companion_id");
                int playerId = rsPlayerCompanions.getInt("player_id");
                
                // Log that we found a companion that's attached to a player but not in a room
                System.out.println("Found player companion (ID: " + companionId + 
                                  ") for player " + playerId + 
                                  " that isn't in a room - will be loaded with player data");
            }
            
            System.out.println("Loaded rooms from database: " + roomList.size() + " rooms");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load rooms from database", e);
        }
    }

    // helper to pull a single item from the ITEM table
    /**  
     * Simple helper: pull a single item (no components) from the ITEM table  
     * and return the right subclass based on its `type`.  
     */
    private Item loadItemById(Connection conn, int id) throws SQLException {
    	String sql =
                "SELECT name, value, weight,\n" +
                "       long_description, short_description,\n" +
                "       type, healing, damage_multi, attack_damage,\n" +
                "       attack_boost, defense_boost, slot\n" +
                "  FROM ITEM\n" +
                " WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("ITEM " + id + " not found");
                }
                int    value       = rs.getInt("value");
                int    weight      = rs.getInt("weight");
                String name        = rs.getString("name");
                String longDesc    = rs.getString("long_description");
                String shortDesc   = rs.getString("short_description");
                String type        = rs.getString("type");

                switch (type.toUpperCase()) {
                    case "UTILITY":
                        int    healing   = rs.getInt("healing");
                        double dmgMulti  = rs.getDouble("damage_multi");
                        return new Utility(
                            value, weight, name, /*components=*/null,
                            longDesc, shortDesc,
                            healing, dmgMulti
                        );

                    case "WEAPON":
                        int attackDmg   = rs.getInt("attack_damage");
                        return new Weapon(
                            value, weight, name, /*components=*/null,
                            attackDmg,
                            longDesc, shortDesc
                        );
                    case "ARMOR":
                        // Now these columns actually exist in your ResultSet:
                        int   healAmt     = rs.getInt("healing");
                        double atkBoost  = rs.getDouble("attack_boost");
                        int   defBoost   = rs.getInt("defense_boost");
                        ArmorSlot slot    = ArmorSlot.valueOf(rs.getString("slot"));
                        return new Armor(
                            value, weight, name, null,
                            longDesc, shortDesc,
                            healAmt,
                            atkBoost,
                            defBoost,
                            slot
                        );

                    default:
                        return new Item(
                            value, weight, name, /*components=*/null,
                            longDesc, shortDesc
                        );
                }
            }
        }
    }


    /**  
     * Full helper: pull an item plus its components,  
     * then dispatch to the right subclass based on `type`.  
     */
    private Item loadItem(Connection conn, int itemId) throws SQLException {
        String sql =
            "SELECT name, value, weight, long_description, short_description, " +
            "       type, healing, damage_multi, attack_damage " +
            "  FROM ITEM " +
            " WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Item not found: " + itemId);
                }

                String name      = rs.getString("name");
                int    value     = rs.getInt("value");
                int    weight    = rs.getInt("weight");
                String longDesc  = rs.getString("long_description");
                String shortDesc = rs.getString("short_description");
                String type      = rs.getString("type");

                // load components
                List<String> comps = new ArrayList<>();
                String compSql = "SELECT component FROM ITEM_COMPONENT WHERE item_id = ?";
                try (PreparedStatement cps = conn.prepareStatement(compSql)) {
                    cps.setInt(1, itemId);
                    try (ResultSet crs = cps.executeQuery()) {
                        while (crs.next()) {
                            comps.add(crs.getString("component"));
                        }
                    }
                }
                String[] components = comps.toArray(new String[0]);

                switch (type.toUpperCase()) {
                    case "UTILITY": {
                        int    healing   = rs.getInt("healing");
                        double dmgMulti  = rs.getDouble("damage_multi");
                        return new Utility(
                            value, weight, name, components,
                            longDesc, shortDesc,
                            healing, dmgMulti
                        );
                    }
                    case "WEAPON": {
                        int attackDmg   = rs.getInt("attack_damage");
                        return new Weapon(
                            value, weight, name, components,
                            attackDmg,
                            longDesc, shortDesc
                        );
                    }
                    default:
                        return new Item(
                            value, weight, name, components,
                            longDesc, shortDesc
                        );
                }
            }
        }
    }


    // --- Movement & Interaction API (restored) ---

    /** Named by GameEngine.updateCurrentRoom(...) */
    public String updateCurrentRoom(String direction) {
        Room current = getCurrentRoom();
        int destNum = current.getConnectedRoom(direction);
        if(destNum == -1 && direction.equals("Shuttle")) {
        	return"\n<b>This is not a shuttle stop.</b>";
        }
        if (destNum == -1) {
            return "\n<b>There is no room in this direction.</b>";
        }
        Room dest = engine.getRooms().get(destNum);
        String req = dest.getRequiredKey();
        if (req != null && !req.isEmpty()
                && !engine.getPlayer().hasKey(req)) {
            return "\n<b>You do not have the required key ("+req+") to enter "
                   + dest.getRoomName()+".</b>";
        }
        if(engine.getPlayer().getPlayerCompanion() != null) {
        	engine.setCurrentRoomNum(destNum);
            dest.setRequiredKey(null);
        	return "<b>\nYou have entered " +dest.getRoomName()+" with your " +engine.getPlayer().getPlayerCompanion().name +"!</b>";
        }
        engine.setCurrentRoomNum(destNum);
        dest.setRequiredKey(null);
        return "\n<b>You have entered "+dest.getRoomName()+"!</b>";
    }
    

    /** Used by UIManager & GameEngine for map display */
    public int getMapOutput(String direction) {
        return getCurrentRoom().getConnectedRoom(direction);
    }

    public String getCurrentRoomName() {
        return getCurrentRoom().getRoomName();
    }

    public String getRoomName(int roomNum) {
        if (roomNum < 0 || roomNum >= engine.getRooms().size()) {
            return "Unknown Room";
        }
        return engine.getRooms().get(roomNum).getRoomName();
    }

    /** Helper to get the actual Room object */
    public Room getCurrentRoom() {
        return engine.getRooms().get(engine.getCurrentRoomNum());
    }

    /** Character lookup by name */
    public int CharNameToID(String name) {
        Room room = getCurrentRoom();
        for (int i = 0; i < room.getCharacterContainer().size(); i++) {
            if (room.getCharacterContainer().get(i)
                    .getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }
    
    public int CompanionNameToID(String name) {
    	Room room = getCurrentRoom();
    	for(int i = 0; i< room.getCompanionContainer().size(); i++) {
    		if(room.getCompanionContainer().get(i).getName().equalsIgnoreCase(name)) {
    			return i;
    		}
    	}
    	return -1;
    }

    /** NPC interaction stubs */
    public String talkToNPC(int idx) {
        return getCurrentRoom().talkToNPC(idx);
    }
    public String[] getResponseOptions(int idx) {
        return getCurrentRoom().getNPCResponseOptions(idx);
    }
    public String interactWithNPC(String choice, int idx) {
        return getCurrentRoom().interactWithNPC(choice, idx);
    }

    /** Examine a character */
    public String examineCharacter(int idx) {
        if (idx < 0 || idx >= getCurrentRoom().getCharacterContainer().size()) {
            return "\n<b>Invalid Character selection.</b>";
        }
        return "\n<b>"+ getCurrentRoom()
               .getCharacter(idx).getCharDescription() +"</b>";
    }
    public String getGo(String noun) {
        String direction;
        final Set<String> NORTH = new HashSet<>(Arrays.asList("North","north","N","n"));
        final Set<String> SOUTH = new HashSet<>(Arrays.asList("South","south","S","s"));
        final Set<String> EAST  = new HashSet<>(Arrays.asList("East","east","E","e"));
        final Set<String> WEST  = new HashSet<>(Arrays.asList("West","west","W","w"));

        if (NORTH.contains(noun)) {
            direction = "North";
        } else if (SOUTH.contains(noun)) {
            direction = "South";
        } else if (EAST.contains(noun)) {
            direction = "East";
        } else if (WEST.contains(noun)) {
            direction = "West";
        } else {
            return "\n<b>This is not a valid direction.</b>";
        }

        // Delegate to our existing updateCurrentRoom(...)
        String newMessage = updateCurrentRoom(direction);
        return newMessage;
    }

    /**
     * Handle the "shuttle" command (requires the shuttle pass).
     */
    public String getOnShuttle() {
        String newMessage;
        if (engine.getPlayer().hasKey("Shuttle Pass")) {
            newMessage = updateCurrentRoom("Shuttle");
        } else {
            newMessage = "\n<b>You do not have the Shuttle Pass.</b>";
        }
        return newMessage;
    }
}
