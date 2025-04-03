package GameEngine;

import java.util.ArrayList;
import java.util.*;
import java.util.Random;
import java.util.HashMap;

import models.Room;
import models.Utility;
import models.Player;
import models.Character;
import models.Connections;
import models.Inventory;
import models.Item;
import models.NPC;
import models.Response;
import models.Weapon;
import models.GameInputHandler;
import models.ConversationTree;
import models.ConversationNode;


public class GameEngine
{
	
	private Player player;
	private boolean isRunning = false;
	private int currentRoomNum;
	private ArrayList<Room> rooms = new ArrayList<>();
	private String runningMessage = "";
	private String error = "";
	private GameInputHandler inputHandler;
	
	// Empty instantiation so data can be loaded using loadData()
    public GameEngine()
    {
        this.inputHandler = new GameInputHandler(this);
    }
    
    // called after creating the GameEngine instantiation in the session to load the current data and set isRunning to true
    public void start()
    {
        loadData();
        this.isRunning = true;
    }
    
    // "loads data" from .csv file in future but for now is where we create the instantiation of the game state for our demo
    public void loadData()
    {
        loadRooms();
        loadPlayer();
        this.currentRoomNum = 0;
        String roomName = getCurrentRoomName();
    }
    
    // loading of rooms
    public void loadRooms()
    {
        // Room One: No key required
        String roomName1 = "First Room";
        String[] components = {};
        Weapon weapon1 = new Weapon(20, 30, "Sword", components, 80, "<b>A rusty sword. Doesn't really fit in your bag.</b>", "<b>A sword.</b>");
        ArrayList<Item> itemContainer1 = new ArrayList<>();
        itemContainer1.add(weapon1);
        Utility potion1 = new Utility(10, 1, "Potion", null, "<b>A red potion that has \"POTION\" written across it. (+40 health)</b>", "<b>+40 Health potion.</b>",40,1);
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
        this.rooms.add(newRoom1);
        
        // Room Two: No key required
        String roomName2 = "Second Room";
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
        Item goldKey = new Item(0, 1, "gold key", new String[]{}, "<b>A super shiny key! Probably unlocks a door, but what do I know.</b>", "<b>A key!</b>");
        itemContainerBoss.add(goldKey);
        Inventory inventoryBoss = new Inventory(itemContainerBoss, 300);
        ArrayList<models.Character> characterContainer2 = new ArrayList<>();
        // Example NPC boss added to room two
        models.NPC boss = new NPC("Moe", 160, true, null, 80, inventoryBoss, "<b>Powerful man. Don't mess around!</b>", "<b>That's Moe!</b>");
        characterContainer2.add(boss);
        Room newRoom2 = new Room(roomName2, inventory2, connections2, characterContainer2, "<b>You are standing in front of the Slums- I mean Manors.</b>", "<b>You look out at the road out front of the Manors.</b>");
        this.rooms.add(newRoom2);
        
        // Room Three: Requires the "gold key" to enter
        String roomName3 = "Third Room";
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
        this.rooms.add(newRoom3);
        
        //Room Four: To Show off Shuttle
        String roomName4 = "Fourth Room";
        ArrayList<Item> itemContainer4 = new ArrayList<>();
        Inventory inventory4 = new Inventory(itemContainer4, 300);
        Connections connection4 = new Connections();
        connection4.setConnection("North", null);
        connection4.setConnection("East", null);
        connection4.setConnection("South", null);
        connection4.setConnection("West", null);
        connection4.setConnection("Shuttle", 0);
        
        ArrayList<models.Character> characterContainer4 = new ArrayList<>();
        Room newRoom4 = new Room(roomName4, inventory4, connection4, characterContainer4, "<b>Looking around West Campus, you see the Rutter's to the west and the road back to main on the east.</b>", "<b>You glance around West Campus.</b>");
        this.rooms.add(newRoom4);
    }
    
    
    // updates currentRoom to returned int if room is available, and returns true if updated; false otherwise
    public String updateCurrentRoom(String direction) {
        Room currentRoom = rooms.get(currentRoomNum);
        int newRoomNum = currentRoom.getConnectedRoom(direction);
        if (newRoomNum != -1) {
            Room destination = rooms.get(newRoomNum);
            String requiredKey = destination.getRequiredKey();
            
            
            // If a key is required, check if the player has it.
            if (requiredKey != null && !requiredKey.isEmpty()) {
                if (!player.hasKey(requiredKey)) {
                    return "\n<b>You do not have the required key (" + requiredKey + ") to enter " + destination.getRoomName() + ".</b>";
                }
            }
            
            // Allow the room change.
            this.currentRoomNum = newRoomNum;
            rooms.get(currentRoomNum).setRequiredKey(null);
            return "\n<b>You have entered " + destination.getRoomName() + "!</b>";
        }
        return "\n<b>There is no room in this direction.</b>";
    }
    

	
	
	// loading of player data
	public void loadPlayer()
	{
		String[] components = {};
		Weapon weaponPlayer = new Weapon(20, 30, "Dagger", components, 40, "<b>A trusty dagger that fits in your back pocket.</b>", "<b>A dagger.</b>");
		ArrayList<Item> itemContainer = new ArrayList<>();
		itemContainer.add(weaponPlayer);
		String playerName = "Cooper";
		Inventory inventory = new Inventory(itemContainer, 30);
		Player newPlayer = new Player(playerName, 200, 0, inventory, "<b>It's you! You know, you!</b>", "<b>You.</b>",1);
		this.player = newPlayer;
	}
	
	// updates currentRoom to returned int if room is available, and returns true (that the room was updated) and returns false if it isn't reachable, for the response to the user

	
	public int getMapOutput(String direction)
	{
		Room currentRoom = rooms.get(currentRoomNum);
		int output = currentRoom.getConnectedRoom(direction);
		return output;
	}
	
	// method for the player attacking a character - needs the characterNum to attack and item being used to attack
	public String playerAttackChar(int itemNum, int characterNum)
	{
		if (characterNum == -1 && itemNum == -1) {
			return "\n<b>Attack who with what?</b>";
		}
		else if (characterNum == -1) 
		{
			return "\n<b>Attack who?</b>";
		}
		else if (itemNum == -1) {
			return "\n<b>Attack with what?</b>";
		}
		Room currentRoom = rooms.get(currentRoomNum);
		double damageMulti = player.getdamageMulti();
		double attackDmg = player.getAttackDmg(itemNum)*damageMulti;//Needed
		int charHealth = currentRoom.getCharacterHealth(characterNum);
		double newHealth = charHealth - attackDmg;
		
		boolean aggressive = currentRoom.isCharAgressive(characterNum);
		
		
		if(newHealth <= 0)
		{
			String temp = currentRoom.getCharacterName(characterNum);
			
			currentRoom.handleCharacterDeath(characterNum);
			
			return "<b>" + currentRoom.getCharacterName(characterNum) + " has been slain and dropped its inventory!</b>";

		}
		else
		{
			currentRoom.setCharacterHealth(characterNum, newHealth);
			charAttackPlayer(0, characterNum, aggressive);

			// Format attack damage string
			String dmgString = (attackDmg % 1 == 0) ? String.format("%.0f", attackDmg) : String.format("%.1f", attackDmg);

			if (player.getHp() <= 0)
			{
				return "\n<b>You Died!</b>";
			}
			else if (aggressive)
			{
				int counterDmg = currentRoom.getCharacterAttackDmg(characterNum, 0);
				return "\n" + currentRoom.getCharacterName(characterNum) + " has taken " + dmgString + " damage." +
				       "\n" + currentRoom.getCharacterName(characterNum) + " hit back for " + counterDmg + " damage.";
			}
			else {
				return "\n" + "<b>" + currentRoom.getCharacterName(characterNum) + " has taken " + dmgString + " damage.</b>";
			}
		}
	}
	
	// method for the character characterNum attacking the player with itemNum (can be 0 for now and only give enemies 1 weapon for MS1 demo)
	public void charAttackPlayer(int itemNum, int characterNum, boolean aggressive)
	{
	    Room currentRoom = rooms.get(currentRoomNum);
	    Character character = currentRoom.getCharacter(characterNum);

	    // Don't attack if not aggressive
	    if (!aggressive) return;

	    // If character has no item at index, skip attack
	    if (character.getInventorySize() <= itemNum) {
	        runningMessage += "\n" + character.getName() + " has no weapon to attack with.";
	        return;
	    }

	    int attackDmg = currentRoom.getCharacterAttackDmg(characterNum, itemNum);
	    int playerHealth = player.getHp();

	    player.setHp(playerHealth - attackDmg);
	}


	
	// method for player to pickup item itemNum from room inventory
	public String pickupItem(int itemNum)
	{
		if (itemNum == -1) 
		{
			return "\nPick up what?";
			
		}
		Room currentRoom = rooms.get(currentRoomNum);
		String itemName = currentRoom.getItemName(itemNum); // temp name for the item name when we remove it
		Item item = currentRoom.getItem(itemNum);
		currentRoom.removeItem(itemNum);
		this.player.addItem(item);
		
		return "<b>\n" + itemName + " was picked up.</b>";
	}
	
	// helper method to convert from an items name to its ID
	public int RoomItemNameToID(String Name) 
	{
		int itemNum = -1; // set to -1 so if there is no item found it will return -1 to pick up / drop item, triggering "what item"
		Room currentRoom = rooms.get(currentRoomNum);
		for (int i = 0; i < currentRoom.getInventorySize(); i++) {
			if (Name.equalsIgnoreCase(currentRoom.getItemName(i))) {
				itemNum = i;
				break;
			}
		}
		return itemNum;
	}
	
	public int CharItemNameToID(String Name) 
	{
		int itemNum = -1; // set to -1 so if there is no item found it will return -1 to pick up / drop item, triggering "what item"
		
		for (int i = 0; i < player.getInventorySize(); i++) {
			if (Name.equalsIgnoreCase(player.getItemName(i))) {
				itemNum = i;
				break;
			}
		}
		return itemNum;
	}
	
	// helper method to convert from an char name to its ID
	public int CharNameToID(String Name) 
	{
		int charNum = -1; // set to -1 so if there is no char found it will return -1 to PlayerAttackChar, triggering "what char"
		Room currentRoom = rooms.get(currentRoomNum);
		for (int i = 0; i < currentRoom.getCharacterTotal(); i++) {
			if (Name.equalsIgnoreCase(currentRoom.getCharacterName(i))) {
				charNum = i;
			}
		}
		return charNum;
	}
	
	//method to get description of an item
	public String examineItemName(int itemNum) {
		if(itemNum == -1) 
		{
			return "\n<b>Examine what Item?</b>";
		}
		
		if(itemNum < 0 || itemNum >= player.getInventorySize())
		{
			return "\n<b>Invalid item selection.</b>";
		}
		
		Item InvenItem = player.getItem(itemNum);
		return "\n" + InvenItem.getDescription();
	}
	
	public String examineCharacter(int charNum) {
		if(charNum == -1) {
			return "\n<b>Examine what Character?</b>";
		}
		
		if(charNum < 0 || charNum >= rooms.get(currentRoomNum).getCharacterContainerSize()) {
			return "\n<b>Invalid Character selection.</b>";
		}
		
		Character character = rooms.get(currentRoomNum).getCharacter(charNum);
		return"\n<b>" + character.getCharDescription() + "</b>";
	}
	
	//InputProcess Method that gets the description of an items
	public String getExamine(String noun)
	{
		String message = "";
		int itemNum = CharItemNameToID(noun);
		int charNum = CharNameToID(noun);
		if(noun.toLowerCase().equals("room")) {
			message = "\n"+rooms.get(currentRoomNum).getRoomDescription();
		}
		if(charNum >= 0) {
			message = examineCharacter(charNum);
		}
		if(itemNum >= 0) {
			message = examineItemName(itemNum);
		}
		return message;
	}
	
	// method to drop an item from player inventory into room inventory
	public String dropItem(int itemNum)
	{
		if (itemNum == -1) 
		{
			return "\n<b>Drop what?</b>";
		}
		
		if (itemNum < 0 || itemNum >= player.getInventorySize()) {
	        return "\n<b>Invalid item selection.</b>";
	    }
		
		Room currentRoom = rooms.get(currentRoomNum);
		Item InvenItem = player.getItem(itemNum);
		player.removeItem(itemNum);
	    currentRoom.addItem(InvenItem);
		
		return "\n<b>" + InvenItem.getName() + " was dropped.</b>";
	}
	
	// method to get character name for display()
	public String getCharacterName(int characterNum)
	{
		Room currentRoom = rooms.get(currentRoomNum);
		String name = currentRoom.getCharacterName(characterNum);
		return name;
		
	}
	
	public int getCurrentRoomNum()
	{
		return this.currentRoomNum;
	}
	
	public String getCurrentRoomName()
	{
		Room currentRoom = rooms.get(currentRoomNum);
		return currentRoom.getRoomName();
	}
	
	public String getRoomName(int roomNum)
	{
		Room currentRoom = rooms.get(roomNum);
		return currentRoom.getRoomName();
	}
	
	public String getCurrentRoomItems()
	{
		String currentRoomItems = "Room Inventory:\n";
		Room currentRoom = rooms.get(currentRoomNum);
		int size = currentRoom.getInventorySize();
		for (int i = 0; i < size; i ++)
		{
			currentRoomItems += i+1 + "\t" + currentRoom.getItemName(i) + "\n";
		}
		
		return currentRoomItems;
	}
	
	public String getPlayerInventoryString()
	{
		String playerInventoryString = "Player Inventory:\n";
		int size = this.player.getInventorySize();
		for (int i = 0; i < size; i ++)
		{
			playerInventoryString += i+1 + "\t" + player.getItemName(i) + "\n";
		}
		
		return playerInventoryString;
	}
	
	public String getPlayerInfo()
	{
		String playerInfo = "Player Info:\n";
		playerInfo += "Name: " + player.getName() + "\nHealth: " + player.getHp() + "\n";
		return playerInfo;
	}
	
	public String getRoomCharactersInfo()
	{
		String charactersInfo = "Characters in Room:\n";
		Room currentRoom = rooms.get(currentRoomNum);
		int size = currentRoom.getCharacterContainerSize();
		for (int i = 0; i < size; i ++)
		{
			charactersInfo += i + 1 + "\t" + currentRoom.getCharacterName(i) + "\nHealth: " + currentRoom.getCharacterHealth(i) + "\n";
		}
		return charactersInfo;
	}
	
	public String getRoomConnectionOutput()
	{
		String roomConnectionOutput = "Rooms available for each movement:\n";
		int outputNorth = getMapOutput("North");
		int outputEast = getMapOutput("East");
		int outputSouth = getMapOutput("South");
		int outputWest = getMapOutput("West");
		int outputShuttle = getMapOutput("Shuttle");
		
		if (outputNorth == -1)
		{
			roomConnectionOutput += "North : None\n";
		}
		else
		{
			int sum = outputNorth;
			roomConnectionOutput += "North : " + getRoomName(sum) + "\n";
		}
		
		if (outputEast == -1)
		{
			roomConnectionOutput += "East : None\n";
		}
		else
		{
			int sum = outputEast;
			roomConnectionOutput += "East : " + getRoomName(sum) + "\n";
		}
		if (outputSouth == -1)
		{
			roomConnectionOutput += "South : None\n";
		}
		else
		{
			int sum = outputSouth ;
			roomConnectionOutput += "South : " + getRoomName(sum) + "\n";
		}
		
		if (outputWest == -1)
		{
			roomConnectionOutput += "West : None\n" ;
		}
		else
		{
			int sum = outputWest ;
			roomConnectionOutput += "West : " + getRoomName(sum) + "\n";
		}
		
		if(outputShuttle == -1) 
		{
			roomConnectionOutput += "Shuttle : None\n";
		}
		else
		{
			int sum = outputShuttle;
			roomConnectionOutput += "Shuttle : " +getRoomName(sum) + "\n";
		}
		
		return roomConnectionOutput;
		
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
	    runningMessage += newMessage;
	    
	    // Return only the new message (so you don't repeat the entire history).
	    return newMessage;
	}
	public String usePotion(int itemNum) {
		if (itemNum == -1) 
		{
			return "\n<b>Use what?</b>";
		}
		
		if (itemNum < 0 || itemNum >= player.getInventorySize()) {
	        return "\n<b>Invalid item selection.</b>";
	    }
		
		Item InvenItem = player.getItem(itemNum);
		
		double Multi = ((Utility) InvenItem).getDamageMulti();
		int Healing = ((Utility) InvenItem).getHealing();
		int newHp = player.getHp() + Healing;
		
		player.setdamageMulti(Multi);
		player.setHp(newHp);
		player.removeItem(itemNum);
		
		return "\n<b>" + InvenItem.getName() + " was used.</b>";
		
	}
		
	
	public String getOnShuttle() {
		String newMessage = "";
		
		if(player.hasKey("Shuttle Pass")) {
			String direction = "Shuttle";
			newMessage = updateCurrentRoom(direction);
			runningMessage += newMessage;
		}
		
		else {
			newMessage = "<b>\nYou do not have the Shuttle Pass.</b>";
			runningMessage += newMessage;
		}
		
		return newMessage;
	}
	
	public String getHelp() 
	{
		String message ="<b>\nTake, get, grab, pickup are for picking up an item. (ex. (get|grab|pickup) sword)\n";
		message += "\nDrop is to drop an item (ex. drop dagger)\n";
		message += "\nAttack, swing, slash, hit, strike are for attacking an enemy. (ex. (attack|swing|slash|hit|strike) moe with trident)\n";
		message += "\nGo, move, walk are for moving in a direction. (ex. (walk|move) north)\n";
		message += "\nExamine and look are for looking at the description of an item or room. (ex. (examine|look) dagger)\n";
		message += "\nShuttle is the same as movement but for traveling via shuttle. (ex. shuttle | drive)\n";
		message += "\nTalk is how to interact with valid NPCs. (ex. (talk) curly. Continue conversation with Respond #</b>";
		return message;
	}

	
	// method called in servlet to get input and call methods based on it, need to figure out optimal CLI parsing technique
	// if the input is valid and processed return true, and if it is an invalid input return false, so the servlet knows when to send error message
    public boolean processInput(String input) {
        return inputHandler.processInput(input);
    }
    
    public void appendMessage(String message) { 
        this.runningMessage += message;
    }
    
    public String talkToNPC(int characterNum)
    {
    	Room currentRoom = rooms.get(currentRoomNum);
    	return currentRoom.talkToNPC(characterNum);
    }
    
    public String[] getResponseOptions(int characterNum)
    {
    	Room currentRoom = rooms.get(currentRoomNum);
    	return currentRoom.getNPCResponseOptions(characterNum);
    }
    
    public String interactWithNPC(String choice, int characterNum)
    {
    	Room currentRoom = rooms.get(currentRoomNum);
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

    
    public boolean reachedFinalNode()
    {
    	return true;
    }
	
    public String getCurrentRoomImage() {
	    String roomName = getCurrentRoomName();
	    switch(roomName) {
	        case "First Room":
	            return "images/firstRoom.jpg";
	        case "Second Room":
	            return "images/firstRoom.jpg";
	        case "Third Room":
	            return "images/firstRoom.jpg";
	        case "Fourth Room":
	        	return "images/firstRoom.jpg";
	        default:
	            return "images/default.jpg";
	    }
	    
	 
	    
	}
    
    private Map<Integer, List<double[]>> roomItemPositions = new HashMap<>();
    
    public String getRoomItemsOverlay() {
        Room currentRoom = rooms.get(currentRoomNum);
        StringBuilder sb = new StringBuilder();
        int roomIndex = currentRoomNum;
        
        // Get stored positions for the current room, if available.
        List<double[]> positions = roomItemPositions.get(roomIndex);
        Random rand = new Random();
        if (positions == null) {
            positions = new ArrayList<>();
        }
        // If stored positions are fewer than items in the room, generate additional ones.
        while (positions.size() < currentRoom.getInventorySize()) {
            double leftPercent = 40 +rand.nextDouble() * 40;         // anywhere from 0% to 90%
            double topPercent = 65 + rand.nextDouble() * 15;       // between 65% and 80%
            positions.add(new double[]{leftPercent, topPercent});
        }
        // Update the map with the (possibly extended) list.
        roomItemPositions.put(roomIndex, positions);
        
        // Build the overlay using the stored positions.
        for (int i = 0; i < currentRoom.getInventorySize(); i++) {
            String itemName = currentRoom.getItemName(i);
            double[] pos = positions.get(i);
            sb.append("<img src='images/").append(itemName).append(".png' alt='").append(itemName)
              .append("' style='position:absolute; left:").append(pos[0])
              .append("%; top:").append(pos[1])
              .append("%; width:1in; height:auto; background-color: transparent;'/>");
        }
        return sb.toString();
    }

    public String getRoomCharactersOverlay() {
        Room currentRoom = rooms.get(currentRoomNum);
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
            sb.append("<img src='images/").append(charName).append(".png' alt='").append(charName)
              .append("' style='width:2.5in; height:auto; background-color: transparent;'/>");
            
            sb.append("</div>");
        }
        return sb.toString();
    }
    
    public String getCurrentRoomNumber() {
        // Adding 1 so that room 0 becomes "1", room 1 becomes "2", etc.
        return String.valueOf(currentRoomNum + 1);
    }
	// returns a Response object to be sent over the get and post request so that the page displays the current game state at all times and updates based on post request input
    public Response display()
    {
        Response response = new Response(
            getCurrentRoomItems(), 
            getPlayerInventoryString(), 
            getRoomCharactersInfo(), 
            getPlayerInfo(), 
            getRoomConnectionOutput(),
            runningMessage, 
            "Test error",
            getCurrentRoomImage(),
            getCurrentRoomNumber(),
            getRoomItemsOverlay(),         // New field: items overlay
            getRoomCharactersOverlay()     // New field: characters overlay
        );
        return response;
    }
	
	
	
}