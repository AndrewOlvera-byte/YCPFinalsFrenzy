package models;

import orm.annotations.*;

@Entity             
@Table(name = "items") // ADDED: Use the same table as the base Character class.
public class Weapon extends Item
{
	@Column(name = "attack_dmg")
	private int attackDmg;
	
	public Weapon(int value, int weight, String name, String[] components, int attackDmg, String longdescription, String shortdescription)
	{
		super(value, weight, name, components, longdescription, shortdescription);
		this.attackDmg = attackDmg;
	}
	
	public Weapon() {
		 super();
	}
	
	public int getAttackDmg()
	{
		return this.attackDmg;
	}
}