package models;
public class ConversationTree {
    private ConversationNode root;
    private ConversationNode currentNode;

    public ConversationTree(ConversationNode root) {
        this.root = root;
        this.currentNode = root;
    }

    public ConversationNode getCurrentNode() {
        return currentNode;
    }

    // Traverse to the next node based on input.
    // If input is invalid, the current node remains unchanged.
    public void traverse(String input) {
        ConversationNode nextNode = currentNode.getNextNode(input);
        if (nextNode != null) {
            currentNode = nextNode;
        } else {
            System.out.println("Invalid input, please try again.");
        }
    }
    
    public String getCurrentNodeMessage()
    {
    	return this.currentNode.getMessage();
    }
    
    public String[] getCurrentNodeResponseOptions()
    {
    	return currentNode.getResponseKeys().toArray(new String[0]);
    }
    
    public boolean isCurrentNodeToAggressive()
    {
    	return currentNode.isBecomeAggressive();
    }
    
    public boolean isCurrentNodeDropItem()
    {
    	return currentNode.isDropItem();
    }
    public int getItemToDrop()
    {
    	return currentNode.getItemToDrop();
    }
    
}
