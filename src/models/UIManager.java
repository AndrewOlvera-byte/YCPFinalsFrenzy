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
        StringBuilder help = new StringBuilder();
        help.append("<b>\nTake, get, grab, pickup are for picking up an item. (ex. (get|grab|pickup) sword)</b>\n");
        help.append("<b>\nDrop is to drop an item (ex. drop dagger)</b>\n");
        help.append("<b>\nAttack, swing, slash, hit, strike are for attacking an enemy. (ex. (attack|swing|slash|hit|strike) moe with trident)</b>\n");
        help.append("<b>\nGo, move, walk are for moving in a direction. (ex. (walk|move) north)</b>\n");
        help.append("<b>\nExamine and look are for looking at the description of an item or room. (ex. (examine|look) dagger)</b>\n");
        help.append("<b>\nShuttle is the same as movement but for traveling via shuttle. (ex. shuttle | drive)</b>\n");
        help.append("<b>\nTalk is how to interact with valid NPCs. (ex. (talk) curly. Continue conversation with Respond #</b>\n");
        help.append("<b>\nDrink is used to consume potions. (ex. (drink | apply) potion.</b>\n");
        help.append("<b>\nDisassemble items into components if possible. (ex. disassemble gold key)</b>\n");
        help.append("<b>\nCombine two items without naming the result. (ex. combine string and stick)</b>\n");
        help.append("<b>\nEquip a piece of armor. (ex. equip helmet)</b>\n");
        help.append("<b>\nTo unequip a piece of armor. (ex. unequip head)</b>\n");
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
        Player p = engine.getPlayer();
        // ← these two lines are the "easiest fix"
        if (p == null || p.getInventory() == null || p.getInventorySize() == 0) {
            inventoryString.append("(no items)\n");
            return inventoryString.toString();
        }

        int size = p.getInventorySize();
        for (int i = 0; i < size; i++) {
            inventoryString
              .append(i + 1)
              .append("\t")
              .append(p.getItemName(i))
              .append("\n");
        }
        return inventoryString.toString();
    }
    
    public String getCompanionInventoryString() {
        StringBuilder inventoryString = new StringBuilder("Companion Inventory:\n");
        Companion companion = engine.getPlayer().getPlayerCompanion();
        // ← these two lines are the "easiest fix"
        if (companion == null || companion.getInventory() == null || companion.getInventorySize() == 0) {
            inventoryString.append("(no items)\n");
            return inventoryString.toString();
        }

        int size = companion.getInventorySize();
        for (int i = 0; i < size; i++) {
            inventoryString
              .append(i + 1)
              .append("\t")
              .append(companion.getItemName(i))
              .append("\n");
        }
        return inventoryString.toString();
    }
    
    public String getPlayerCompanionString() {
    	StringBuilder playerCompanionString = new StringBuilder("Player Companion: \n");
    	Player p = engine.getPlayer();
    	
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
        StringBuilder info = new StringBuilder("Player Info:\n");
        Player player = engine.getPlayer();
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
 
    // Generate overlay of items in room with persistent positions.
    public String getRoomItemsOverlay() {
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        
        // Define grid parameters
        int maxFloorItemsPerRow = 4; // Maximum items per row
        int totalItems = currentRoom.getInventorySize();
        
        // Calculate number of rows needed
        int rows = (int) Math.ceil((double) totalItems / maxFloorItemsPerRow);
        
        // 2D array to track grid occupancy
        boolean[][] gridOccupied = new boolean[rows][maxFloorItemsPerRow];
        
        for (int i = 0; i < totalItems; i++) {
            String itemName = currentRoom.getItemName(i);
            double leftPercent, topPercent;
            
            // Check if the item already has an assigned position.
            if (itemPositions.containsKey(itemName)) {
                double[] pos = itemPositions.get(itemName);
                leftPercent = pos[0];
                topPercent = pos[1];
            } else {
                // Determine row and column placement
                int row = i / maxFloorItemsPerRow;
                int col = i % maxFloorItemsPerRow;
                gridOccupied[row][col] = true;
                
                // Calculate base position with some randomness within grid cell
                // Spread horizontally between 25% and 75% to leave room for characters
                leftPercent = 25 + (col * 10) ;
                
                // Position items vertically based on row
                // First row (bottom/floor) around 80-82%
                // Second row higher up, around 70-72%
                // Subsequent rows continue upward
                if (row == 0) {
                    topPercent = 80 ;
                } else {
                    topPercent = 70 - ((row - 1) * 7) ;
                }
                
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
              .append("%; width:auto; max-height:1in; object-fit:contain; background-color: transparent;'/>\n");
        }
        
        return sb.toString();
    } 
    // Generate overlay of characters in room
 // Here are the modified methods from the UIManager class to implement health bars

 // Modified method for character overlay with health bars
 // Modified method for character overlay with health bars
    public String getRoomCharactersOverlay() {
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        StringBuilder sb = new StringBuilder();
        
        // Get player's max health using getMaxHp() method
        Player player = engine.getPlayer();
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
              .append(playerName)
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
                    String file = equipped.getName().replaceAll("\\s+","") + "2.png";
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
 // Modified method for companion overlay with health bars

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
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        StringBuilder sb = new StringBuilder();

        // Calculate base left offset and increment
        double baseLeftOffsetInches = 7.25; // starting horizontal position
        double companionWidthInches = 1.5; // width of companion image
        double companionSpacingInches = 1.0; // space between companions

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

            // Calculate left offset for this companion
            double leftOffsetInches = baseLeftOffsetInches + (i * (companionWidthInches + companionSpacingInches));

            // Container div for health bar and character image
            sb.append("<div style='position:absolute; left:")
              .append(leftOffsetInches)
              .append("in; top:70%; width:").append(companionWidthInches)
              .append("in; display:flex; flex-direction:column; align-items:center;'>");

            // Health bar container with centered content
            sb.append("<div style='width:80%; height:0.2in; background-color:#ccc; border-radius:3px; margin-bottom:0.15in; position:relative;'>\n")
              .append("<div style='position:absolute; height:100%; width:")
              .append(healthPercentage * 100) // Width as percentage of the container
              .append("%; background-color:#2ecc71; border-radius:3px;'></div>\n")
              .append("<div style='position:absolute; width:100%; text-align:center; color:black; font-size:0.9em; font-weight:bold; z-index:1;'>")
              .append(compHealth)
              .append("/").append(compMaxHealth).append("</div>\n")
              .append("</div>\n");

            // Character image
            sb.append("<img src='images/")
              .append(compName)
              .append(".png' alt='")
              .append(compName)
              .append("' style='width:1.5in; height:auto; background-color: transparent;'/>");

            sb.append("</div>");
        }

        return sb.toString();
    }
    // Generate overlay of active quests
    public String getQuestOverlay() {
        List<Quest> qs = engine.getPlayer().getActiveQuests();
        StringBuilder sb = new StringBuilder();
        sb.append("Active Quests:<br/>");
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

    // Generate overlay of player's inventory items at the top of the room image
    public String getPlayerInventoryOverlay() {
        Player player = engine.getPlayer();
        StringBuilder sb = new StringBuilder();
        
        // Add grey background strip at the top
        sb.append("<div style='position:absolute; left:0; top:0; width:100%; height:1in; background-color:rgba(128, 128, 128, 0.7); z-index:1;'></div>\n");
        
        if (player == null || player.getInventory() == null || player.getInventorySize() == 0) {
            return sb.toString(); // Return the background strip even if no items
        }
        
        int totalItems = player.getInventorySize();
        double itemWidth = 0.8; // Reduced width of each item in inches
        double spacing = 0.2; // Spacing between items in inches
        double totalWidth = (itemWidth + spacing) * totalItems - spacing; // Total width of all items
        double startLeft = (10 - totalWidth) / 2; // Center the items horizontally (assuming 10in width)
        
        for (int i = 0; i < totalItems; i++) {
            String itemName = player.getItemName(i);
            double leftPosition = startLeft + (i * (itemWidth + spacing));
            
            sb.append("<img src='images/")
              .append(itemName)
              .append(".png' alt='")
              .append(itemName)
              .append("' style='position:absolute; left:")
              .append(leftPosition)
              .append("in; top:0.1in; width:")
              .append(itemWidth)
              .append("in; height:")
              .append(itemWidth)
              .append("in; object-fit:contain; background-color: transparent; z-index:2;'/>\n");
        }
        
        return sb.toString();
    }

    // Main display method to construct Response object
    public Response display() {
        Response response = new Response(
            getCurrentRoomItems(),
            getPlayerInventoryString(),
            getPlayerCompanionString(),
            getCompanionInventoryString(),
            getRoomCharactersInfo(),
            getRoomCompanionsInfo(),
            getPlayerInfo(),
            getRoomConnectionOutput(),
            engine.getRunningMessage(),
            "",  // Error message
            getCurrentRoomImage(),
            engine.getCurrentRoomNumber(),
            getRoomItemsOverlay(),
            getRoomCharactersOverlay(),
            getRoomCompanionsOverlay(),
            getQuestOverlay(),
            getPlayerInventoryOverlay()  // Add the new overlay
        );
        return response;
    }
}