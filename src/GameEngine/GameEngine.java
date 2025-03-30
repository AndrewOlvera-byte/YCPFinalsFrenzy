package GameEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.*;
import models.Character;
import orm.OrmManager;

public class GameEngine {
    private Player player;
    private boolean isRunning = false;
    private int currentRoomNum = 0;
    private ArrayList<Room> rooms = new ArrayList<>();
    private String runningMessage = "";
    private GameInputHandler inputHandler;

    public GameEngine() {
        this.inputHandler = new GameInputHandler(this);
    }

    public void start(OrmManager orm) {
        try {
            this.player = orm.find(Player.class, 1);
            
            if (this.player == null) {
                throw new RuntimeException("🚨 No player found in database! Seed your data first.");
            }
            
            this.player.getInventory().loadItemsFromDB(orm);

            List<Room> dbRooms = orm.findAll(Room.class);
            if (dbRooms.isEmpty()) {
                System.err.println("No rooms found in the database!");
            }
            List<RoomCharacterLink> links = orm.findAll(RoomCharacterLink.class);
            List<Character> allCharacters = orm.findAll(Character.class);

            this.rooms.clear();
            this.rooms.addAll(dbRooms);

            for (Room room : dbRooms) {
                room.getInventory().loadItemsFromDB(orm);

                ArrayList<Character> chars = new ArrayList<>();
                for (RoomCharacterLink link : links) {
                    if (link.getRoomId() == room.getId()) {
                        for (Character c : allCharacters) {
                            if (c.getId() == link.getCharacterId()) {
                                c.getInventory().loadItemsFromDB(orm);
                                chars.add(c);
                                break;
                            }
                        }
                    }
                }

                room.setCharacterContainer(chars);
            }

            this.currentRoomNum = 0;
            this.isRunning = true;
            System.out.println("Game loaded from database.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveGameState(OrmManager orm) {
        try {
            int itemLinkId = 1;
            int charLinkId = 1;

            orm.save(player);
            orm.save(player.getInventory());
            for (Item item : player.getInventory().getInventory()) {
                orm.save(item);
                orm.save(new InventoryItemLink(itemLinkId++, player.getInventory().getId(), item.getId()));
            }

            for (Room room : rooms) {
                orm.save(room);
                Inventory roomInv = room.getInventory();
                orm.save(roomInv);
                for (Item item : roomInv.getInventory()) {
                    orm.save(item);
                    orm.save(new InventoryItemLink(itemLinkId++, roomInv.getId(), item.getId()));
                }

                orm.save(room.getConnections());

                for (Character c : room.getCharacterContainer()) {
                    orm.save(c);
                    Inventory charInv = c.getInventory();
                    orm.save(charInv);
                    for (Item item : charInv.getInventory()) {
                        orm.save(item);
                        orm.save(new InventoryItemLink(itemLinkId++, charInv.getId(), item.getId()));
                    }
                    orm.save(new RoomCharacterLink(charLinkId++, room.getId(), c.getId()));
                }
            }

            System.out.println("Game state saved to DB.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean processInput(String input) {
        return inputHandler.processInput(input);
    }

    public void appendMessage(String message) {
        this.runningMessage += message;
    }
    
    public boolean isGameLoaded() {
        return isRunning && player != null && !rooms.isEmpty();
    }

    public Player getPlayer() {
        return player;
    }

    public Room getCurrentRoom() {
    	if (rooms.isEmpty()) {
            throw new IllegalStateException("No rooms available.");
        }
        return rooms.get(currentRoomNum);
    }
    
    public List<Room> getRooms() {
        return rooms;
    }

    public String getGo(String noun) {
        Room currentRoom = rooms.get(currentRoomNum);
        Integer nextRoomId = currentRoom.getConnections().getConnection(noun.toLowerCase());
        if (nextRoomId == null) {
            return "\nThere is no room in that direction.";
        }
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getId() == nextRoomId) {
                currentRoomNum = i;
                return "\nYou move to " + rooms.get(i).getRoomName() + ".";
            }
        }
        return "\nThat room ID doesn't exist.";
    }

    public int RoomItemNameToID(String name) {
        Room currentRoom = getCurrentRoom();
        for (int i = 0; i < currentRoom.getInventorySize(); i++) {
            if (name.equalsIgnoreCase(currentRoom.getItemName(i))) {
                return i;
            }
        }
        return -1;
    }

    public int CharItemNameToID(String name) {
        for (int i = 0; i < player.getInventorySize(); i++) {
            if (name.equalsIgnoreCase(player.getItemName(i))) {
                return i;
            }
        }
        return -1;
    }

    public int CharNameToID(String name) {
        Room currentRoom = getCurrentRoom();
        for (int i = 0; i < currentRoom.getCharacterTotal(); i++) {
            if (name.equalsIgnoreCase(currentRoom.getCharacterName(i))) {
                return i;
            }
        }
        return -1;
    }

    public String getExamine(String noun) {
        int itemNum = CharItemNameToID(noun);
        int charNum = CharNameToID(noun);
        if (noun.equalsIgnoreCase("room")) {
            return "\n" + getCurrentRoom().getRoomDescription();
        }
        if (charNum >= 0) {
            return "\n" + getCurrentRoom().getCharacter(charNum).getCharDescription();
        }
        if (itemNum >= 0) {
            return "\n" + player.getItem(itemNum).getDescription();
        }
        return "\nNothing to examine.";
    }

    public String getHelp(String verb) {
        return "\nAvailable commands:\n" +
            "- pickup/take/get/grab [item]\n" +
            "- drop [item]\n" +
            "- examine/look [item|character|room]\n" +
            "- go/move/walk [north|south|east|west]\n" +
            "- attack/swing/hit/strike [character] with [item]";
    }

    public Response display() {
    	
    	if (!isGameLoaded()) {
            return new Response(
                "", "", "", "No player info",
                "No room connections", "Game not loaded", "GameEngine not initialized."
            );
        }
    	
        Room currentRoom = getCurrentRoom();
        return new Response(
            currentRoom.getInventory().listItems(),
            player.getInventory().listItems(),
            getRoomCharactersInfo(),
            getPlayerInfo(),
            getRoomConnectionOutput(),
            runningMessage,
            ""
        );
    }

    private String getRoomCharactersInfo() {
        Room currentRoom = rooms.get(currentRoomNum);
        StringBuilder info = new StringBuilder("Characters in Room:\n");
        for (Character character : currentRoom.getCharacterContainer()) {
            info.append(character.getName()).append("\nHealth: ").append(character.getHp()).append("\n");
        }
        return info.toString();
    }

    private String getPlayerInfo() {
        return "Name: " + player.getName() + "\nHealth: " + player.getHp();
    }
    
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
		
		return "\n" + itemName + " was picked up.";
	}
    
    public String dropItem(int itemNum)
	{
		if (itemNum == -1) 
		{
			return "\nDrop what?";
		}
		
		if (itemNum < 0 || itemNum >= player.getInventorySize()) {
	        return "\nInvalid item selection.";
	    }
		
		Room currentRoom = rooms.get(currentRoomNum);
		Item InvenItem = player.getItem(itemNum);
		player.removeItem(itemNum);
	    currentRoom.addItem(InvenItem);
		
		return "\n" + InvenItem.getName() + " was dropped.";
	}
    
    public String playerAttackChar(int itemNum, int characterNum)
	{
		if (characterNum == -1 && itemNum == -1) {
			return "\nAttack who with what?";
		}
		else if (characterNum == -1) 
		{
			return "\nAttack who?";
		}
		else if (itemNum == -1) {
			return "\nAttack with what?";
		}
		Room currentRoom = rooms.get(currentRoomNum);
		int attackDmg = player.getAttackDmg(itemNum);//Needed
		int charHealth = currentRoom.getCharacterHealth(characterNum);
		int newHealth = charHealth - attackDmg;
		
		boolean aggressive = currentRoom.isCharAgressive(characterNum);
		
		
		if(newHealth <= 0)
		{
			String temp = currentRoom.getCharacterName(characterNum);
			
			currentRoom.handleCharacterDeath(characterNum);
			
			return "\n" + temp + " has been slain and dropped its inventory!";
		}
		else
		{
			currentRoom.setCharacterHealth(characterNum, newHealth);
			charAttackPlayer(0, characterNum, aggressive);
			if(player.getHp() <= 0)
			{
				return "\nYou Died!";
			}
			else if (aggressive == true)
			{
				return "\n" + currentRoom.getCharacterName(characterNum) + " has taken " + attackDmg + " damage." + "\n"+currentRoom.getCharacterName(characterNum)+" Hit back for "+ currentRoom.getCharacterAttackDmg(characterNum, 0);
				
		}
			else {
				return "\n" + currentRoom.getCharacterName(characterNum) + " has taken " + attackDmg + " damage.";
			}
		}
	}
    
 // method for the character characterNum attacking the player with itemNum (can be 0 for now and only give enemies 1 weapon for MS1 demo)
 	public void charAttackPlayer(int itemNum, int characterNum, boolean aggressive)
 	{
 		Room currentRoom = rooms.get(currentRoomNum);
 		int attackDmg = currentRoom.getCharacterAttackDmg(characterNum, itemNum);
 		int playerHealth = player.getHp();
 		int newHealth = playerHealth - attackDmg;

 		if (aggressive == true) {
 			player.setHp(playerHealth - attackDmg);
 		}
 	}

    private String getRoomConnectionOutput() {
        Room currentRoom = rooms.get(currentRoomNum);
        Connections conn = currentRoom.getConnections();
        return "North: " + nameFor(conn.getConnection("north")) +
               "\nEast: " + nameFor(conn.getConnection("east")) +
               "\nSouth: " + nameFor(conn.getConnection("south")) +
               "\nWest: " + nameFor(conn.getConnection("west"));
    }

    private String nameFor(Integer roomId) {
        if (roomId == null) return "None";
        for (Room r : rooms) {
            if (r.getId() == roomId) return r.getRoomName();
        }
        return "Unknown";
    }
}
