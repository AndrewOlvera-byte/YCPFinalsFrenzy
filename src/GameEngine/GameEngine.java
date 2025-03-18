package GameEngine;

import java.util.ArrayList;

import models.Room;
import models.Player;
import models.Connections;
import models.Inventory;
import models.Item;
import models.Response;


public class GameEngine
{
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
	}
	
	// loading of rooms
	public void loadRooms()
	{
		// example implementation but will be looped over data in the .csv file to load the rooms state last left off
		String roomName1 = "First Room";
		ArrayList<Item> itemContainer1 = new ArrayList<>();
		Inventory inventory1 = new Inventory(itemContainer1, 300);
		Connections connections1 = new Connections();
		connections1.setConnection("North", 1);
		connections1.setConnection("East", null);
		ArrayList<models.Character> characterContainer = new ArrayList<>();
		Room newRoom1 = new Room(roomName1, inventory1, connections1, characterContainer);
		this.rooms.add(newRoom1);
	}
	
	// loading of player data
	public void loadPlayer()
	{
		ArrayList<Item> itemContainer = new ArrayList<>();
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
			return true;
		}
		return false;
	}
	
	// method for the player attacking a character - needs the characterNum to attack and item being used to attack
	public void playerAttackChar(int itemNum, int characterNum)
	{
		Room currentRoom = rooms.get(currentRoomNum);
		int attackDmg = player.getAttackDmg(itemNum);//Needed
		int charHealth = currentRoom.getCharacterHealth(characterNum);
		int newHealth = charHealth - attackDmg;
		if(newHealth <= 0)
		{
			currentRoom.removeCharacter(characterNum);
		}
		else
		{
			currentRoom.setCharacterHealth(characterNum, newHealth);
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
	public void pickupItem(int itemNum)
	{
		Room currentRoom = rooms.get(currentRoomNum);
		Item item = currentRoom.getItem(itemNum);
		currentRoom.removeItem(itemNum);
		this.player.addItem(item);
	}
	
	// method to drop an item from player inventory into room inventory
	public void dropItem(int itemNum)
	{
		Room currentRoom = rooms.get(currentRoomNum);
		Item item = player.getItem(itemNum);
		currentRoom.addItem(item);
		player.removeItem(itemNum);
	}
	
	// method to get character name for display()
	public String getCharacterName(int characterNum)
	{
		Room currentRoom = rooms.get(currentRoomNum);
		String name = currentRoom.getCharacterName(characterNum);
		return name;
		
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
		
		// We need to decide whether every agressive character attacks every turn, or every other, or something else
		// TODO: iterate over agressivw characters and do charAttackPlayer(0,i)
		//-----------------
		
		//String parsing logic and calling methods inside
		return true;
	}
	
	// returns a Response object to be sent over the get and post request so that the page displays the current game state at all times and updates based on post request input
	public Response display()
	{
		Response response = new Response("Test Room Inventory", "Test Player Inventory", "Test room connections", "Test message", "Test error");// put fields inside here which will be called with ${response.attribute} in jsp and html
		return response;
	}
	
}