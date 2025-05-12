package models;

import java.util.*;

import GameEngine.GameEngine;
import models.Player;
import java.util.List;
import models.Quest;
import models.QuestDefinition;

public class UIManager {
    private GameEngine engine;
    // Map to store the position for each item based on its name.
    // The array holds two doubles: index 0 for leftPercent and index 1 for topPercent.
    private Map<String, double[]> itemPositions;
    
    public UIManager(GameEngine engine) {
        this.engine = engine;
        this.itemPositions = new HashMap<>();
        initArmorStyleSettings();
    }
    
    // Define a nested class to hold armor style parameters
    private static class ArmorStyle {
        String topPercent;
        String leftPercent;
        String width;
        String height;
        
        // Extra style parameters for two-piece armor (legs and gauntlets)
        String leftPieceTop;
        String leftPieceLeft;
        String leftPieceWidth;
        String leftPieceHeight;
        
        String rightPieceTop;
        String rightPieceLeft;
        String rightPieceWidth;
        String rightPieceHeight;
        
        // Constructor for single-piece armor (head, torso)
        public ArmorStyle(String top, String left, String width, String height) {
            this.topPercent = top;
            this.leftPercent = left;
            this.width = width;
            this.height = height;
            
            // Default values for two-piece components (not used for single-piece armor)
            this.leftPieceTop = top;
            this.leftPieceLeft = left;
            this.leftPieceWidth = width;
            this.leftPieceHeight = height;
            this.rightPieceTop = top;
            this.rightPieceLeft = left;
            this.rightPieceWidth = width;
            this.rightPieceHeight = height;
        }
        
        // Constructor for two-piece armor (legs, gauntlets)
        public ArmorStyle(String top, String left, String width, String height,
                         String leftPieceTop, String leftPieceLeft, String leftPieceWidth, String leftPieceHeight,
                         String rightPieceTop, String rightPieceLeft, String rightPieceWidth, String rightPieceHeight) {
            this.topPercent = top;
            this.leftPercent = left;
            this.width = width;
            this.height = height;
            
            // Two-piece component styles
            this.leftPieceTop = leftPieceTop;
            this.leftPieceLeft = leftPieceLeft;
            this.leftPieceWidth = leftPieceWidth;
            this.leftPieceHeight = leftPieceHeight;
            
            this.rightPieceTop = rightPieceTop;
            this.rightPieceLeft = rightPieceLeft;
            this.rightPieceWidth = rightPieceWidth;
            this.rightPieceHeight = rightPieceHeight;
        }
        
        public String getStyleString() {
            return "top:" + topPercent + "; left:" + leftPercent + 
                   "; width:" + width + "; height:" + height + ";";
        }
        
        public String getLeftPieceStyleString() {
            return "top:" + leftPieceTop + "; left:" + leftPieceLeft + 
                   "; width:" + leftPieceWidth + "; height:" + leftPieceHeight + ";";
        }
        
        public String getRightPieceStyleString() {
            return "top:" + rightPieceTop + "; left:" + rightPieceLeft + 
                   "; width:" + rightPieceWidth + "; height:" + rightPieceHeight + ";";
        }
    }
    
    // Maps for player-specific armor styles
    private static final Map<String, Map<ArmorSlot, ArmorStyle>> playerArmorStyles = new HashMap<>();
    
    // Initialize armor style settings for different players
    private void initArmorStyleSettings() {
        // Cooper's armor settings
        Map<ArmorSlot, ArmorStyle> cooperStyles = new HashMap<>();
        cooperStyles.put(ArmorSlot.HEAD, new ArmorStyle("-4%", "-10.3%", "3in", "auto"));
        cooperStyles.put(ArmorSlot.TORSO, new ArmorStyle("0%", "-2%", "2.6in", "auto"));
        
        // Cooper's leg armor (two-piece) - main, left leg, right leg
        cooperStyles.put(ArmorSlot.LEGS, new ArmorStyle(
            "-3%", "0%", "2.6in", "auto",                // Base style (not directly used)
            "-3%", "0%", "2.6in", "auto",                // Left leg specific styling
            "-5.4%", "-6%", "2.7in", "auto"              // Right leg specific styling
        ));
        
        // Cooper's gauntlet armor (two-piece) - main, left gauntlet, right gauntlet
        cooperStyles.put(ArmorSlot.ACCESSORY, new ArmorStyle(
            "-1%", "1%", "2.6in", "auto",                // Base style (not directly used)
            "-1%", "1%", "2.6in", "auto",                // Left gauntlet specific styling
            "-1%", "-6%", "2.6in", "auto"                // Right gauntlet specific styling
        ));
        playerArmorStyles.put("Cooper", cooperStyles);
        
        // Chuck's armor settings (medium character)
        Map<ArmorSlot, ArmorStyle> chuckStyles = new HashMap<>();
        chuckStyles.put(ArmorSlot.HEAD, new ArmorStyle("-6%", "-12%", "3.2in", "auto"));
        chuckStyles.put(ArmorSlot.TORSO, new ArmorStyle("-2%", "-3%", "2.8in", "auto"));
        
        // Chuck's leg armor (two-piece) - main, left leg, right leg
        chuckStyles.put(ArmorSlot.LEGS, new ArmorStyle(
            "-5%", "-1%", "2.7in", "auto",               // Base style (not directly used)
            "-5%", "-1%", "2.7in", "auto",               // Left leg specific styling
            "-7%", "-7%", "2.8in", "auto"                // Right leg specific styling
        ));
        
        // Chuck's gauntlet armor (two-piece) - main, left gauntlet, right gauntlet
        chuckStyles.put(ArmorSlot.ACCESSORY, new ArmorStyle(
            "-3%", "0%", "2.7in", "auto",                // Base style (not directly used)
            "-3%", "0%", "2.7in", "auto",                // Left gauntlet specific styling
            "-3%", "-7%", "2.7in", "auto"                // Right gauntlet specific styling
        ));
        playerArmorStyles.put("Chuck", chuckStyles);
        
        // Tank's armor settings (largest character)
        Map<ArmorSlot, ArmorStyle> tankStyles = new HashMap<>();
        tankStyles.put(ArmorSlot.HEAD, new ArmorStyle("-2%", "-14%", "3.5in", "auto"));
        tankStyles.put(ArmorSlot.TORSO, new ArmorStyle("-4%", "-4%", "3.0in", "auto"));
        
        // Tank's leg armor (two-piece) - main, left leg, right leg
        tankStyles.put(ArmorSlot.LEGS, new ArmorStyle(
            "-7%", "-2%", "3.0in", "auto",               // Base style (not directly used)
            "-7%", "-2%", "3.0in", "auto",               // Left leg specific styling
            "-9%", "-8%", "3.1in", "auto"                // Right leg specific styling
        ));
        
        // Tank's gauntlet armor (two-piece) - main, left gauntlet, right gauntlet
        tankStyles.put(ArmorSlot.ACCESSORY, new ArmorStyle(
            "-5%", "-1%", "2.8in", "auto",               // Base style (not directly used)
            "-5%", "-1%", "2.8in", "auto",               // Left gauntlet specific styling
            "-5%", "-8%", "2.9in", "auto"                // Right gauntlet specific styling
        ));
        playerArmorStyles.put("Tank", tankStyles);
    }
    
    // Get the appropriate armor style for the current player and slot
    private ArmorStyle getArmorStyle(String playerName, ArmorSlot slot) {
        // Default to Cooper's style if player not found or not specified
        if (playerName == null || !playerArmorStyles.containsKey(playerName)) {
            playerName = "Cooper";
        }
        
        Map<ArmorSlot, ArmorStyle> playerStyles = playerArmorStyles.get(playerName);
        return playerStyles.getOrDefault(slot, new ArmorStyle("0%", "0%", "2.5in", "auto"));
    }

    // Returns help text for player
    public String getHelp() {
        StringBuilder output = new StringBuilder("Help Menu:\n\n" +
            "- Basic Commands -\n\n" +
            "look (room/item/character): Examine the room, an item, or a character.\n" +
            "go (direction): Move player in the specified direction (North, South, East, West).\n" +
            "pickup/grab/take (item): Pick up an item from the current room.\n" +
            "drop (item): Drop an item from your inventory into the current room.\n\n" +
            
            "- Items and Combat -\n\n" +
            "hit (enemy) with (weapon): Attack an enemy with a specified weapon.\n" +
            "apply/drink (potion): Use a potion to restore health.\n" +
            "equip (armor): Put on a piece of armor to increase protection.\n" +
            "unequip (armor): Take off a piece of armor.\n\n" +
            
            "- Characters and Dialogue -\n\n" +
            "talk (character): Start a conversation with a character.\n" +
            "respond (1/2): Choose dialogue options during conversations.\n\n" +
            
            "- Companions -\n\n" +
            "choose (companion): Select a companion to join you.\n" +
            "shoo (companion): Send away your current companion.\n" +
            "takec (item): Take an item from your companion's inventory.\n" +
            "give (item): Give an item to your companion.\n\n" +
            
            "- Crafting -\n\n" +
            "disassemble (item): Break down an item into components.\n" +
            "combine (itemA) with (itemB): Combine two components into a new item.\n\n" +
            
            "You can also use shorthand directions like n, s, e, w for movement."
        );
        return output.toString();
    }

    // Returns formatted string of items in current room
    public String getCurrentRoomItems() {
        return getCurrentRoomItems(0); // Default to first player
    }
    
    // Returns formatted string of items in current room for specific player
    public String getCurrentRoomItems(int playerId) {
        StringBuilder roomItems = new StringBuilder("Items in Room:\n");
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        
        for (int i = 0; i < currentRoom.getInventorySize(); i++) {
            roomItems.append(i + 1).append("\t")
                    .append(currentRoom.getItemName(i)).append("\n");
        }
        
        return roomItems.toString();
    }

    // Return player's inventory as formatted string
    public String getPlayerInventoryString() {
        return getPlayerInventoryString(0); // Default to first player
    }
    
    // Return specified player's inventory as formatted string
    public String getPlayerInventoryString(int playerId) {
        Player player = engine.getPlayerById(playerId);
        if (player == null || player.getInventory() == null || player.getInventorySize() == 0) {
            return "";
        }
        
        // Use the new UI style from joe-ui branch, but with multi-player support
        StringBuilder inventoryString = new StringBuilder();
        int size = player.getInventorySize();
        for (int i = 0; i < size; i++) {
            inventoryString.append(player.getItemName(i));
            if (i < size - 1) {
                inventoryString.append(",");
            }
        }
        return inventoryString.toString();
    }
    
    public String getCompanionInventoryString() {
        return getCompanionInventoryString(0); // Default to first player
    }
    
    public String getCompanionInventoryString(int playerId) {
        StringBuilder companionInventory = new StringBuilder();
        Player player = engine.getPlayerById(playerId);
        
        if (player == null || player.getPlayerCompanion() == null) {
            companionInventory.append("(no items)\n");
            return companionInventory.toString();
        }
        
        // Get companion inventory size and iterate through items
        Companion companion = player.getPlayerCompanion();
        int size = companion.getInventorySize();
        
        if (size == 0) {
            companionInventory.append("(no items)\n");
            return companionInventory.toString();
        }
        
        for (int i = 0; i < size; i++) {
            companionInventory.append(i + 1)
                      .append("\t")
                      .append(companion.getItemName(i))
                      .append("\n");
        }
        
        return companionInventory.toString();
    }
    
    public String getPlayerCompanionString() {
        return getPlayerCompanionString(0); // Default to first player
    }
    
    public String getPlayerCompanionString(int playerId) {
    	StringBuilder playerCompanionString = new StringBuilder("Player Companion: \n");
    	Player p = engine.getPlayerById(playerId);
    	
    	if(p == null || p.getPlayerCompanion() == null) {
    		playerCompanionString.append("(no companions) \n");
    		return playerCompanionString.toString();
    	}
    	
    	int size = 1;
    	for (int i = 0; i < size; i++) {
    		playerCompanionString
    			.append(i+1)
    			.append("\t")
    			.append(p.getPlayerCompanion().getName())
    			.append("\n");
    	}
    	return playerCompanionString.toString();
    }

    
    // Returns formatted string of player stats
    public String getPlayerInfo() {
        return getPlayerInfo(0); // Default to first player
    }
    
    // Returns formatted string of specified player stats
    public String getPlayerInfo(int playerId) {
        StringBuilder info = new StringBuilder("Player Info:\n");
        Player player = engine.getPlayerById(playerId);
        if (player == null) {
            info.append("(no player loaded)\n");
            return info.toString();
        }

        info.append("Name: ")
            .append(player.getName() != null ? player.getName() : "(unknown)")
            .append("\nHealth: ")
            .append(player.getHp())
            .append("\n");
        
        info.append("Equipped Armor:\n");
            for (ArmorSlot slot : ArmorSlot.values()) {
                Armor a = player.getEquippedArmor(slot);
                info.append(slot.name())
                    .append(": ")
                    .append(a != null ? a.getName() : "none")
                    .append("\n");
            }

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
    
    public String getRoomCompanionsInfo() {
    	StringBuilder companionsInfo = new StringBuilder("Companions in Room: \n");
    	Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
    	 int size = currentRoom.getCompanionContainerSize();
    	 for (int i = 0; i < size; i++) {
             companionsInfo.append(i + 1).append("\t")
                     .append(currentRoom.getCompanion(i).getName())
                     .append("\nHealth: ")
                     .append(currentRoom.getCompanionHealth(i))
                     .append("\n");
         }
    	 
    	 return companionsInfo.toString();
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
        if (roomName == null) {
            return "images/default.jpg";
        }
        String fileName = roomName.replaceAll("\\s+", "") + ".png";
        return "images/" + fileName;
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
                leftPercent = 30 + rand.nextDouble() * 30;  // anywhere from 30% to 60%
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
              .append("%; width:1in; height:auto; background-color: transparent;'/>\n");
        }
        
        return sb.toString();
    }
    
    // Modified method for character overlay with health bars
    public String getRoomCharactersOverlay() {
        return getRoomCharactersOverlay(0); // Default to first player
    }
    
    public String getRoomCharactersOverlay(int playerId) {
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        StringBuilder sb = new StringBuilder();
        
        // Get player's max health using getMaxHp() method
        Player player = engine.getPlayerById(playerId);
        int maxHealth = 100; // Default fallback
        if (player != null) {
            // Use the getMaxHp() method instead of current hp
            maxHealth = player.getMaxHp();
        }
        
        // 1. Render all NPCs first
        for (int i = 0; i < currentRoom.getCharacterContainerSize(); i++) {
            String charName = currentRoom.getCharacterName(i);
            int charHealth = currentRoom.getCharacterHealth(i);
            
            // Get character object to access its maxHp
            Character character = currentRoom.getCharacter(i);
            int charMaxHealth = maxHealth; // Default to player's max if can't get the character
            
            if (character != null) {
                charMaxHealth = character.getMaxHp();
            }
            
            // Calculate health percentage for the bar using character's own max health
            double healthPercentage = (double)charHealth / charMaxHealth;
            // Cap at 100% just in case
            healthPercentage = Math.min(healthPercentage, 1.0);
            
            double leftOffsetInches = i * 2;

            sb.append("<div style='position:absolute; left:")
              .append(leftOffsetInches)
              .append("in; top:40%; width:2.5in; text-align:center;'>\n");

            // Health bar with actual max health values - smaller size
            sb.append("<div style='height:0.2in; width:80%; margin:0 auto; background-color:#ccc; border-radius:3px; margin-bottom:0.15in;'>\n")
              .append("<div style='height:100%; width:")
              .append(healthPercentage * 100) // Width as percentage of the container
              .append("%; background-color:#2ecc71; border-radius:3px;'></div>\n")
              .append("<div style='margin-top:-0.2in; text-align:center; color:black; font-size:0.9em; font-weight:bold;'>")
              .append(charHealth)
              .append("/").append(charMaxHealth).append("</div>\n")
              .append("</div>\n");

            sb.append("<img src='images/")
              .append(charName)
              .append(".png' alt='")
              .append(charName)
              .append("' style='width:2.5in; height:auto; background-color: transparent;'/>\n");

            sb.append("</div>\n");
        }

        // 2. Render the Player separately on the far right
        if (player != null) {
            int playerHealth = player.getHp();
            int playerMaxHealth = player.getMaxHp();
            // Calculate health percentage for the bar using player's own max health
            double healthPercentage = (double)playerHealth / playerMaxHealth;
            String playerName = player.getName();

            double leftOffsetInches = 8; // Far right
            double topOffset = 40;

            sb.append("<div style='position:absolute; left:")
              .append(leftOffsetInches)
              .append("in; top:")
              .append(topOffset)
              .append("%; width:2.5in; text-align:center;'>\n");

            // Health bar using player's max health - smaller size
            sb.append("<div style='height:0.2in; width:80%; margin:0 auto; background-color:#ccc; border-radius:3px; margin-bottom:0.15in;'>\n")
              .append("<div style='height:100%; width:")
              .append(healthPercentage * 100) // Width as percentage of the container
              .append("%; background-color:#2ecc71; border-radius:3px;'></div>\n")
              .append("<div style='margin-top:-0.2in; text-align:center; color:black; font-size:0.9em; font-weight:bold;'>")
              .append(playerHealth)
              .append("/").append(playerMaxHealth).append("</div>\n")
              .append("</div>\n");

            // Base player image
            sb.append("<div style='position: relative; display: inline-block;'>\n");
            sb.append("<img src='images/")
              .append("Cooper")
              .append(".png' alt='")
              .append(playerName)
              .append("' style='width:2.5in; height:auto; background-color: transparent;'/>\n");

            // Equipped armor overlays with player-specific styling
            for (ArmorSlot slot : ArmorSlot.values()) {
                Armor equipped = player.getEquippedArmor(slot);
                if (equipped == null) continue;
                
                // Get player-specific armor style for this slot
                ArmorStyle style = getArmorStyle(playerName, slot);

                switch (slot) {
                  case HEAD:
                  case TORSO:
                    // Single-piece overlay with player-specific styling
                    String file = equipped.getName().replaceAll("\\s+","") + ".png";
                    sb.append("<img src='images/")
                      .append(file)
                      .append("' alt='").append(slot.name()).append("' ")
                      .append("style='position:absolute; ")
                      .append(style.getStyleString())
                      .append(" background-color:transparent;'/>");
                    break;

                  case LEGS:
                    // Two-piece pants with player-specific styling for each leg
                    sb.append("<img src='images/leftleg.png' alt='left leg' ")
                      .append("style='position:absolute; ")
                      .append(style.getLeftPieceStyleString())
                      .append(" background-color:transparent;'/>");
                      
                    sb.append("<img src='images/rightleg.png' alt='right leg' ")
                      .append("style='position:absolute; ")
                      .append(style.getRightPieceStyleString())
                      .append(" background-color:transparent;'/>");
                    break;

                  case ACCESSORY:
                    // Two-piece gauntlets with player-specific styling for each gauntlet
                    sb.append("<img src='images/leftgauntlet.png' alt='left gauntlet' ")
                      .append("style='position:absolute; ")
                      .append(style.getLeftPieceStyleString())
                      .append(" background-color:transparent;'/>");
                      
                    sb.append("<img src='images/rightgauntlet.png' alt='right gauntlet' ")
                      .append("style='position:absolute; ")
                      .append(style.getRightPieceStyleString())
                      .append(" background-color:transparent;'/>");
                    break;
                }
            }

            sb.append("</div>\n</div>\n");
        }

        return sb.toString();
    }

    // Method to allow adjusting single-piece armor styles at runtime
    public void updateArmorStyle(String playerName, ArmorSlot slot, String top, String left, String width, String height) {
        if (!playerArmorStyles.containsKey(playerName)) {
            playerArmorStyles.put(playerName, new HashMap<>());
        }
        
        playerArmorStyles.get(playerName).put(slot, new ArmorStyle(top, left, width, height));
    }
    
    // Method to allow adjusting two-piece armor styles at runtime
    public void updateTwoPieceArmorStyle(String playerName, ArmorSlot slot, 
                                        String leftPieceTop, String leftPieceLeft, String leftPieceWidth, String leftPieceHeight,
                                        String rightPieceTop, String rightPieceLeft, String rightPieceWidth, String rightPieceHeight) {
        if (!playerArmorStyles.containsKey(playerName)) {
            playerArmorStyles.put(playerName, new HashMap<>());
        }
        
        // Use dummy values for the base style since it's not directly used for two-piece armor
        playerArmorStyles.get(playerName).put(
            slot, 
            new ArmorStyle(
                "0%", "0%", "auto", "auto",  // Base style (dummy values)
                leftPieceTop, leftPieceLeft, leftPieceWidth, leftPieceHeight,  // Left piece
                rightPieceTop, rightPieceLeft, rightPieceWidth, rightPieceHeight  // Right piece
            )
        );
    }
    
    public String getRoomCompanionsOverlay() {
        return getRoomCompanionsOverlay(0); // Default to first player
    }
    
    public String getRoomCompanionsOverlay(int playerId) {
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        StringBuilder sb = new StringBuilder();
        
        // 1. Render all companions with their own max health
        for (int i = 0; i < currentRoom.getCompanionContainerSize(); i++) {
            String compName = currentRoom.getCompanionName(i);
            Companion companion = currentRoom.getCompanion(i);
            
            if (companion == null) continue;
            
            int compHealth = companion.getHp();
            int compMaxHealth = companion.getMaxHp();
            
            // Calculate health percentage for the bar using companion's own max health
            double healthPercentage = (double)compHealth / compMaxHealth;
            // Cap at 100% just in case
            healthPercentage = Math.min(healthPercentage, 1.0);
            
            double leftOffsetInches = 5;   // horizontal offset for each companion
            
            // Container div for health bar and character image
            sb.append("<div style='position:absolute; left:")
              .append(leftOffsetInches)
              .append("in; top:40%; width:2.5in; text-align:center;'>");
            
            // Health bar with actual max health values - smaller size
            sb.append("<div style='height:0.2in; width:80%; margin:0 auto; background-color:#ccc; border-radius:3px; margin-bottom:0.15in;'>\n")
              .append("<div style='height:100%; width:")
              .append(healthPercentage * 100) // Width as percentage of the container
              .append("%; background-color:#2ecc71; border-radius:3px;'></div>\n")
              .append("<div style='margin-top:-0.2in; text-align:center; color:black; font-size:0.9em; font-weight:bold;'>")
              .append(compHealth)
              .append("/").append(compMaxHealth).append("</div>\n")
              .append("</div>\n");
            
            // Character image
            sb.append("<img src='images/")
              .append(compName)
              .append(".png' alt='")
              .append(compName)
              .append("' style='width:2.5in; height:auto; background-color: transparent;'/>");
            
            sb.append("</div>");
        }
        
        return sb.toString();
    }
    
    // Generate overlay of active quests
    public String getQuestOverlay() {
        return getQuestOverlay(0); // Default to first player
    }
    
    public String getQuestOverlay(int playerId) {
        Player player = engine.getPlayerById(playerId);
        if (player == null) {
            return "Active Quests:<br/>(none)<br/>";
        }
        
        List<Quest> qs = player.getActiveQuests();
        StringBuilder sb = new StringBuilder();
        if (qs.isEmpty()) {
            sb.append("(none)<br/>");
        } else {
            for (int i = 0; i < qs.size(); i++) {
                Quest q = qs.get(i);
                QuestDefinition d = q.getDef();
                sb.append(i + 1)
                  .append(". ")
                  .append(d.getName())
                  .append(" (")
                  .append(q.getProgress())
                  .append("/")
                  .append(d.getTargetCount())
                  .append(")<br/>");
            }
        }
        return sb.toString();
    }

    // Helper to compute player health percentage for status bar
    private int getPlayerHealthPercent() {
        Player player = engine.getPlayer();
        if (player == null) return 0;
        int maxHp = player.getMaxHp();
        if (maxHp <= 0) return 0;
        return (int) ((player.getHp() * 100.0) / maxHp);
    }

    // Helper to compute player skill points percentage out of 3
    private int getPlayerSkillPointsPercent() {
        Player player = engine.getPlayer();
        if (player == null) return 0;
        int sp = player.getSkillPoints();
        return (int) ((sp * 100.0) / 3);
    }

    // Generate JavaScript scene-data and renderEntities call
    private String getSceneEntitiesData() {
        StringBuilder sb = new StringBuilder();
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        Player player = engine.getPlayer();

        sb.append("<script>\n");
        sb.append("const sceneData = {\n");
        // Player data
        sb.append("  player: {");
        if (player != null) {
            sb.append("name: 'Cooper',");
            sb.append(" health: " + player.getHp() + ",");
            sb.append(" maxHealth: " + player.getMaxHp() + ",");
            sb.append(" skillPoints: " + player.getSkillPoints() + ",");
            sb.append(" position: { left: 80, top: 40 }\n");
        }
        sb.append("  },\n");
        // Enemies data
        sb.append("  enemies: [\n");
        for (int i = 0; i < currentRoom.getCharacterContainerSize(); i++) {
            if (i > 0) sb.append(",\n");
            sb.append("    {");
            sb.append(" name: '" + currentRoom.getCharacterName(i) + "',");
            sb.append(" health: " + currentRoom.getCharacterHealth(i) + ",");
            sb.append(" maxHealth: " + currentRoom.getCharacter(i).getMaxHp() + ",");
            sb.append(" position: { left: " + (20 + i*15) + ", top: 40 } }");
        }
        sb.append("\n  ],\n");
        // Items data
        sb.append("  items: [\n");
        for (int i = 0; i < currentRoom.getInventorySize(); i++) {
            if (i > 0) sb.append(",\n");
            sb.append("    { name: '" + currentRoom.getItemName(i) + "', position: { left: " + (40 + i*10) + ", top: 60 } }");
        }
        sb.append("\n  ]\n");
        sb.append("};\n");
        // Render function
        sb.append("function renderEntities() {\n");
        // Clear and render player
        sb.append("  const playerContainer = document.getElementById('player-container');\n");
        sb.append("  playerContainer.innerHTML = '';\n");
        sb.append("  if (sceneData.player && sceneData.player.name) {\n");
        sb.append("    const p = sceneData.player;\n");
        sb.append("    const pel = document.createElement('div'); pel.className = 'entity'; pel.style.left = p.position.left + '%'; pel.style.top = p.position.top + '%';\n");
        sb.append("    const phb = document.createElement('div'); phb.className = 'entity-health-bar'; const php = Math.floor((p.health / p.maxHealth) * 100); phb.innerHTML = `<div class='entity-health-fill' style='width:${php}%'>${p.health}</div>`; pel.appendChild(phb);\n");
        sb.append("    const pimg = document.createElement('img'); pimg.src = 'images/Cooper.png'; pimg.alt = p.name; pimg.style.width = '350px'; pel.appendChild(pimg);\n");
        sb.append("    playerContainer.appendChild(pel);\n");
        sb.append("  }\n");
        // Clear and render enemies
        sb.append("  const enemiesContainer = document.getElementById('enemies-container');\n");
        sb.append("  enemiesContainer.innerHTML = '';\n");
        sb.append("  sceneData.enemies.forEach(enemy => {\n");
        sb.append("    const el = document.createElement('div'); el.className = 'entity'; el.style.left = enemy.position.left + '%'; el.style.top = enemy.position.top + '%';\n");
        sb.append("    const hb = document.createElement('div'); hb.className = 'entity-health-bar'; const hpp = Math.floor((enemy.health / enemy.maxHealth) * 100); hb.innerHTML = `<div class='entity-health-fill' style='width:${hpp}%'>${enemy.health}</div>`; el.appendChild(hb);\n");
        sb.append("    const img = document.createElement('img'); img.src = 'images/' + enemy.name + '.png'; img.alt = enemy.name; img.style.width = '350px'; el.appendChild(img);\n");
        sb.append("    enemiesContainer.appendChild(el);\n");
        sb.append("  });\n");
        // Clear and render items
        sb.append("  const itemsContainer = document.getElementById('items-container');\n");
        sb.append("  itemsContainer.innerHTML = '';\n");
        sb.append("  sceneData.items.forEach(item => {\n");
        sb.append("    const el = document.createElement('div'); el.className = 'entity'; el.style.left = item.position.left + '%'; el.style.top = item.position.top + '%';\n");
        sb.append("    const img = document.createElement('img'); img.src = 'images/' + item.name + '.png'; img.alt = item.name; img.style.width = '280px'; el.appendChild(img);\n");
        sb.append("    itemsContainer.appendChild(el);\n");
        sb.append("  });\n");
        sb.append("}\n");
        sb.append("window.onload = renderEntities;\n");
        sb.append("</script>");
        return sb.toString();
    }

    // Main display method to construct Response object
    public Response display() {
        return display(0); // Default to first player
    }
    
    // Display for specific player
    public Response display(int playerId) {
        Response response = new Response(
            getCurrentRoomItems(playerId),
            getPlayerInventoryString(playerId),
            getPlayerCompanionString(playerId),
            getCompanionInventoryString(playerId),
            getRoomCharactersInfo(),
            getRoomCompanionsInfo(),
            getPlayerInfo(playerId),
            getRoomConnectionOutput(),
            engine.getRunningMessage(),
            "",  // Error message
            getCurrentRoomImage(),
            engine.getCurrentRoomNumber(),
            getRoomItemsOverlay(),
            getRoomCharactersOverlay(playerId),
            getRoomCompanionsOverlay(playerId),
            getQuestOverlay(playerId)
        );
        // Room connections
        int north = engine.getMapOutput("North");
        response.setNorthRoom(north != -1 ? engine.getRoomName(north) : null);
        int east = engine.getMapOutput("East");
        response.setEastRoom(east != -1 ? engine.getRoomName(east) : null);
        int south = engine.getMapOutput("South");
        response.setSouthRoom(south != -1 ? engine.getRoomName(south) : null);
        int west = engine.getMapOutput("West");
        response.setWestRoom(west != -1 ? engine.getRoomName(west) : null);
        // After computing room connections and before player status
        // Set the current room name for the UI header
        response.setRoomName(engine.getCurrentRoomName());
        // Player status
        Player player = engine.getPlayer();
        if (player != null) {
            response.setPlayerCurrentHP(player.getHp());
            response.setPlayerMaxHP(player.getMaxHp());
            response.setPlayerHealthPercent(getPlayerHealthPercent());
            response.setPlayerSkillPoints(player.getSkillPoints());
            response.setPlayerSkillPointsPercent(getPlayerSkillPointsPercent());
        }
        // Inject scene data
        response.setSceneEntitiesData(getSceneEntitiesData());
        return response;
    }
}