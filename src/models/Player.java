package models;

public class Player extends Character {
    private int skillPoints;
    
    public Player(String name, int hp, int skillPoints, Inventory inventory, String longdescription, String shortdescription) {
        super(name, hp, inventory, longdescription, shortdescription);
        this.skillPoints = skillPoints;
    }
    
    public void move(String direction) {
        System.out.println(name + " moves " + direction + " to a new location.");
        // Implement movement logic based on Room class
    }
    
    public void useSkillPoints() {
        if (skillPoints > 0) {
            skillPoints--;
            System.out.println(name + " used a skill point. Remaining: " + skillPoints);
        } else {
            System.out.println(name + " has no skill points left.");
        }
    }
    
    // New method to check if the player has a specific key in their inventory.
    public boolean hasKey(String keyName) {
        // Assume getInventory() is available from the Character class
        for (int i = 0; i < getInventory().getSize(); i++) {
            if (getInventory().getItemName(i).equalsIgnoreCase(keyName)) {
                return true;
            }
        }
        return false;
    }
}
