package models;


import GameEngine.GameEngine;


public class InventoryManager {
    private GameEngine engine;
    
    public InventoryManager(GameEngine engine) {
        this.engine = engine;
    }
    
    // Method for player to pickup item itemNum from room inventory
    public String pickupItem(int itemNum) {
        if (itemNum == -1) {
            return "\nPick up what?";
        }
        
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        String itemName = currentRoom.getItemName(itemNum); // temp name for the item name when we remove it
        Item item = currentRoom.getItem(itemNum);
        currentRoom.removeItem(itemNum);
        engine.getPlayer().addItem(item);
        
        return "<b>\n" + itemName + " was picked up.</b>";
    }
    
    // Method to drop an item from player inventory into room inventory
    public String dropItem(int itemNum) {
        if (itemNum == -1) {
            return "\n<b>Drop what?</b>";
        }
        
        if (itemNum < 0 || itemNum >= engine.getPlayer().getInventorySize()) {
            return "\n<b>Invalid item selection.</b>";
        }
        
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        Item InvenItem = engine.getPlayer().getItem(itemNum);
        engine.getPlayer().removeItem(itemNum);
        currentRoom.addItem(InvenItem);
        
        return "\n<b>" + InvenItem.getName() + " was dropped.</b>";
    }
    
    // Helper method to convert from an items name to its ID in room
    public int RoomItemNameToID(String name) {
        int itemNum = -1; // set to -1 so if there is no item found it will return -1
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        for (int i = 0; i < currentRoom.getInventorySize(); i++) {
            if (name.equalsIgnoreCase(currentRoom.getItemName(i))) {
                itemNum = i;
                break;
            }
        }
        return itemNum;
    }
    
    // Helper method to convert from an items name to its ID in player inventory
    public int CharItemNameToID(String name) {
        int itemNum = -1; // set to -1 so if there is no item found it will return -1
        
        for (int i = 0; i < engine.getPlayer().getInventorySize(); i++) {
            if (name.equalsIgnoreCase(engine.getPlayer().getItemName(i))) {
                itemNum = i;
                break;
            }
        }
        return itemNum;
    }
    
    // Method to examine an item
    public String examineItemName(int itemNum) {
        if(itemNum == -1) {
            return "\n<b>Examine what Item?</b>";
        }
        
        if(itemNum < 0 || itemNum >= engine.getPlayer().getInventorySize()) {
            return "\n<b>Invalid item selection.</b>";
        }
        
        Item InvenItem = engine.getPlayer().getItem(itemNum);
        return "\n" + InvenItem.getDescription();
    }
    
    // Method to use potions and consumables
    public String usePotion(int itemNum) {
        if (itemNum == -1) {
            return "\n<b>Use what?</b>";
        }
        
        if (itemNum < 0 || itemNum >= engine.getPlayer().getInventorySize()) {
            return "\n<b>Invalid item selection.</b>";
        }
        
        Item InvenItem = engine.getPlayer().getItem(itemNum);
        
        if (!(InvenItem instanceof Utility)) {
            return "<b>\nYou can't drink or apply that!</b>";
        }
        
        Utility potion = (Utility) InvenItem;
        double Multi = potion.getDamageMulti();
        int Healing = potion.getHealing();

        int newHp = engine.getPlayer().getHp() + Healing;
        if (Multi != 0) {
            engine.getPlayer().setdamageMulti(Multi);
        }
        engine.getPlayer().setHp(newHp);
        engine.getPlayer().removeItem(itemNum);
        
        return "\n<b>" + InvenItem.getName() + " was applied.</b>";
    }
}