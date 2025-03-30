package models;

import javax.persistence.GeneratedValue;

import orm.annotations.*;

@Entity
@Table(name = "npcs")
public class NPC extends Character {
   @Column(name = "aggression")
   private boolean aggression;
   private String[] dialogue;
   @Column(name = "damage")
   private int damage;
   public NPC(String name, int hp, boolean aggression, String[] dialogue, int damage, Inventory inventory, String longdescription, String shortdescription) {
       super(name, hp, inventory, longdescription, shortdescription);
       this.aggression = aggression;
       this.dialogue = dialogue;
       this.damage = damage;
   }
   public void attackPlayer(Player player) {
       if (aggression) {
           System.out.println(name + " attacks " + player.getName() + " for " + damage + " damage!");
           player.setHp(player.getHp() - damage);
       }
   }
   
   public NPC() {}
   
   /*public String converse() {
       return name + " says: " + dialogue;
   }*/
   
   public String converse() {
       return name + " says: " + (dialogue != null ? String.join(" ", dialogue) : "...");
   }
   
   public boolean getAggresion(){
	   return this.aggression;
   
   }
   
   public int getDamage() { return damage; }
   public void setDamage(int damage) { this.damage = damage; }

   public String[] getDialogue() { return dialogue; }
   public void setDialogue(String[] dialogue) { this.dialogue = dialogue; }
   
   public void setAgression(boolean aggression){
	   this.aggression = aggression;
   }
}
