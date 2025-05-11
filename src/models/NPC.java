package models;

public class NPC extends Character {
   private boolean aggression;
   protected String[] dialogue;
   protected int damage;
   protected ConversationTree conversationTree;
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
   
   public String getMessage() {
       // DEBUG: entering NPC.getMessage
       System.out.println("DEBUG: NPC.getMessage for '" + name + "', conversationTree!=null: " + (conversationTree != null));
       if (conversationTree != null && conversationTree.getCurrentNode() != null) {
           System.out.println("DEBUG: NPC.getMessage returning tree message: " + conversationTree.getCurrentNode().getMessage());
           return conversationTree.getCurrentNode().getMessage();
       }
       if (dialogue != null && dialogue.length > 0) {
           System.out.println("DEBUG: NPC.getMessage falling back to dialogue[]");
           return name + " says: " + String.join(" ", dialogue);
       }
       System.out.println("DEBUG: NPC.getMessage falling back to default 'This character has nothing to say.'");
       return "<b>This character has nothing to say.</b>";
   }

   
   public String[] getResponseOptions() {
	    if (conversationTree == null || conversationTree.getCurrentNode() == null) {
	        return null;
	    }
	    return conversationTree.getCurrentNodeResponseOptions();
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
