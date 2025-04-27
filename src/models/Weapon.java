package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Weapon extends Item
{
	private int attackDmg;
	private List<String> components;
	
	public Weapon(int value, int weight, String name, String[] components, int attackDmg, String longdescription, String shortdescription)
	{
		super(value, weight, name, components, longdescription, shortdescription);
		this.attackDmg = attackDmg;
		this.components = components != null ? new ArrayList<>(Arrays.asList(components)) : new ArrayList<>();
	}
	
	public int getAttackDmg()
	{
		return this.attackDmg;
	}
	
	public void addComponent(String component) {
		if (this.components == null) {
			this.components = new ArrayList<>();
		}
		this.components.add(component);
	}
	
	public List<String> getComponents() {
		return this.components;
	}
}