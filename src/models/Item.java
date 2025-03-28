package models;
public class Item {
	private int value, weight;
	private String name;
	private String[] components;
	
	public Item(int value, int weight, String name, String[] components) {
		this.weight = weight;
		this.value = value;
		this.name = name;
		this.components = components;
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

	public String getDescription() {
		return null;
	}
}
