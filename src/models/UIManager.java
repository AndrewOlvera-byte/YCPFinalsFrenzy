package models;

import java.util.*;

import GameEngine.GameEngine;

public class UIManager {
    private GameEngine engine;
    // Map to store the position for each item based on its name.
    // The array holds two doubles: index 0 for leftPercent and index 1 for topPercent.
    private Map<String, double[]> itemPositions;
    
    public UIManager(GameEngine engine) {
        this.engine = engine;
        this.itemPositions = new HashMap<>();
    }
    
    // Returns help text for player
    public String getHelp() {
        StringBuilder help = new StringBuilder();
        help.append("<b>\nTake, get, grab, pickup are for picking up an item. (ex. (get|grab|pickup) sword)</b>\n");
        help.append("<b>\nDrop is to drop an item (ex. drop dagger)</b>\n");
        help.append("<b>\nAttack, swing, slash, hit, strike are for attacking an enemy. (ex. (attack|swing|slash|hit|strike) moe with trident)</b>\n");
        help.append("<b>\nGo, move, walk are for moving in a direction. (ex. (walk|move) north)</b>\n");
        help.append("<b>\nExamine and look are for looking at the description of an item or room. (ex. (examine|look) dagger)</b>\n");
        help.append("<b>\nShuttle is the same as movement but for traveling via shuttle. (ex. shuttle | drive)</b>\n");
        help.append("<b>\nTalk is how to interact with valid NPCs. (ex. (talk) curly. Continue conversation with Respond #</b>\n");
        help.append("<b>\nDrink is used to consume potions. (ex. (drink | apply) potion.</b>");
        return help.toString();
    }
    
    // Returns formatted string of items in the current room
    public String getCurrentRoomItems() {
        StringBuilder itemsString = new StringBuilder("Room Inventory:\n");
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        int size = currentRoom.getInventorySize();
        
        for (int i = 0; i < size; i++) {
            itemsString.append(i + 1).append("\t").append(currentRoom.getItemName(i)).append("\n");
        }
        
        return itemsString.toString();
    }
    
    // Returns formatted string of player's inventory
    public String getPlayerInventoryString() {
        StringBuilder inventoryString = new StringBuilder("Player Inventory:\n");
        int size = engine.getPlayer().getInventorySize();
        
        for (int i = 0; i < size; i++) {
            inventoryString.append(i + 1).append("\t").append(engine.getPlayer().getItemName(i)).append("\n");
        }
        
        return inventoryString.toString();
    }
    
    // Returns formatted string of player stats
    public String getPlayerInfo() {
        StringBuilder info = new StringBuilder("Player Info:\n");
        Player player = engine.getPlayer();
        info.append("Name: ").append(player.getName()).append("\nHealth: ").append(player.getHp()).append("\n");
        
        return info.toString();
    }
    
    // Returns formatted string of characters in the current room
    public String getRoomCharactersInfo() {
        StringBuilder charactersInfo = new StringBuilder("Characters in Room:\n");
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        int size = currentRoom.getCharacterContainerSize();
        
        for (int i = 0; i < size; i++) {
            charactersInfo.append(i + 1).append("\t")
                    .append(currentRoom.getCharacterName(i))
                    .append("\nHealth: ")
                    .append(currentRoom.getCharacterHealth(i))
                    .append("\n");
        }
        
        return charactersInfo.toString();
    }
    
    // Returns formatted string of room connections
    public String getRoomConnectionOutput() {
        StringBuilder roomConnectionOutput = new StringBuilder("Rooms available for each movement:\n");
        int outputNorth = engine.getMapOutput("North");
        int outputEast = engine.getMapOutput("East");
        int outputSouth = engine.getMapOutput("South");
        int outputWest = engine.getMapOutput("West");
        int outputShuttle = engine.getMapOutput("Shuttle");
        
        if (outputNorth == -1) {
            roomConnectionOutput.append("North : None\n");
        } else {
            roomConnectionOutput.append("North : ").append(engine.getRoomName(outputNorth)).append("\n");
        }
        
        if (outputEast == -1) {
            roomConnectionOutput.append("East : None\n");
        } else {
            roomConnectionOutput.append("East : ").append(engine.getRoomName(outputEast)).append("\n");
        }
        
        if (outputSouth == -1) {
            roomConnectionOutput.append("South : None\n");
        } else {
            roomConnectionOutput.append("South : ").append(engine.getRoomName(outputSouth)).append("\n");
        }
        
        if (outputWest == -1) {
            roomConnectionOutput.append("West : None\n");
        } else {
            roomConnectionOutput.append("West : ").append(engine.getRoomName(outputWest)).append("\n");
        }
        
        if (outputShuttle == -1) {
            roomConnectionOutput.append("Shuttle : None\n");
        } else {
            roomConnectionOutput.append("Shuttle : ").append(engine.getRoomName(outputShuttle)).append("\n");
        }
        
        return roomConnectionOutput.toString();
    }
    
    // Get current room image for display
    public String getCurrentRoomImage() {
        String roomName = engine.getCurrentRoomName();
        switch (roomName) {
            case "Manor South Lobby":
                return "images/default.jpg";
            case "Manors Road":
                return "images/default.jpg";
            case "The Student Union":
                return "images/default.jpg";
            case "The Shuttle Stop":
                return "images/default.jpg";
            default:
                return "images/default.jpg";
        }
    }
    
    // Generate overlay of items in room with persistent positions.
    public String getRoomItemsOverlay() {
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        
        for (int i = 0; i < currentRoom.getInventorySize(); i++) {
            String itemName = currentRoom.getItemName(i);
            double leftPercent, topPercent;
            
            // Check if the item already has an assigned position.
            if (itemPositions.containsKey(itemName)) {
                double[] pos = itemPositions.get(itemName);
                leftPercent = pos[0];
                topPercent = pos[1];
            } else {
                // Generate new random positions within the desired ranges.
                leftPercent = 40 + rand.nextDouble() * 40;  // anywhere from 40% to 80%
                topPercent = 65 + rand.nextDouble() * 15;     // between 65% and 80%
                // Store the generated position for this item.
                itemPositions.put(itemName, new double[]{leftPercent, topPercent});
            }
            
            sb.append("<img src='images/")
              .append(itemName)
              .append(".png' alt='")
              .append(itemName)
              .append("' style='position:absolute; left:")
              .append(leftPercent)
              .append("%; top:")
              .append(topPercent)
              .append("%; width:1in; height:auto; background-color: transparent;'/>");
        }
        
        return sb.toString();
    }
    
    // Generate overlay of characters in room
    public String getRoomCharactersOverlay() {
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        StringBuilder sb = new StringBuilder();
        
        // Arrange characters side by side
        for (int i = 0; i < currentRoom.getCharacterContainerSize(); i++) {
            String charName = currentRoom.getCharacterName(i);
            int charHealth = currentRoom.getCharacterHealth(i);
            
            // Calculate number of full hearts and check for a half heart
            int fullHearts = charHealth / 50;
            boolean showHalfHeart = (charHealth % 50) > 0;
            
            double leftOffsetInches = i * 2;   // horizontal offset for each character
            
            // Container div for hearts and character image
            sb.append("<div style='position:absolute; left:")
              .append(leftOffsetInches)
              .append("in; top:40%; width:2.5in; text-align:center;'>");
            
            // Row of hearts above the character image
            sb.append("<div style='height:0.5in;'>");
            
            // Append full hearts
            for (int h = 0; h < fullHearts; h++) {
                sb.append("<img src='images/heart.png' alt='heart' style='width:0.25in; height:auto; display:inline-block;'/>");
            }
            
            // Append a half heart if needed
            if (showHalfHeart) {
                sb.append("<img src='images/halfheart.png' alt='half heart' style='width:0.25in; height:auto; display:inline-block;'/>");
            }
            
            sb.append("</div>");
            
            // Character image
            sb.append("<img src='images/")
              .append(charName)
              .append(".png' alt='")
              .append(charName)
              .append("' style='width:2.5in; height:auto; background-color: transparent;'/>");
            
            sb.append("</div>");
        }
        
        return sb.toString();
    }
    
    // Main display method to construct Response object
    public Response display() {
        Response response = new Response(
            getCurrentRoomItems(),
            getPlayerInventoryString(),
            getRoomCharactersInfo(),
            getPlayerInfo(),
            getRoomConnectionOutput(),
            engine.getRunningMessage(),
            "",  // Error message (empty for now)
            getCurrentRoomImage(),
            engine.getCurrentRoomNumber(),
            getRoomItemsOverlay(),
            getRoomCharactersOverlay()
        );
        
        return response;
    }
}

