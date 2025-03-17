package GameEngine;

import java.util.ArrayList;
import java.util.JSONObject;
import models.Room;
import models.Connections;
import models.Inventory;
import models.Character;
import models.Response;
import models.Player;

public class GameEngine
{
	private Player player;
	private boolean isRunning = false;
	private int currentRoomNum;
	private ArrayList<Room> rooms = new ArrayList<>();
	private String returnMessage = "";
	
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
		Inventory inventory = new Inventory();
		Connections connections = new Connections();
		ArrayList<Character> characterContainer = new ArrayList<>();
		Room newRoom = new Room(inventory, connections, characterContainer);
		this.rooms.add(newRoom);
	}
	
	// loading of player data
	public void loadPlayer()
	{
		Inventory inventory = new Inventory();
		Player newPlayer = new Player(inventory);
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
		currentRoom.setCharacterHealth(characterNum, charHealth - attackDmg);
	}
	
	// method for the character characterNum attacking the player with itemNum (can be 0 for now and only give enemies 1 weapon for MS1 demo)
	public void charAttackPlayer(int itemNum, int characterNum)
	{
		Room currentRoom = rooms.get(currentRoomNum);
		int attackDmg = currentRoom.getCharacterAttackDmg(characterNum, itemNum);
		int playerHealth = player.getHealth();
		player.setHealth(playerHealth - attackDmg);
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
	
	// method called in servlet to get input and call methods based on it, need to figure out optimal CLI parsing technique
	// if the input is valid and processed return true, and if it is an invalid input return false, so the servlet knows when to send error message
	public boolean processInput(String input)
	{
		//String parsing logic and calling methods inside
	}
	
	// returns a Response object to be sent over the get and post request so that the page displays the current game state at all times and updates based on post request input
	public Response display()
	{
		Response response = new Response();// put fields inside here which will be called with ${response.attribute} in jsp and html
		return response;
	}
	
}