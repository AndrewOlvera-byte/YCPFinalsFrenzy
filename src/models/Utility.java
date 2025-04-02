package models;

public class Utility extends Item {
   private int healing;
   private double damageMulti;
   public Utility(int value, int weight, String name, String[] components, String longdescription, String shortdescription,int healing,double damageMulti) {
       super(value, weight, name, components, longdescription,shortdescription);
       this.healing = healing;
       this.damageMulti = damageMulti;
       
   }
   public int getHealing(){
	   return this.healing;
   }
   public double getDamageMulti(){
	   return this.damageMulti;
   }
   }

   