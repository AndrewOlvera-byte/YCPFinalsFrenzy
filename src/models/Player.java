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
   
   public void useSkillPoints() {
       if (skillPoints > 0) {
           skillPoints--;
           System.out.println(name + " used a skill point. Remaining: " + skillPoints);
       } else {
           System.out.println(name + " has no skill points left.");
       }
   }
}
