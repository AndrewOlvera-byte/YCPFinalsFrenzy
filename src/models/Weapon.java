package models;

import orm.annotations.*;
import javax.persistence.GeneratedValue;


@Entity
@Table(name = "weapons")
public class Weapon extends Item {

    @Column(name = "attack_dmg")
    private int attackDmg;    

    public Weapon() {
        super();
    }

    public Weapon(int value, int weight, String name, String[] components, int attackDmg, String longdescription, String shortdescription) {
        super(value, weight, name, components, longdescription, shortdescription);
        this.attackDmg = attackDmg;
    }

    public int getAttackDmg() {
        return this.attackDmg;
    }

    public void setAttackDmg(int attackDmg) {
        this.attackDmg = attackDmg;
    }

} 
