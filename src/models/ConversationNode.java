package models;
//ConversationNode.java
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConversationNode {
 private String message;
 private boolean becomeAggressive;
 private boolean dropItem;
 private int itemToDrop;
 private Map<String, ConversationNode> responses;

 public ConversationNode(String message) {
     this.message = message;
     this.responses = new HashMap<>();
 }

 // Flag setters
 public void setBecomeAggressive(boolean becomeAggressive) {
     this.becomeAggressive = becomeAggressive;
 }

 public void setDropItem(boolean dropItem) {
     this.dropItem = dropItem;
 }

 public void setItemToDrop(int itemToDrop) {
     this.itemToDrop = itemToDrop;
 }

 // Flag getters
 public boolean isBecomeAggressive() {
     return becomeAggressive;
 }

 public boolean isDropItem() {
     return dropItem;
 }

 public int getItemToDrop() {
     return itemToDrop;
 }

 public String getMessage() {
     return message;
 }

 // Link a response using a key (for example, "1" or "2")
 public void addResponse(String input, ConversationNode node) {
     responses.put(input, node);
 }

 // Get the next node based on player's input
 public ConversationNode getNextNode(String input) {
     return responses.get(input);
 }
 
 public Set<String> getResponseKeys() {
     return responses.keySet();
 }
}
