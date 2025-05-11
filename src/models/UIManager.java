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
    }
    
 // Style settings per armor slot
    private static final Map<ArmorSlot, String> armorSlotStyles = new HashMap<>();
    static {
        armorSlotStyles.put(ArmorSlot.HEAD,      "top:0%; left:20%; width:1in;");
        armorSlotStyles.put(ArmorSlot.TORSO,     "top:25%; left:10%; width:2.2in;");
        armorSlotStyles.put(ArmorSlot.LEGS,      "top:55%; left:15%; width:2in;");
        armorSlotStyles.put(ArmorSlot.ACCESSORY, "top:30%; left:0%; width:2.5in;");
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
        Player p = engine.getPlayer();
        if (p == null || p.getInventory() == null || p.getInventorySize() == 0) {
            return "";
        }
        StringBuilder inventoryString = new StringBuilder();
        int size = p.getInventorySize();
        for (int i = 0; i < size; i++) {
            inventoryString.append(p.getItemName(i));
            if (i < size - 1) {
                inventoryString.append(",");
            }
        }
        return inventoryString.toString();
    }
    
    public String getCompanionInventoryString() {
        StringBuilder inventoryString = new StringBuilder();
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
    
    // Generate overlay of characters in room
    public String getRoomCharactersOverlay() {
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        StringBuilder sb = new StringBuilder();
        
        // 1. Render all NPCs first
        for (int i = 0; i < currentRoom.getCharacterContainerSize(); i++) {
            String charName = currentRoom.getCharacterName(i);
            int charHealth = currentRoom.getCharacterHealth(i);

            int fullHearts = charHealth / 50;
            boolean showHalfHeart = (charHealth % 50) > 0;

            double leftOffsetInches = i * 2;

            sb.append("<div style='position:absolute; left:")
              .append(leftOffsetInches)
              .append("in; top:40%; width:2.5in; text-align:center;'>\n");

            sb.append("<div style='height:0.5in;'>\n");
            for (int h = 0; h < fullHearts; h++) {
                sb.append("<img src='images/heart.png' alt='heart' style='width:0.25in; height:auto; display:inline-block;'/>\n");
            }
            if (showHalfHeart) {
                sb.append("<img src='images/halfheart.png' alt='half heart' style='width:0.25in; height:auto; display:inline-block;'/>\n");
            }
            sb.append("</div>\n");

            sb.append("<img src='images/")
              .append(charName)
              .append(".png' alt='")
              .append(charName)
              .append("' style='width:2.5in; height:auto; background-color: transparent;'/>\n");

            sb.append("</div>\n");
        }

     // 2. Render the Player separately on the far right
        Player player = engine.getPlayer();
        if (player != null) {
            int playerHealth = player.getHp();
            int fullHearts = playerHealth / 50;
            boolean showHalfHeart = (playerHealth % 50) > 0;

            double leftOffsetInches = 8; // Far right
            double topOffset = 40;

            sb.append("<div style='position:absolute; left:")
              .append(leftOffsetInches)
              .append("in; top:")
              .append(topOffset)
              .append("%; width:2.5in; text-align:center;'>\n");

            // Hearts
            sb.append("<div style='height:0.5in;'>\n");
            for (int h = 0; h < fullHearts; h++) {
                sb.append("<img src='images/heart.png' alt='heart' style='width:0.25in; height:auto; display:inline-block;'/>\n");
            }
            if (showHalfHeart) {
                sb.append("<img src='images/halfheart.png' alt='half heart' style='width:0.25in; height:auto; display:inline-block;'/>\n");
            }
            sb.append("</div>\n");

            // Base player image
            sb.append("<div style='position: relative; display: inline-block;'>\n");
            sb.append("<img src='images/")
              .append(player.getName())
              .append(".png' alt='")
              .append(player.getName())
              .append("' style='width:2.5in; height:auto; background-color: transparent;'/>\n");

            // Equipped armor overlays (must exist in /images)
            for (ArmorSlot slot : ArmorSlot.values()) {
                Armor equipped = player.getEquippedArmor(slot);
                if (equipped != null) {
                    String imageName = equipped.getName().replaceAll("\\s+", "") + ".png";
                    String slotClass = slot.name().toLowerCase(); // e.g., head, legs, accessory

                    String customStyle = armorSlotStyles.getOrDefault(slot, "top:0; left:0; width:2.5in;");

                    sb.append("<img src='images/")
                      .append(imageName)
                      .append("' alt='")
                      .append(slot.name())
                      .append("' style='position:absolute; ")
                      .append(customStyle)
                      .append(" height:auto; background-color: transparent;'/>\n");

                }
            }

            sb.append("</div>\n</div>\n");
        }


        return sb.toString();
    }

    
    
    public String getRoomCompanionsOverlay() {
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        StringBuilder sb = new StringBuilder();
        
        // Arrange characters side by side
        for (int i = 0; i < currentRoom.getCompanionContainerSize(); i++) {
            String compName = currentRoom.getCompanionName(i);
            int compHealth = currentRoom.getCompanionHealth(i);
            
            // Calculate number of full hearts and check for a half heart
            int fullHearts = compHealth / 50;
            boolean showHalfHeart = (compHealth % 50) > 0;
            
            double leftOffsetInches = 5;   // horizontal offset for each companion
            
            // Container div for hearts and character image
            sb.append("<div style='position:absolute; left:")
              .append(leftOffsetInches)
              .append("in; top:40%; width:1.5in; text-align:center;'>");
            
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
        List<Quest> qs = engine.getPlayer().getActiveQuests();
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
            sb.append("name: '" + player.getName() + "',");
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
        sb.append("    const pimg = document.createElement('img'); pimg.src = 'images/' + p.name + '.png'; pimg.alt = p.name; pimg.style.width = '350px'; pel.appendChild(pimg);\n");
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
            getQuestOverlay()
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
