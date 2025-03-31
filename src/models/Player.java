package models;

import orm.annotations.*;

@Entity
@Table(name = "characters") // ADDED: Use the same table as the base Character class.
public class Player extends Character {
   
   // private Room location;
   public Player(String name, int hp, int skillPoints, Inventory inventory, String longdescription, String shortdescription) {
       super(name, hp, inventory, longdescription, shortdescription);
       this.setSkillPoints(skillPoints);
   }
   public void move(String direction) {
       System.out.println(name + " moves " + direction + " to a new location.");
       // Implement movement logic based on Room class
   }
   
   public Player() {
	   super();
	}
   
   public void useSkillPoints() {
       if (this.getSkillPoints() > 0) {
    	   this.setSkillPoints(this.getSkillPoints() - 1);
           System.out.println(name + " used a skill point. Remaining: " + this.getSkillPoints());
       } else {
           System.out.println(name + " has no skill points left.");
       }
   }
}
