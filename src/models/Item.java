package models;
import java.util.List;
import java.util.ArrayList;
public class Item {
	private int value, weight;
	private String name;
	private String longdescription;
	private String shortdescription;
	private String[] components;
	private Boolean examined;
	
	public Item(int value, int weight, String name, String[] components, String longdescription, String shortdescription) {
		this.weight = weight;
		this.value = value;
		this.name = name;
		this.components = components;
		this.longdescription = longdescription;
		this.shortdescription = shortdescription;
		examined = false;
	}
	
	public String getDescription() {
		if(!examined) {
			examined = true;
			return longdescription;
		}
		else {
			return shortdescription;
		}
	}
	
	public void setDescription(String longdescription, String shortdescription) {
		this.longdescription = longdescription;
		this.shortdescription = shortdescription;
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
	
	public List<String> getComponents() {
		int length = components.length;
		List<String> comps = new ArrayList<String>();
		for (int i = 0; i < length; i++)
		{
			comps.add(components[i]);
		}
        return comps;
    }
	
	public void setComponents(String[] components) {
		this.components = components;
	}


}
