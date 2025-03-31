package models;

import java.util.ArrayList;
import orm.OrmManager;
import orm.annotations.*;
import java.util.List;

import javax.persistence.GeneratedValue;

@Entity
@Table(name = "inventories")
public class Inventory {
	private ArrayList<Item> inventory;
	@Column(name = "current_weight")
	private int currentWeight;
	@Column(name = "max_weight")
	private int maxWeight;
	
	@Id
	@GeneratedValue
	@Column(name = "id")
    private int id;
	
	public Inventory(ArrayList<Item> inventory, int maxWeight) {
		this.inventory = inventory;
		this.maxWeight = maxWeight;
		currentWeight = 0;
		for(int i = 0; i < inventory.size(); i++) {
			currentWeight += inventory.get(i).getWeight();
		}
	}
	
	public int getId() { return id; }
    public void setId(int id) { this.id = id; }
	
	public Inventory() {
		this.inventory = new ArrayList<>();
	}
	
	public void addItem(Item item) {
		/*if(currentWeight + item.getWeight() < maxWeight) {
			inventory.add(item);
			currentWeight += item.getWeight();
		}*/
		inventory.add(item);
		currentWeight += item.getWeight();
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
	
	public int getSize()
	{
		return inventory.size();
	}
	
	public String getItemName(int itemNum)
	{
		Item item = inventory.get(itemNum);
		return item.getName();
	}
	
	public void loadItemsFromDB(OrmManager orm) throws Exception {
		List<InventoryItemLink> links = orm.findAll(InventoryItemLink.class);
		List<Item> allItems = orm.findAll(Item.class);
		ArrayList<Item> result = new ArrayList<>();

		for (InventoryItemLink link : links) {
			if (link.getInventoryId() == this.id) {
				for (Item item : allItems) {
					if (item.getId() == link.getItemId()) {
						result.add(item);
						break;
					}
				}
			}
		}
		this.setInventory(result);
	}
}
