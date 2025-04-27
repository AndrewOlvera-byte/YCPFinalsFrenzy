package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utility extends Item {
   private int healing;
   private double damageMulti;
   private List<String> components;
   
   public Utility(int value, int weight, String name, String[] components, String longdescription, String shortdescription, int healing, double damageMulti) {
       super(value, weight, name, components, longdescription, shortdescription);
       this.healing = healing;
       this.damageMulti = damageMulti;
       this.components = components != null ? new ArrayList<>(Arrays.asList(components)) : new ArrayList<>();
   }
   
   public int getHealing() {
       return this.healing;
   }
   
   public double getDamageMulti() {
       return this.damageMulti;
   }
   
   public void addComponent(String component) {
       if (this.components == null) {
           this.components = new ArrayList<>();
       }
       this.components.add(component);
   }
   
   public List<String> getComponents() {
       return this.components;
   }
}

   