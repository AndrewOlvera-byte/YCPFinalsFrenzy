package models;
import java.util.ArrayList;
import java.util.List;

public class Character {
   protected String name;
   protected int hp;
   protected Inventory inventory;
   private boolean justAttacked = false;
   private String longdescription;
   private String shortdescription;
   private Boolean examined;
   public Character(String name, int hp, Inventory inventory, String longdescription, String shortdescription) {
       this.name = name;
       this.hp = hp;
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
   public void setHp(int hp) {
       this.hp = hp;
   }
   
   public int getAttackDmg(int itemNum)
   {
	   Item item = inventory.getItem(itemNum);
	   Weapon weapon = (Weapon) item;
	   return weapon.getAttackDmg();
	   
   }
   public boolean isAgressive()
   {
	   return false;
   }
   
   public void addItem(Item item)
   {
	   this.inventory.addItem(item);
   }
   
   public Item getItem(int itemNum)
   {
	   Item item = inventory.getItem(itemNum);
	   return item;
   }
   
   public void removeItem(int itemNum)
   {
	   this.inventory.removeItem(itemNum);
   }
   
   public boolean getJustAttacked()
   {
	   return this.justAttacked;
   }
   
   public void setJustAttacked(boolean value)
   {
	   this.justAttacked = value;
   }
   
   public int getInventorySize()
   {
	   return this.inventory.getSize();
   }
   
   public String getItemName(int itemNum)
   {
	   return this.inventory.getItemName(itemNum);
   }
   
   public ArrayList<Item> dropAllItems() 
   {
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
