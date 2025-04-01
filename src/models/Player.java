package models;

public class Player extends Character {
   private int skillPoints;
   private int damageMulti;
   // private Room location;
   public Player(String name, int hp, int skillPoints, Inventory inventory, String longdescription, String shortdescription,int damageMulti) {
       super(name, hp, inventory, longdescription, shortdescription);
       this.skillPoints = skillPoints;
       this.damageMulti = damageMulti;
   }
   public void move(String direction) {
       System.out.println(name + " moves " + direction + " to a new location.");
       // Implement movement logic based on Room class
   }
   
   public int getdamageMulti() {
		return damageMulti;
	}
	
	public void setdamageMulti(int damageMulti) {
		this.damageMulti = damageMulti;
	}
    
    // New method to check if the player has a specific key in their inventory.
    public boolean hasKey(String keyName) {
        // Assume getInventory() is available from the Character class
        for (int i = 0; i < getInventory().getSize(); i++) {
            if (getInventory().getItemName(i).equalsIgnoreCase(keyName)) {
                return true;
            }
        }
        return false;
    }
}
