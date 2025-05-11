package models;
import models.Armor;
import models.ArmorSlot;


import java.util.EnumMap;
import java.util.Map;

public class Player extends Character {
	private int id;
    private int skillPoints;
    private double damageMulti;
    private int attackBoost;
    private int defenseBoost;
    // --- new field: track equipped armor per slot ---
   private final Map<ArmorSlot, Armor> equippedArmor = new EnumMap<>(ArmorSlot.class);
   private Companion hasCompanion;
   // private Room location;
   public Player(String name, int hp, int skillPoints, Inventory inventory, String longdescription, String shortdescription,double damageMulti,int attackBoost,int defenseBoost) {
       super(name, hp, inventory, longdescription, shortdescription);
       this.skillPoints = skillPoints;
       this.damageMulti = damageMulti;
       this.attackBoost = attackBoost;
       this.defenseBoost = defenseBoost;
       this.hasCompanion = null;
   }
   public void move(String direction) {
       System.out.println(name + " moves " + direction + " to a new location.");
       // Implement movement logic based on Room class
   }
   
   public double getdamageMulti() {
		return damageMulti;
	}
	
	public void setdamageMulti(double damageMulti) {
		this.damageMulti = damageMulti;
	}
    
    // New method to check if the player has a specific key in their inventory.
    public boolean hasKey(String keyName) {
        for (int i = 0; i < getInventory().getSize(); i++) {
            if (getInventory().getItemName(i).equalsIgnoreCase(keyName)) {
                return true;
            }
        }
        return false;
    }

    // your original boosts:
    public int getAttackBoost() {
        return attackBoost;
    }
    public void setAttackBoost(int attackBoost) {
        this.attackBoost = attackBoost;
    }
    public int getdefenseBoost() {
        return defenseBoost;
    }
    public void setdefenseBoost(int defenseBoost) {
        this.defenseBoost = defenseBoost;
    }

    // --- NEW armor‐related methods ---

    /** Equip an Armor item into the given slot */
    public void equip(ArmorSlot slot, Armor armor) {
        if (equippedArmor.get(slot) != null) {
            throw new IllegalStateException(
                slot + " slot is already filled with " + equippedArmor.get(slot).getName()
            );
        }
        equippedArmor.put(slot, armor);
    }
    /**
     * Returns the Armor currently equipped in the given slot,
     * or null if nothing is equipped there.
     */
    public Armor getEquippedArmor(ArmorSlot slot) {
        return equippedArmor.get(slot);
    }


    /** Unequip whatever is in that slot */
    public void unequip(ArmorSlot slot) {
        equippedArmor.remove(slot);
    }

    /** Peek at the Armor in that slot (or null if none) */
    public Armor getEquipped(ArmorSlot slot) {
        return equippedArmor.get(slot);
    }

    /** Sum of all armor‐provided defense boosts */
    /** Sum of all armor‐provided defense boosts (no streams) */
    public int getArmorDefenseTotal() {
        int total = 0;
        for (Armor a : equippedArmor.values()) {
            if (a != null) {
                total += a.getdefenseBoost();
            }
        }
        return total;
    }


    /** Sum of all armor‐provided attack boosts */
    public double getArmorAttackTotal() {
        return equippedArmor.values().stream()
            .filter(a -> a != null)
            .mapToDouble(Armor::getattackBoost)
            .sum();
    }

    /** Print out each slot and what's in it */
    public void printEquipment() {
        for (ArmorSlot slot : ArmorSlot.values()) {
            Armor a = equippedArmor.get(slot);
            if (a != null) {
                System.out.printf("%-9s: %s (DEF+%d, ATK+%.1f)%n",
                    slot,
                    a.getName(),
                    a.getdefenseBoost(),
                    a.getattackBoost()
                );
            } else {
                System.out.printf("%-9s: none%n", slot);
            }
        }
    }
    
    public Companion getPlayerCompanion() {
    	return this.hasCompanion;
    }
    
    
    public void dropCompanion() {
    	this.hasCompanion = null;
    }
    
    public void setPlayerCompanion(Companion companion) {
    	this.hasCompanion = companion;
    }
    public void removePlayerCompanion() {
    	this.hasCompanion = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public boolean useSkillPoints(int points) {
        if (skillPoints >= points) {
            skillPoints -= points;
            return true;
        }
        return false;
    }
}
