package models;

public class NPC extends Character {
   private boolean aggression;
   private String[] dialogue;
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
   public String converse() {
       return name + " says: " + dialogue;
   }
   
   public boolean getAggresion(){
	   return this.aggression;
   
   }
   public void setAgression(boolean aggression){
	   this.aggression = aggression;
   }
}
