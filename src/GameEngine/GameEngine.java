package GameEngine;

import java.util.*;
import models.*;

public class GameEngine {
    private Player player;
    private boolean isRunning = false;
    private int currentRoomNum;
    private ArrayList<Room> rooms = new ArrayList<>();
    private String runningMessage = "";
    private String error = "";
    private GameInputHandler inputHandler;
    
    // Managers for different components
    private RoomManager roomManager;
    private PlayerManager playerManager;
    private CombatManager combatManager;
    private InventoryManager inventoryManager;
    private UIManager uiManager;
    
    // Empty instantiation so data can be loaded using loadData()
    public GameEngine() {
        this.inputHandler = new GameInputHandler(this);
        this.roomManager = new RoomManager(this);
        this.playerManager = new PlayerManager(this);
        this.combatManager = new CombatManager(this);
        this.inventoryManager = new InventoryManager(this);
        this.uiManager = new UIManager(this);
    }
    
    // called after creating the GameEngine instantiation in the session to load the current data and set isRunning to true
    public void start() {
        loadData();
        this.isRunning = true;
    }
    
    // "loads data" from .csv file in future but for now is where we create the instantiation of the game state for our demo
    public void loadData() {
        roomManager.loadRooms();
        playerManager.loadPlayer();
        this.currentRoomNum = 0;
        String roomName = getCurrentRoomName();
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
     
    // Combat commands - delegate to CombatManager
    public String playerAttackChar(int itemNum, int characterNum) {
        return combatManager.playerAttackChar(itemNum, characterNum);
    }
    
    // Inventory and item commands - delegate to InventoryManager
    public String pickupItem(int itemNum) {
        return inventoryManager.pickupItem(itemNum);
    }
    
    public String dropItem(int itemNum) {
        return inventoryManager.dropItem(itemNum);
    }
    
    public String usePotion(int itemNum) {
        return inventoryManager.usePotion(itemNum);
    }
    
    public String examineItemName(int itemNum) {
        return inventoryManager.examineItemName(itemNum);
    }
    
    // Helper methods to convert names to IDs - delegate to appropriate managers
    public int RoomItemNameToID(String name) {
        return inventoryManager.RoomItemNameToID(name);
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
    
    public String getExamine(String noun) {
        String message = "";
        int itemNum = CharItemNameToID(noun);
        int charNum = CharNameToID(noun);
        
        if(noun.toLowerCase().equals("room")) {
            message = "\n" + roomManager.getCurrentRoom().getRoomDescription();
        }
        if(charNum >= 0) {
            message = examineCharacter(charNum);
        }
        if(itemNum >= 0) {
            message = examineItemName(itemNum);
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
    
    // Main display method for constructing Response
    public Response display() {
        return uiManager.display();
    }
}