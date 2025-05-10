package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Companion extends NPC {
	private boolean companion;

	public Companion(String name, int hp, boolean aggression, String[] dialogue, int damage, Inventory inventory,
			String longdescription, String shortdescription, boolean companion) {
		super(name, hp, aggression, dialogue, damage, inventory, longdescription, shortdescription);
		this.companion = companion;
	}

	public int getAttack() {
		return this.damage;
	}
	
	public int getHp() {
		return hp;
	}
	
	public String getName() {
		return name;
	}
	
	public Inventory getInventory() {
		return inventory;
	}

    public Item getItem(int itemNum)
    {
        return this.inventory.getItem(itemNum);
    }
    
    public String getItemName(int itemNum)
    {
        return inventory.getItemName(itemNum);
    }
    
    public void removeItem(int itemNum)
    {
        this.inventory.removeItem(itemNum);
    }

	public boolean getCompanion() {
		return this.companion;
	}
	
	public void removeCompanion(int companionNumber) {
		if (companionNumber >= 0) { 
            this.companion = false;
        }
	}

	public String converse() {
		return name + " says: " + dialogue;
	}

	public void addConversationTree(ConversationTree conversationTree) {
		this.conversationTree = conversationTree;
	}

	public String interact(String input) {
		conversationTree.traverse(input);
		ConversationNode node = conversationTree.getCurrentNode();
		return node.getMessage();
	}

	public String getMessage() {
		if (conversationTree == null || conversationTree.getCurrentNode() == null) {
			return "<b>This character has nothing to say.</b>";
		}
		return conversationTree.getCurrentNode().getMessage();
	}

	public String[] getResponseOptions() {
		    if (conversationTree == null || conversationTree.getCurrentNode() == null) {
		        return null;
		    }
		    return conversationTree.getCurrentNodeResponseOptions();
		    
	}
}