package models;
import java.util.ArrayList;

public class Inventory {
	private ArrayList<Item> inventory;
	private int currentWeight;
	private int maxWeight;
	
	public Inventory(ArrayList<Item> inventory, int maxWeight) {
		this.inventory = inventory;
		currentWeight = 0;
		for(int i = 0; i < inventory.size(); i++) {
			currentWeight += inventory.get(i).getWeight();
		}
		this.maxWeight = maxWeight;
	}
	
	public void addItem(Item item) {
		if(currentWeight + item.getWeight() < maxWeight) {
			inventory.add(item);
			currentWeight += item.getWeight();
		}
	}
	
	public void removeItem(int itemNumber) {
		if (itemNumber >= 0 && itemNumber < inventory.size()) { 
            currentWeight -= inventory.get(itemNumber).getWeight();
            inventory.remove(itemNumber);
        }
	}
	
	public String listItems() {
        if (inventory.isEmpty()) {
            return "Inventory is empty.";
        }
        StringBuilder items = new StringBuilder();
        for (int i = 0; i < inventory.size(); i++) {
            if (i > 0) {
                items.append(", ");
            }
            items.append(inventory.get(i).getName());
        }
        return items.toString();
    }
	
	public int getCurrentWeight() {
		return currentWeight;
	}
	
	public void setCurrentWeight(int currentWeight) {
		this.currentWeight = currentWeight;
	}
	
	public int getMaxWeight() {
		return maxWeight;
	}
	
	public void setMaxWeight(int maxWeight) {
		this.maxWeight = maxWeight;
	}
	
	public ArrayList<Item> getInventory() {
	    return new ArrayList<>(inventory);
	}
	
	public void setInventory(ArrayList<Item> inventory) {
		this.inventory = inventory;
	}
	
	public Item getItem(int itemNum)
	{
		Item item = inventory.get(itemNum);
		return item;
	}
}
