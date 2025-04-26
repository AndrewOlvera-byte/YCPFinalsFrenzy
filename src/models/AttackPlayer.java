package models;

public class AttackPlayer extends Player {
    private double attackBoost;

    public AttackPlayer(String name,
                        int hp,
                        int skillPoints,
                        Inventory inventory,
                        String longdescription,
                        String shortdescription,
                        double damageMulti,
                        double attackBoost) {
        super(name, hp, skillPoints, inventory, longdescription, shortdescription, damageMulti);
        this.attackBoost = attackBoost;
    }

    public double getAttackBoost() {
        return attackBoost;
    }

    public void setAttackBoost(double attackBoost) {
        this.attackBoost = attackBoost;
    }
}
