import java.util.ArrayList;
import java.util.List;

public class Character {
   protected String name;
   protected int hp;
   protected Inventory inventory;
   public Character(String name, int hp) {
       this.name = name;
       this.hp = hp;
       this.inventory = new Inventory();
   }
     public void callInventory() {
      inventory.showItems();
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
}
