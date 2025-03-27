package models;

public class Weapon extends Item
{
	private int attackDmg;
	
	public Weapon(int value, int weight, String name, String[] components, int attackDmg, String longdescription, String shortdescription)
	{
		super(value, weight, name, components, longdescription, shortdescription);
		this.attackDmg = attackDmg;
	}
	
	public int getAttackDmg()
	{
		return this.attackDmg;
	}
}