package GameEngine;

import java.util.ArrayList;
import java.util.*;

import models.Room;
import models.Player;
import models.Character;
import models.Connections;
import models.Inventory;
import models.Item;
import models.Response;
import models.Weapon;


public class GameEngine
{
	private static final Set<String> VALID_VERBS = new HashSet<>(Arrays.asList(
	        "pickup", "drop", "use", "examine", "go", "talk"
	));
	
	private static final Set<String> PREPOSITIONS = new HashSet<>(Arrays.asList(
	        "on", "with", "at", "in", "to"
	    ));
	
	private Player player;
	private boolean isRunning = false;
	private int currentRoomNum;
	private ArrayList<Room> rooms = new ArrayList<>();
	private String runningMessage = "";
	private String error = "";
	
	// Empty instantiation so data can be loaded using loadData()
	public GameEngine()
	{}
	
	// called after creating the GameEngine instantiation in the session to load the current data and set isRunning to true
	public void start()
	{
		loadData();
		this.isRunning = true;
	}
	
	// "loads data" from .csv file in future but for now is where we create the instantiation of the game state for out MS1 demo
	public void loadData()
	{
		loadRooms();
		loadPlayer();
		this.currentRoomNum = 0;
		String roomName = getCurrentRoomName();
		this.runningMessage += "You have entered " + roomName + "!\t";
	}
	
	// loading of rooms
	public void loadRooms()
	{
		// example implementation but will be looped over data in the .csv file to load the rooms state last left off
		String roomName1 = "First Room";
		String[] components = {};
		Weapon weapon1 = new Weapon(20, 30, "Sword", components, 40);
		ArrayList<Item> itemContainer1 = new ArrayList<>();
		itemContainer1.add(weapon1);
		Inventory inventory1 = new Inventory(itemContainer1, 300);
		Connections connections1 = new Connections();
		connections1.setConnection("North", 1);
		connections1.setConnection("East", null);
		connections1.setConnection("South", null);
		connections1.setConnection("West", null);
		ArrayList<models.Character> characterContainer1 = new ArrayList<>();
		Room newRoom1 = new Room(roomName1, inventory1, connections1, characterContainer1);
		this.rooms.add(newRoom1);
		
		String roomName2 = "Second Room";
		ArrayList<Item> itemContainer2 = new ArrayList<>();
		Inventory inventory2 = new Inventory(itemContainer2, 300);
		
		Connections connections2 = new Connections();
		connections2.setConnection("North", null);
		connections2.setConnection("East", null);
		connections2.setConnection("South", 0);
		connections2.setConnection("West", null);
		
		
		ArrayList<Item> itemContainerBoss = new ArrayList<>();
		String[] componentsBoss = {};
		Weapon weaponBoss = new Weapon(20, 30, "Boss Sword", componentsBoss, 90);
		itemContainerBoss.add(weaponBoss);
		Inventory inventoryBoss = new Inventory(itemContainerBoss, 300);
		
		ArrayList<models.Character> characterContainer2 = new ArrayList<>();
		Character boss = new Character("Big Boss", 400, inventoryBoss);
		characterContainer2.add(boss);
		Room newRoom2 = new Room(roomName2, inventory2, connections2, characterContainer2);
		this.rooms.add(newRoom2);
	}
	
	// loading of player data
	public void loadPlayer()
	{
		String[] components = {};
		Weapon weaponPlayer = new Weapon(20, 30, "Badass Sword", components, 80);
		ArrayList<Item> itemContainer = new ArrayList<>();
		itemContainer.add(weaponPlayer);
		String playerName = "Bob";
		Inventory inventory = new Inventory(itemContainer, 30);
		Player newPlayer = new Player(playerName, 200, 0, inventory);
		this.player = newPlayer;
	}
	
	// updates currentRoom to returned int if room is available, and returns true (that the room was updated) and returns false if it isn't reachable, for the response to the user
	public Boolean updateCurrentRoom(String direction)
	{
		Room currentRoom = rooms.get(currentRoomNum);
		int newRoomNum = currentRoom.getConnectedRoom(direction);
		if(newRoomNum != -1)
		{
			this.currentRoomNum = newRoomNum;
			String roomName = getCurrentRoomName();
			this.runningMessage += "You have entered " + roomName + "!\t";
			return true;
		}
		return false;
	}
	
	public int getMapOutput(String direction)
	{
		Room currentRoom = rooms.get(currentRoomNum);
		int output = currentRoom.getConnectedRoom(direction);
		return output;
	}
	
	// method for the player attacking a character - needs the characterNum to attack and item being used to attack
	public String playerAttackChar(int itemNum, int characterNum)
	{
		if (characterNum == -1) 
		{
			return "Attack who?";
		}
		Room currentRoom = rooms.get(currentRoomNum);
		int attackDmg = player.getAttackDmg(itemNum);//Needed
		int charHealth = currentRoom.getCharacterHealth(characterNum);
		int newHealth = charHealth - attackDmg;
		if(newHealth <= 0)
		{
			currentRoom.removeCharacter(characterNum);
			return currentRoom.getCharacterName(characterNum) + " has died!";
		}
		else
		{
			currentRoom.setCharacterHealth(characterNum, newHealth);
			return currentRoom.getCharacterName(characterNum) + " has taken " + attackDmg + " damage.";
		}
	}
	
	// method for the character characterNum attacking the player with itemNum (can be 0 for now and only give enemies 1 weapon for MS1 demo)
	public void charAttackPlayer(int itemNum, int characterNum)
	{
		Room currentRoom = rooms.get(currentRoomNum);
		int attackDmg = currentRoom.getCharacterAttackDmg(characterNum, itemNum);
		int playerHealth = player.getHp();
		int newHealth = playerHealth - attackDmg;
		if(newHealth <= 0)
		{
			this.error = "You Died!";
		}
		else
		{
			player.setHp(playerHealth - attackDmg);
		}	
	}
	
	// method for player to pickup item itemNum from room inventory
	public String pickupItem(int itemNum)
	{
		if (itemNum == -1) 
		{
			return "Pick up what?";
		}
		Room currentRoom = rooms.get(currentRoomNum);
		Item item = currentRoom.getItem(itemNum);
		currentRoom.removeItem(itemNum);
		this.player.addItem(item);
		
		return currentRoom.getItemName(itemNum) + " was picked up.";
	}
	
	// helper method to convert from an items name to its ID
	public int ItemNameToID(String Name) 
	{
		int itemNum = -1; // set to -1 so if there is no item found it will return -1 to pick up / drop item, triggering "what item"
		Room currentRoom = rooms.get(currentRoomNum);
		for (int i = 0; i < currentRoom.getInventorySize(); i++) {
			if (Name == currentRoom.getItemName(i)) {
				itemNum = i;
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
			if (Name == currentRoom.getCharacterName(i)) {
				charNum = i;
			}
		}
		return charNum;
	}
	
	// method to drop an item from player inventory into room inventory
	public String dropItem(int itemNum)
	{
		if (itemNum == -1) 
		{
			return "Drop what?";
		}
		Room currentRoom = rooms.get(currentRoomNum);
		Item item = player.getItem(itemNum);
		currentRoom.addItem(item);
		player.removeItem(itemNum);
		
		return currentRoom.getItemName(itemNum) + " was dropped.";
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
		
		if (outputNorth == -1)
		{
			roomConnectionOutput += "North : None" + " -- ";
		}
		else
		{
			int sum = outputNorth;
			roomConnectionOutput += "North : Room " + sum + " -- ";
		}
		
		if (outputEast == -1)
		{
			roomConnectionOutput += "East : None" + " -- ";
		}
		else
		{
			int sum = outputEast;
			roomConnectionOutput += "East : Room " + sum + " -- ";
		}
		if (outputSouth == -1)
		{
			roomConnectionOutput += "South : None" + " -- ";
		}
		else
		{
			int sum = outputSouth;
			roomConnectionOutput += "South : Room " + sum + " -- ";
		}
		
		if (outputWest == -1)
		{
			roomConnectionOutput += "West : None" ;
		}
		else
		{
			int sum = outputWest;
			roomConnectionOutput += "West : Room " + sum ;
		}
		
		return roomConnectionOutput;
		
	}
	
	public static String[] parseInput(String command) {
        String[] words = command.split("\\s+");
        if (words.length == 0) return new String[]{null, null};

        String verb = words[0];
        String noun = (words.length > 1) ? words[1] : null;

        return new String[]{verb, noun};
    }
	
	// method called in servlet to get input and call methods based on it, need to figure out optimal CLI parsing technique
	// if the input is valid and processed return true, and if it is an invalid input return false, so the servlet knows when to send error message
	public boolean processInput(String input)
	{
		
		//-----------------
		//Do at beginning of every turn
		// Check if any character's have aggression in the room
		ArrayList<Integer> aggressiveCharacters = new ArrayList<>();
		Room currentRoom = rooms.get(currentRoomNum);
		int size = currentRoom.getCharacterTotal();
		for (int i = 0; i < size; i++)
		{
			if (currentRoom.isCharAgressive(i))
			{
				aggressiveCharacters.add(i);
			}
		}
		int aggCharSize = aggressiveCharacters.size();
		if (aggCharSize > 0)
		{
			this.runningMessage+="\nThere are aggressive enemies in this room!";
		}
		for (int i = 0; i < aggCharSize; i ++)
		{
			int characterNum = aggressiveCharacters.get(i);
			if (currentRoom.getCharacterJustAttacked(characterNum))
			{
				currentRoom.setCharacterJustAttacked(characterNum, false);
			}
			else
			{
				charAttackPlayer(0, characterNum);
				this.runningMessage+="\nYou have been Attacked!";
			}
		}
		
		//updateCurrentRoom("North");
		
		
		
		
		// String parsing logic and calling methods inside
		String[] parsedInput = parseInput(input);
        String verb = parsedInput[0];
        String noun = parsedInput[1];
        String preposition = parsedInput[2];
        String noun2 = parsedInput[3];

        if (verb == null || verb.isEmpty()) {
            this.runningMessage += "I don't understand that.";
        }

        if (!VALID_VERBS.contains(verb)) {
            this.runningMessage += "Unknown command: " + verb;
        }

        switch (verb) {
            case "pickup":
                pickupItem(ItemNameToID(noun));
            case "drop":
            	dropItem(ItemNameToID(noun));
            case "swing": // for hand-held weapons that can be swung, this is like an "or" 
            case "slash":
            case "hit":
            case "strike":
            	playerAttackChar(CharNameToID(noun), ItemNameToID(noun2));
            //case "throw": throwable weapons?	
                
            case "go":
                
            case "examine":
                
            case "talk":
                
            case "use":
                
            default:
                this.runningMessage += "Command not implemented.";
        }
    
		return true;
	}
	
	// returns a Response object to be sent over the get and post request so that the page displays the current game state at all times and updates based on post request input
	public Response display()
	{
		Response response = new Response(getCurrentRoomItems(), getPlayerInventoryString(), getRoomCharactersInfo(), getPlayerInfo(), getRoomConnectionOutput(), runningMessage, "Test error");// put fields inside here which will be called with ${response.attribute} in jsp and html
		return response;
	}
	
}