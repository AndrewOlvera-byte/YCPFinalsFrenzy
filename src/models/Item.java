package models;
public class Item {
	private int value, weight;
	private String name;
	private String description;
	private String[] components;
	
	public Item(int value, int weight, String name, String[] components, String description) {
		this.weight = weight;
		this.value = value;
		this.name = name;
		this.components = components;
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	} 
	
	public String getName() {
		return name; 
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String[] getComponents() {
        return components.clone();
    }
	
	public void setComponents(String[] components) {
		this.components = components;
	}
}
