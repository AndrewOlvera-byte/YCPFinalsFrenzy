package GameEngine;

import java.util.*;
import models.*;
import models.Character;  // your NPC base
import java.sql.SQLException;
import models.CraftingManager;

public class GameEngine {
	
    private static final boolean USE_FAKE_DB = false;
    
    private String[] tables = {"conversation_edges", "conversation_nodes", "GAME_STATE", "PLAYER_INVENTORY", "NPC_INVENTORY",
    		"ROOM_INVENTORY", "NPC_ROOM", "ROOM_CONNECTIONS", "ITEM_COMPONENT", "COMPANION_ROOM", "PLAYER_COMPANION", "COMPANION_INVENTORY", "COMPANION", "NPC", "ROOM", "ITEM", "PLAYER"};

    private Player player;
    private boolean isRunning = false;
    private int currentRoomNum;
    private ArrayList<Room> rooms = new ArrayList<>();
    private String runningMessage = "";
    private String error = "";
    private GameInputHandler inputHandler;
    
    // Managers
    private RoomManager roomManager;
    private PlayerManager playerManager;
    private CombatManager combatManager;
    private InventoryManager inventoryManager;
    private UIManager uiManager;
    private ConversationManager conversationManager;
    private CompanionManager companionManager;
    private CraftingManager craftingManager;

    
    // CSV-fake loader
    private FakeGameDatabase fakeDb = new FakeGameDatabase();

    public GameEngine() {
        this.inputHandler = new GameInputHandler(this);
        this.roomManager = new RoomManager(this);
        this.playerManager = new PlayerManager(this);
        this.combatManager = new CombatManager(this);
        this.inventoryManager = new InventoryManager(this);
        this.uiManager = new UIManager(this);
        this.conversationManager = new ConversationManager(this);
        this.companionManager = new CompanionManager(this);
        this.craftingManager = new CraftingManager(this);
    }

    /** Called once to seed/initialize, then loadData. */
    public void start() {
        if (!USE_FAKE_DB) {
            DatabaseInitializer.initialize();
            System.out.println("Seeding DB for the first time…");
        }
        loadData();
        this.isRunning = true;
    }

    /** Load rooms + player either from Derby or from CSV (FakeGameDatabase). */
    public void loadData() {
        if (USE_FAKE_DB) {
            // 1) Rooms + NPCs
            this.rooms.clear();
            this.rooms.addAll(fakeDb.loadAllRooms());

            // 2) Player + inventory
            this.player = fakeDb.loadPlayer();
            this.currentRoomNum = 0;  // start in room #1

            // 3) Conversations can remain empty or implement a fake loader later
            this.conversationManager.setConversations(new HashMap<>());
        }
        else {
            // real Derby path
            roomManager.loadRooms();                      // from DerbyDatabase
            conversationManager.loadConversations();      // fills NPC conversation trees
            for (Room room : getRooms()) {
                for (Character npc : room.getCharacterContainer()) {
                    ConversationTree tree = conversationManager.getConversation(npc.getName());
                    if (tree != null) {
                        if (npc instanceof NPC) {
                            ((NPC) npc).addConversationTree(tree);
                        }
                    }
                }
            }
            playerManager.loadPlayer();                   // from PLAYER & PLAYER_INVENTORY tables
            GameStateManager.loadState(this);             // from GAME_STATE
            this.currentRoomNum = getCurrentRoomNum();
        }
    }
    
    /** Like start(), but only load rooms & conversations so we can inject our own Player. */
    public void startWithoutPlayer() {
        if (!USE_FAKE_DB) {
            DatabaseInitializer.initialize();
        }
        // 1) Rooms
        roomManager.loadRooms();
        // 2) Conversations
        conversationManager.loadConversations();
        this.currentRoomNum = 0;
        this.isRunning = true;
    }

    /** After startWithoutPlayer(), load exactly the class they picked. */
    public void loadPlayerOfClass(String className) {
    	try {
    		new PlayerManager(this).loadPlayerByType(className);
    	       } catch (SQLException e) {
    	           throw new RuntimeException("Failed to load player of class: " + className, e);
    	       }
    }
    
    // Getters and setters for managers to access GameEngine state
    public Player getPlayer() {
        return player;
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    public ArrayList<Room> getRooms() {
        return rooms;
    }
    
    public int getCurrentRoomNum() {
        return this.currentRoomNum;
    }
    
    public void setCurrentRoomNum(int roomNum) {
        this.currentRoomNum = roomNum;
    }
    
    public String getRunningMessage() {
        return runningMessage;
    }
    
    public void setRunningMessage(String message) {
        this.runningMessage = message;
    }
    
    public void appendMessage(String message) { 
        this.runningMessage += message;
    }
    
    // Core navigation method - delegates to RoomManager
    public String updateCurrentRoom(String direction) {
        return roomManager.updateCurrentRoom(direction);
    }
    
    // Helper methods that delegate to managers
    public int getMapOutput(String direction) {
        return roomManager.getMapOutput(direction);
    }
    
    public String getCurrentRoomName() {
        return roomManager.getCurrentRoomName();
    }
    
    public String getRoomName(int roomNum) {
        return roomManager.getRoomName(roomNum);
    }
    
    // Movement commands - delegate to RoomManager
    public String getGo(String noun) {
        return roomManager.getGo(noun);
    }
    
    public String getOnShuttle() {
        return roomManager.getOnShuttle();
    }
    
    //Companion Commands
    public String chooseCompanion(int companionID) {
    	return companionManager.chooseCompanion(companionID);
    }
    
    public String shooCompanion(int companionID) {
    	return companionManager.shooCompanion(companionID);
    }
     
    // Combat commands - delegate to CombatManager
    public String playerAttackChar(int itemNum, int characterNum) {
        return combatManager.playerAttackChar(itemNum, characterNum);
    }
    
    // Inventory and item commands - delegate to InventoryManager
    public String pickupItem(int itemNum) {
        return inventoryManager.pickupItem(itemNum);
    }
    
    public String pickupItemFromCompanion(int itemNum) {
        return inventoryManager.pickupItemFromCompanion(itemNum);
    }
    
    public String dropItem(int itemNum) {
        return inventoryManager.dropItem(itemNum);
    }
    
    public String giveItemToCompanion(int itemNum) {
        return inventoryManager.giveItemToCompanion(itemNum);
    }
    
    public String usePotion(int itemNum) {
        return inventoryManager.usePotion(itemNum);
    }
    public String equipArmor(int itemNum) {
        return inventoryManager.equipArmor(itemNum);
    }
    public String unequipArmor(String name) {
        return inventoryManager.unequipArmor(name);
    }
    public String examineItemName(int itemNum) {
        return inventoryManager.examineItemName(itemNum);
    }
    
    // Helper methods to convert names to IDs - delegate to appropriate managers
    public int RoomItemNameToID(String name) {
        return inventoryManager.RoomItemNameToID(name);
    }
    
    public int CompanionItemNameToID(String name) {
        return inventoryManager.CompanionItemNameToID(name);
    }
    
    public int CharItemNameToID(String name) {
        return inventoryManager.CharItemNameToID(name);
    }
    
    public int CharNameToID(String name) {
        return roomManager.CharNameToID(name);
    }
    
    // Examination commands
    public String examineCharacter(int charNum) {
        return roomManager.examineCharacter(charNum);
    }
    
    public String examineCompanion(String name) {
    	return companionManager.examineCompanion(name);
    }
    
    public int RoomCompanionNameToID(String name) {
        return companionManager.RoomCompanionNameToID(name);
    }
    
    public int playerCompanionNameToID(String name) {
    	return companionManager.playerCompanionNameToID(name);
    }
    
    public int companionNameToID(String name) {
    	return roomManager.CompanionNameToID(name);
    }
    
    public String getExamine(String noun) {
        String message = "";
        int itemNum = CharItemNameToID(noun);
        int charNum = CharNameToID(noun);
        int companionNum = companionNameToID(noun);
        
        if(noun.toLowerCase().equals("room")) {
            message = "\n" + roomManager.getCurrentRoom().getRoomDescription();
        }
        if(charNum >= 0) {
            message = examineCharacter(charNum);
        }
        if(itemNum >= 0) {
            message = examineItemName(itemNum);
        }
        if(companionNum >= 0) {
        	message = examineCompanion(noun);
        }
        return message;
    }
    
    // NPC interaction methods - delegate to RoomManager
    public String talkToNPC(int characterNum) {
        return roomManager.talkToNPC(characterNum);
    }
    
    public String[] getResponseOptions(int characterNum) {
        return roomManager.getResponseOptions(characterNum);
    }
    
    public String interactWithNPC(String choice, int characterNum) {
        return roomManager.interactWithNPC(choice, characterNum);
    }
    
    public boolean reachedFinalNode() {
        return true;
    }
    
    // Help command
    public String getHelp() {
        return uiManager.getHelp();
    }
    
    // Input processing - delegates to InputHandler
    public boolean processInput(String input) {
        return inputHandler.processInput(input);
    }
    
    // UI/Display methods - delegate to UIManager
    public String getCurrentRoomItems() {
        return uiManager.getCurrentRoomItems();
    }
    
    public String getPlayerInventoryString() {
        return uiManager.getPlayerInventoryString();
    }
    
    public String getPlayerInfo() {
        return uiManager.getPlayerInfo();
    }
    
    public String getRoomCharactersInfo() {
        return uiManager.getRoomCharactersInfo();
    }
    
    public String getRoomConnectionOutput() {
        return uiManager.getRoomConnectionOutput();
    }
    
    public String getCurrentRoomImage() {
        return uiManager.getCurrentRoomImage();
    }
    
    public String getRoomItemsOverlay() {
        return uiManager.getRoomItemsOverlay();
    }
    
    public String getRoomCharactersOverlay() {
        return uiManager.getRoomCharactersOverlay();
    }
    
    public String getCurrentRoomNumber() {
        return String.valueOf(currentRoomNum + 1);
    }
    
    // conversation manager if needed
    public ConversationManager getConversationManager() {
        return conversationManager;
    }
    
    
    // Main display method for constructing Response
    public Response display() {
        // if the player is dead, return a special "game over" response
        if (player != null && player.getHp() <= 0) {
            Response resp = new Response();
            resp.setGameOver(true);
            // relative to your webapp root—adjust if needed
            resp.setGameOverImage("/images/GameOver.png");
            return resp;
        }
        // otherwise, fall back to the normal UIManager flow
        return uiManager.display();
    }
    //Companion methods in gameEngine

    
    public String reset() {
    		DerbyDatabase.reset(tables);
       		DatabaseInitializer.initialize();
       		loadData();
       		this.currentRoomNum = 0;
       		return "\n<b>Game was restarted to original state </b>";
    }
       public void initialize() {
    	   DatabaseInitializer.initialize();
    	   loadData();
    }

    // Disassemble command wrapper
    public String disassembleItem(String itemName) {
        return craftingManager.disassembleItem(itemName);
    }

    // Combine two components into a new item
    public String combineItems(String compA, String compB) {
        return craftingManager.combineItems(compA, compB);
    }

}