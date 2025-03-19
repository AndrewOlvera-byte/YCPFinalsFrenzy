package models;

public class Weapon extends Item
{
	private int attackDmg;
	
	public Weapon(int value, int weight, String name, String[] components, int attackDmg)
	{
		super(value, weight, name, components);
		this.attackDmg = attackDmg;
	}
	
	public int getAttackDmg()
	{
		return this.attackDmg;
	}
}