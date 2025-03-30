package models;

import javax.persistence.GeneratedValue;

import orm.annotations.*;

@Entity
@Table(name = "players")
public class Player extends Character {
	
	@Column(name = "skill_points")
	private int skillPoints;
	
	public Player() {
        super();
    }
	
   // private Room location;
   public Player(String name, int hp, int skillPoints, Inventory inventory, String longdescription, String shortdescription) {
       super(name, hp, inventory, longdescription, shortdescription);
       this.skillPoints = skillPoints;
   }
   
   public int getSkillPoints() { return skillPoints; }
   public void setSkillPoints(int skillPoints) { this.skillPoints = skillPoints; }
   
   public void move(String direction) {
       System.out.println(name + " moves " + direction + " to a new location.");
       // Implement movement logic based on Room class
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
