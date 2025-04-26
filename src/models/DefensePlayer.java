package models;

public class DefensePlayer extends Player {
    private double defenseBoost;

    public DefensePlayer(String name,
                         int hp,
                         int skillPoints,
                         Inventory inventory,
                         String longdescription,
                         String shortdescription,
                         double damageMulti,
                         double defenseBoost) {
        // Grant defense players an extra 50 HP at creation
        super(name, hp + 50, skillPoints, inventory, longdescription, shortdescription, damageMulti);
        this.defenseBoost = defenseBoost;
    }

    public double getDefenseBoost() {
        return defenseBoost;
    }

    public void setDefenseBoost(double defenseBoost) {
        this.defenseBoost = defenseBoost;
    }
}
