package models;

public class Utility extends Item {
   private int healing;
   private int damageMulti;
   public Utility(int value, int weight, String name, String[] components, String longdescription, String shortdescription,int healing,int damageMulti) {
       super(value, weight, name, components, longdescription,shortdescription);
       this.healing = healing;
       this.damageMulti = damageMulti;
       
   }
   public int getHealing(int healing){
	   return this.healing;
   }
   public int getDamageMulti(int damageMulti){
	   return this.damageMulti;
   }
   }

   