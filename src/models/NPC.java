package models;

import orm.annotations.*;

@Entity
@Table(name = "characters") // ADDED: Use the same table as the base Character class.
public class NPC extends Character {
   public NPC(String name, int hp, boolean aggression, String[] dialogue, int damage, Inventory inventory, String longdescription, String shortdescription) {
       super(name, hp, inventory, longdescription, shortdescription);
       this.setAggression(aggression);
       this.setDialogue(dialogue != null ? String.join(",", dialogue) : "");
       this.setDamage(damage);
   }
   
   public NPC() {
	   super();
   }
   
   public String getDialogue() {
       return getDialogue();
   }

   public void setDialogue(String[] dialogue) {
       setDialogue(dialogue);
   }
   
   public void attackPlayer(Player player) {
       if (this.getAggression() != null && this.getAggression()) {
           System.out.println(getName() + " attacks " + player.getName() + " for " + this.getDamage() + " damage!");
           player.setHp(player.getHp() - this.getDamage());
       }
   }
   public String converse() {
       return name + " says: " + this.getDialogue();
   }
   
   public boolean getAggresion(){
	   return this.getAggression() != null ? this.getAggression() : false;
   }
   public void setAgression(boolean aggression){
	   this.setAggression(aggression);
   }
}
