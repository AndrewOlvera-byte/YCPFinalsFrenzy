package models;
import java.util.ArrayList;


public class Character {
   protected String name;
   protected int hp;
   protected int maxHp;           // New field for maximum health
   protected Inventory inventory;
   private boolean justAttacked = false;
   private String longdescription;
   private String shortdescription;
   private Boolean examined;
   public Character(String name, int hp, Inventory inventory, String longdescription, String shortdescription) {
       this.name = name;
       this.hp = hp;
       this.maxHp = hp;         // Assume starting hp is the max health
       this.inventory = inventory;
       this.longdescription = longdescription;
       this.shortdescription = shortdescription;
       this.examined = false;
   }
   
   public void callInventory() {
      //inventory.showItems();
   }
   
   public void combat(Character opponent) {
       System.out.println(name + " is bouta brawl with " + opponent.getName());
   }
   
   public String getName() {
       return name;
   }
   
   public int getHp() {
       return hp;
   }
   
   public void setHp(double newHealth) {
       this.hp = (int) newHealth;
   }
   
   public int getMaxHp() {     // New getter for max health
       return this.maxHp;
   }
   
   public int getAttackDmg(int itemNum) {
	    if (inventory.getSize() == 0) {
	        // Default attack damage if inventory is empty
	        return 1; // Set your desired default damage
	    }

	    if (itemNum < 0 || itemNum >= inventory.getSize()) {
	        // Item number is invalid, default attack damage
	        return 1; // Again, choose an appropriate default
	    }

	    Item item = inventory.getItem(itemNum);
	    if (item instanceof Weapon) {
	        Weapon weapon = (Weapon) item;
	        return weapon.getAttackDmg();
	    } else {
	        // If item isn't a Weapon, return default attack damage
	        return 1;
	    }
	}

   public boolean isAgressive() {
       return false;
   }
   
   public void addItem(Item item) {
       this.inventory.addItem(item);
   }
   
   public Item getItem(int itemNum) {
       Item item = inventory.getItem(itemNum);
       return item;
   }
   
   public void removeItem(int itemNum) {
       if (itemNum < 0 || itemNum >= inventory.getSize()) {
           return;
       }
       this.inventory.removeItem(itemNum);
   }
   
   public boolean getJustAttacked() {
       return this.justAttacked;
   }
   
   public void setJustAttacked(boolean value) {
       this.justAttacked = value;
   }
   
   public int getInventorySize() {
       return this.inventory.getSize();
   }
   
   public String getItemName(int itemNum) {
       return this.inventory.getItemName(itemNum);
   }
   
   public Inventory getInventory() {
       return inventory;
   }
   
   public ArrayList<Item> dropAllItems() {
       ArrayList<Item> droppedItems = new ArrayList<>();
       while (inventory.getSize() > 0) {
           Item droppedItem = inventory.getItem(0);
           droppedItems.add(droppedItem);
           inventory.removeItem(0);
       }
       return droppedItems;
   }
   
   
   public String getCharDescription() {
	   if(!examined) {
		   examined = true;
		   return longdescription;
	   }
	   else {
		   return shortdescription;
	   }
   }
   
   public void setCharDescription(String longdescription, String shortdescription) {
	   this.longdescription = longdescription;
	   this.shortdescription = shortdescription;
   }
}
