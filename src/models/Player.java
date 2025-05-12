package models;
import models.Armor;
import models.ArmorSlot;


import java.util.EnumMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import models.Quest;
import models.QuestManager;
import models.QuestDefinition;
import java.util.List;
import java.util.ArrayList;
import models.Quest;
import models.QuestManager;
import models.QuestDefinition;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import models.DerbyDatabase;

public class Player extends Character {
	private int id;
    private int skillPoints;
    private double damageMulti;
    private int attackBoost;
    private int defenseBoost;
    private String playerType;
    // --- new field: track equipped armor per slot ---
   private final Map<ArmorSlot, Armor> equippedArmor = new EnumMap<>(ArmorSlot.class);
   private Companion hasCompanion;
   // Quest tracking
   private final List<Quest> activeQuests   = new ArrayList<>();
   private final List<Quest> completedQuests= new ArrayList<>();
   // private Room location;
   public Player(String name, int hp, int skillPoints, Inventory inventory, String longdescription, String shortdescription,double damageMulti,int attackBoost,int defenseBoost) {
       super(name, hp, inventory, longdescription, shortdescription);
       this.skillPoints = skillPoints;
       this.damageMulti = damageMulti;
       this.attackBoost = attackBoost;
       this.defenseBoost = defenseBoost;
       this.hasCompanion = null;
       this.playerType = "NORMAL";
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

    // --- NEW armor-related methods ---

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

    /** Sum of all armor-provided defense boosts */
    /** Sum of all armor-provided defense boosts (no streams) */
    public int getArmorDefenseTotal() {
        int total = 0;
        for (Armor a : equippedArmor.values()) {
            if (a != null) {
                total += a.getdefenseBoost();
            }
        }
        return total;
    }


    /** Sum of all armor-provided attack boosts */
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

    public String getPlayerType() {
        return playerType;
    }
    
    public void setPlayerType(String playerType) {
        this.playerType = playerType;
    }
    
    // New fields for persistence
    private int userId;
    private int currentRoomNum = -1; // Default to -1 to indicate not set
    private String runningMessage;
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getCurrentRoomNum() {
        return currentRoomNum;
    }
    
    public void setCurrentRoomNum(int currentRoomNum) {
        this.currentRoomNum = currentRoomNum;
    }
    
    public String getRunningMessage() {
        return runningMessage;
    }
    
    public void setRunningMessage(String runningMessage) {
        this.runningMessage = runningMessage;
    }
    /** Accept a quest by ID, via the QuestManager */
    public void acceptQuest(int questId, QuestManager qm) {
        QuestDefinition def = qm.get(questId);
        if (def != null) {
            activeQuests.add(new Quest(def));
        }
    }

    /** Progress active quests on an event; complete and grant skill points */
    public void onEvent(String type, String name, int amount) {
        List<Quest> copy = new ArrayList<>(activeQuests);
        for (Quest q : copy) {
            q.advance(type, name, amount);
            // Persist quest progress and status
            try (Connection conn = DerbyDatabase.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "UPDATE player_quests SET progress = ?, status = ? WHERE player_id = ? AND quest_id = ?"
                 )) {
                ps.setInt(1, q.getProgress());
                ps.setString(2, q.getStatus().name());
                ps.setInt(3, this.id);
                ps.setInt(4, q.getDef().getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update quest progress", e);
            }
            if (q.isComplete()) {
                activeQuests.remove(q);
                completedQuests.add(q);
                // Award skill points upon quest completion
                this.skillPoints += q.getDef().getRewardSkillPoints();
            }
        }
    }

    /** Returns all quests the player is currently undertaking */
    public List<Quest> getActiveQuests() {
        return activeQuests;
    }

    /** Returns all quests the player has completed */
    public List<Quest> getCompletedQuests() {
        return completedQuests;
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
