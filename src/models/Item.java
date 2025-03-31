package models;

import orm.annotations.*;

@Entity
@Table(name = "items")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Item {
	@Id
    @GeneratedValue
    @Column(name = "id")
    private int id;
	@Column(name = "value")
	private int value;
	@Column(name = "weight")
	private int weight;
	@Column(name = "name")
	private String name;
	@Column(name = "longdescription")
	private String longdescription;
	@Column(name = "shortdescription")
	private String shortdescription;
	private String[] components;
	@Column(name = "examined")
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
	
	public Item() {
    }
	
	public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
	
	public String[] getComponents() {
        return components.clone();
    }
	
	public void setComponents(String[] components) {
		this.components = components;
	}
}
