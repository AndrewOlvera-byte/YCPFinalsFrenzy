package models;

public class NPC extends Character {
   private boolean aggression;
   private String[] dialogue;
   private int damage;
   private ConversationTree conversationTree;
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
   
   public void addConversationTree(ConversationTree conversationTree)
   {
	   this.conversationTree = conversationTree;
   }
   
   public String interact(String input) {
       conversationTree.traverse(input);
       ConversationNode node = conversationTree.getCurrentNode();
       return node.getMessage();
   }
   
   public String getMessage()
   {
	   return this.conversationTree.getCurrentNodeMessage();
   }
   
   public String[] getResponseOptions()
   {
	   String[] responses = conversationTree.getCurrentNodeResponseOptions();
	   return responses;
   }
   public boolean isCurrentNodeToAggressive()
   {
	   return this.conversationTree.isCurrentNodeToAggressive();
   }
   
   public boolean isCurrentNodeDropItem()
   {
	   return this.conversationTree.isCurrentNodeDropItem();
   }
   public int getItemToDrop()
   {
	   return this.conversationTree.getItemToDrop();
   }
}
