package models;

import java.util.*;

import GameEngine.GameEngine;
import models.*;

public class RoomManager {
    private GameEngine engine;
    
    public RoomManager(GameEngine engine) {
        this.engine = engine;
    }
    
    // Gets the current room object
    public Room getCurrentRoom() {
        ArrayList<Room> rooms = engine.getRooms();
        if (rooms.isEmpty()) {
            // If there are no rooms, load rooms manually
            loadRooms();
            // If still empty after loading, throw a clear error
            if (rooms.isEmpty()) {
                throw new IllegalStateException("Room list is empty. Unable to get current room.");
            }
        }
        return rooms.get(engine.getCurrentRoomNum());
    }
    
    // Loading of rooms
    public void loadRooms() {
        // Room One: No key required
        String roomName1 = "Manor South Lobby";
        String[] components = {};
        Weapon weapon1 = new Weapon(20, 30, "Sword", components, 80, "<b>A rusty sword. Doesn't really fit in your bag.</b>", "<b>A sword.</b>");
        ArrayList<Item> itemContainer1 = new ArrayList<>();
        itemContainer1.add(weapon1);
        Utility potion1 = new Utility(10, 1, "Potion", null, "<b>A red potion that has \"POTION\" written across it. (+40 health)</b>", "<b>+40 Health potion.</b>",40,0);
        itemContainer1.add(potion1);
        Utility potion2 = new Utility(10, 1, "Damage Up", null, "<b>An orange potion that has \"DAMAGE UP\" written across it. (x1.2 damage)</b>", "<b>x1.2 Damage potion.</b>",0,1.2);
        itemContainer1.add(potion2);
        Inventory inventory1 = new Inventory(itemContainer1, 300);
        Connections connections1 = new Connections();
        connections1.setConnection("North", 1);
        connections1.setConnection("East", null);
        connections1.setConnection("South", null);
        connections1.setConnection("West", null);
        connections1.setConnection("Shuttle", 2);
        ArrayList<models.Character> characterContainer1 = new ArrayList<>();
        Room newRoom1 = new Room(roomName1, inventory1, connections1, characterContainer1, "<b>You are standing inside the lobby of Manor South. It smells bad in here.</b>", "<b>You glance around the Manor South Lobby.</b>");
        engine.getRooms().add(newRoom1);
        
        // Room Two: No key required
        String roomName2 = "Manors Road";
        ArrayList<Item> itemContainer2 = new ArrayList<>();
        Inventory inventory2 = new Inventory(itemContainer2, 300);
        Connections connections2 = new Connections();
        connections2.setConnection("North", null);
        connections2.setConnection("East", 2);
        connections2.setConnection("South", 0);
        connections2.setConnection("West", null);
        
        ArrayList<Item> itemContainerBoss = new ArrayList<>();
        String[] componentsBoss = {};
        Weapon weaponBoss = new Weapon(20, 30, "Trident", componentsBoss, 90, "<b>A sharp three pronged weapon. You could make like, at least two roasted marshmallows.</b>", "<b>A trident.</b>");
        itemContainerBoss.add(weaponBoss);
        Item goldKey = new Item(0, 1, "Gold Key", new String[]{}, "<b>A super shiny key! Probably unlocks a door, but what do I know.</b>", "<b>A key!</b>");
        itemContainerBoss.add(goldKey);
        Inventory inventoryBoss = new Inventory(itemContainerBoss, 300);
        ArrayList<models.Character> characterContainer2 = new ArrayList<>();
        // Example NPC boss added to room two
        models.NPC boss = new NPC("Moe", 160, true, null, 80, inventoryBoss, "<b>Powerful man. Don't mess around!</b>", "<b>That's Moe!</b>");
        characterContainer2.add(boss);
        Room newRoom2 = new Room(roomName2, inventory2, connections2, characterContainer2, "<b>You are standing in front of the Slums- I mean the Manors.</b>", "<b>You look out at the road out front of the Manors.</b>");
        engine.getRooms().add(newRoom2);
        
        // Room Three: Requires the "gold key" to enter
        String roomName3 = "The Student Union";
        ArrayList<Item> itemContainer3 = new ArrayList<>();
        Inventory inventory3 = new Inventory(itemContainer3, 300);
        Connections connections3 = new Connections();
        connections3.setConnection("North", null);
        connections3.setConnection("East", null);
        connections3.setConnection("South", null);
        connections3.setConnection("West", 1);
        connections3.setConnection("Shuttle", 3);
        
        ArrayList<Item> itemContainerFriend = new ArrayList<>();
        String[] componentsFriend = {};
        Item shuttlePass = new Item(0,1, "Shuttle Pass", new String[] {}, "</b>A blue pass to ride the shuttle. The road awaits!</b>", "<b>A shuttle pass.</b>");
        itemContainer3.add(shuttlePass);
        Weapon weaponFriend = new Weapon(20, 30, "Paint Brush", componentsFriend, 1, "<b>Paint your enemies??</b>", "<b>A paint brush.</b>");
        itemContainerFriend.add(weaponFriend);
        Inventory inventoryFriend = new Inventory(itemContainerFriend, 300);
        ArrayList<models.Character> characterContainer3 = new ArrayList<>();
        NPC Friend = new NPC("Curly", 400, false, null, 5,inventoryFriend, "<b>Curly stands in front of you. He looks content. Must be a Civil Engineer.</b>", "<b>It's Curly!</b>");
        
        ConversationNode root = new ConversationNode("<b>What's up!</b>");
        
        ConversationNode good = new ConversationNode("<b>A paint brush, check this out!</b>");
        good.setDropItem(true);
        good.setItemToDrop(0);
        
        ConversationNode bad = new ConversationNode("<b>I thought we were friends!</b>");
        bad.setBecomeAggressive(true);
        
        ConversationTree conversationTree = new ConversationTree(root);
        root.addResponse("<b>1. Hey Curly, what's that you got?</b>", good);
        root.addResponse("<b>2. AHHHH [Attack]</b>", bad);

        
        Friend.addConversationTree(conversationTree);
        characterContainer3.add(Friend);
        // Room three requires the "gold key"
        Room newRoom3 = new Room(roomName3, inventory3, connections3, characterContainer3, "Gold key", "<b>You stand inside the Student Union building, ahead of you is the Dining Hall.</b>", "<b>You glance around the lobby.</b>");
        engine.getRooms().add(newRoom3);
        
        //Room Four: To Show off Shuttle
        String roomName4 = "The Shuttle Stop";
        ArrayList<Item> itemContainer4 = new ArrayList<>();
        Inventory inventory4 = new Inventory(itemContainer4, 300);
        itemContainer4.add(potion1);
        Connections connection4 = new Connections();
        connection4.setConnection("North", null);
        connection4.setConnection("East", null);
        connection4.setConnection("South", null);
        connection4.setConnection("West", null);
        connection4.setConnection("Shuttle", 0);
        
        ArrayList<models.Character> characterContainer4 = new ArrayList<>();
        Room newRoom4 = new Room(roomName4, inventory4, connection4, characterContainer4, "<b>Looking around West Campus, you see the Rutter's to the west and the road back to main on the east.</b>", "<b>You glance around West Campus.</b>");
        engine.getRooms().add(newRoom4);
    }
    
    // Updates currentRoom to returned int if room is available, and returns true if updated; false otherwise
    public String updateCurrentRoom(String direction) {
        Room currentRoom = getCurrentRoom();
        int newRoomNum = currentRoom.getConnectedRoom(direction);
        if (newRoomNum != -1) {
            Room destination = engine.getRooms().get(newRoomNum);
            String requiredKey = destination.getRequiredKey();
            
            // If a key is required, check if the player has it.
            if (requiredKey != null && !requiredKey.isEmpty()) {
                if (!engine.getPlayer().hasKey(requiredKey)) {
                    return "\n<b>You do not have the required key (" + requiredKey + ") to enter " + destination.getRoomName() + ".</b>";
                }
            }
            
            // Allow the room change.
            engine.setCurrentRoomNum(newRoomNum);
            engine.getRooms().get(engine.getCurrentRoomNum()).setRequiredKey(null);
            return "\n<b>You have entered " + destination.getRoomName() + "!</b>";
        }
        return "\n<b>There is no room in this direction.</b>";
    }
    
    public int getMapOutput(String direction) {
        Room currentRoom = getCurrentRoom();
        int output = currentRoom.getConnectedRoom(direction);
        return output;
    }
    
    public String getCurrentRoomName() {
        try {
            Room currentRoom = getCurrentRoom();
            return currentRoom.getRoomName();
        } catch (Exception e) {
            // If there's an error getting the current room, return a default name
            return "Default Room";
        }
    }
    
    public String getRoomName(int roomNum) {
        if (roomNum < 0 || roomNum >= engine.getRooms().size()) {
            return "Unknown Room";
        }
        Room currentRoom = engine.getRooms().get(roomNum);
        return currentRoom.getRoomName();
    }
    
    // Helper method to convert from a char name to its ID
    public int CharNameToID(String name) {
        int charNum = -1; // set to -1 so if there is no char found it will return -1
        Room currentRoom = getCurrentRoom();
        for (int i = 0; i < currentRoom.getCharacterTotal(); i++) {
            if (name.equalsIgnoreCase(currentRoom.getCharacterName(i))) {
                charNum = i;
            }
        }
        return charNum;
    }
    
    public String examineCharacter(int charNum) {
        if(charNum == -1) {
            return "\n<b>Examine what Character?</b>";
        }
        
        if(charNum < 0 || charNum >= getCurrentRoom().getCharacterContainerSize()) {
            return "\n<b>Invalid Character selection.</b>";
        }
        
        Character character = getCurrentRoom().getCharacter(charNum);
        return"\n<b>" + character.getCharDescription() + "</b>";
    }
    
    public String getGo(String noun) {
        String direction = "";
        final Set<String> NORTH = new HashSet<>(Arrays.asList("North", "north", "N", "n"));
        final Set<String> SOUTH = new HashSet<>(Arrays.asList("South", "south", "s", "S"));
        final Set<String> EAST  = new HashSet<>(Arrays.asList("East", "east", "E", "e"));
        final Set<String> WEST  = new HashSet<>(Arrays.asList("West", "west", "W", "w"));
        
        if (NORTH.contains(noun)) {
            direction = "North";
        } else if (SOUTH.contains(noun)) {
            direction = "South";
        } else if (EAST.contains(noun)) {
            direction = "East";
        } else if (WEST.contains(noun)) {
            direction = "West";
        } else {
            return "\n<b>This is not a valid direction</b>";
        }
        
        // Call updateCurrentRoom once and capture its returned message.
        String newMessage = updateCurrentRoom(direction);
        
        // Append the new message once to runningMessage.
        engine.appendMessage(newMessage);
        
        // Return only the new message
        return newMessage;
    }
    
    public String getOnShuttle() {
        String newMessage = "";
        
        if(engine.getPlayer().hasKey("Shuttle Pass")) {
            String direction = "Shuttle";
            newMessage = updateCurrentRoom(direction);
            engine.appendMessage(newMessage);
        }
        else {
            newMessage = "<b>\nYou do not have the Shuttle Pass.</b>";
            engine.appendMessage(newMessage);
        }
        
        return newMessage;
    }
    
    // NPC interaction methods
    public String talkToNPC(int characterNum) {
        Room currentRoom = getCurrentRoom();
        return currentRoom.talkToNPC(characterNum);
    }
    
    public String[] getResponseOptions(int characterNum) {
        Room currentRoom = getCurrentRoom();
        return currentRoom.getNPCResponseOptions(characterNum);
    }
    
    public String interactWithNPC(String choice, int characterNum) {
        Room currentRoom = getCurrentRoom();
        NPC npc = (NPC) currentRoom.getCharacter(characterNum);
        String result = npc.interact(choice);

        // Handle aggression toggle
        if (npc.isCurrentNodeToAggressive()) {
            npc.setAgression(true);
            result += "<b>\n" + npc.getName() + " is now hostile!</b>";
        }

        // Handle item drop
        if (npc.isCurrentNodeDropItem()) {
            int dropItemIndex = npc.getItemToDrop();
            Item item = npc.getInventory().getItem(dropItemIndex);
            npc.getInventory().removeItem(dropItemIndex);
            currentRoom.addItem(item);
            result += "<b>\n" + npc.getName() + " dropped a " + item.getName() + "!</b>";
        }

        return result;
    }
}