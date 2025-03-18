package models;
import java.util.ArrayList;
import java.util.List;

public class Character {
   protected String name;
   protected int hp;
   protected Inventory inventory;
   public Character(String name, int hp, Inventory inventory) {
       this.name = name;
       this.hp = hp;
       this.inventory = inventory;
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
   
   
}
