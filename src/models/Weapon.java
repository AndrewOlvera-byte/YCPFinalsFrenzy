package models;

public class Weapon extends Item
{
	private int attackDmg;
	
	public Weapon(int value, int weight, String name, String[] components, int attackDmg, String description)
	{
		super(value, weight, name, components, description);
		this.attackDmg = attackDmg;
	}
	
	public int getAttackDmg()
	{
		return this.attackDmg;
	}
}