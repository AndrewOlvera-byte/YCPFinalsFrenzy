package models;


public class Armor extends Item {
   private int healing;
   private double attackBoost;
   private int defenseBoost;
   
   public Armor(int value, int weight, String name, String[] components, String longdescription, String shortdescription, int healing, double attackBoost, int defenseBoost) {
       super(value, weight, name, components, longdescription, shortdescription);
       this.healing = healing;
       this.attackBoost = attackBoost;
       this.defenseBoost = defenseBoost;
   }
   
   public int getHealing() {
       return this.healing;
   }
   
   public double getattackBoost() {
       return this.attackBoost;
   }
   public double getdefenseBoost() {
       return this.defenseBoost;
   }
}
   