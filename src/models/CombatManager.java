package models;



import GameEngine.GameEngine;


public class CombatManager {
    private GameEngine engine;
    
    public CombatManager(GameEngine engine) {
        this.engine = engine;
    }
    
    // Method for the player attacking a character - needs the characterNum to attack and item being used to attack
    public String playerAttackChar(int itemNum, int characterNum) {
        if (characterNum == -1 && itemNum == -1) {
            return "\n<b>Attack who with what?</b>";
        }
        else if (characterNum == -1) {
            return "\n<b>Attack who?</b>";
        }
        else if (itemNum == -1) {
            return "\n<b>Attack with what?</b>";
        }
        
        Room currentRoom   = engine.getRooms().get(engine.getCurrentRoomNum());
        double damageMulti = engine.getPlayer().getdamageMulti();
        double boost       = 0;
        int companionDamage = 0;
        boost = engine.getPlayer().getAttackBoost();
        if(engine.getPlayer().getPlayerCompanion() != null) {
        	companionDamage = engine.getPlayer().getPlayerCompanion().damage;
        }
        
        // calculate raw and total damage
        int    rawDmg    = engine.getPlayer().getAttackDmg(itemNum);
        double attackDmg = rawDmg * damageMulti + boost + companionDamage;
        
        int    charHealth = currentRoom.getCharacterHealth(characterNum);
        double newHealth  = charHealth - attackDmg;
        
        boolean aggressive = currentRoom.isCharAgressive(characterNum);
        
        if (newHealth <= 0) {
            String temp = currentRoom.getCharacterName(characterNum);
            currentRoom.handleCharacterDeath(characterNum);
            return "\n<b>" + temp + " has been slain and dropped its inventory!</b>";
        }
        else {
            currentRoom.setCharacterHealth(characterNum, newHealth);
            charAttackPlayer(0, characterNum, aggressive);

            // Format attack damage string
            String dmgString = (attackDmg % 1 == 0)
                ? String.format("%.0f", attackDmg)
                : String.format("%.1f", attackDmg);

            if (engine.getPlayer().getHp() <= 0) {
                return "\n<b>You Died!</b>";
            }
            else if (aggressive) {
                int counterDmg = currentRoom.getCharacterAttackDmg(characterNum, 0);
                return "<b>\n" + currentRoom.getCharacterName(characterNum) + " has taken " + dmgString + " damage.</b>" +
                       "<b>\n" + currentRoom.getCharacterName(characterNum) + " hit back for " + counterDmg + " damage.</b>";
            }
            else {
                return "\n" + "<b>" + currentRoom.getCharacterName(characterNum) + " has taken " + dmgString + " damage.</b>";
            }
        }
    }
    
    // Method for the character characterNum attacking the player with itemNum
    public void charAttackPlayer(int itemNum, int characterNum, boolean aggressive) {
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        Character character = currentRoom.getCharacter(characterNum);

        // 1) Only attack if the character is aggressive
        if (!aggressive) return;

        // 2) Ensure they have a weapon to hit you with
        if (character.getInventorySize() <= itemNum) {
            engine.appendMessage("\n" + character.getName() + " has no weapon to attack with.");
            return;
        }

        // 3) Base (raw) damage from the NPC
        int rawDmg = currentRoom.getCharacterAttackDmg(characterNum, itemNum);

        int defense = engine.getPlayer().getdefenseBoost();
        
        int armor = engine.getPlayer().getArmorDefenseTotal();

        double totalDmg = rawDmg - defense - armor;
        if (totalDmg < 0) totalDmg = 0;

        // 7) DEBUG: spit out all the numbers so you can confirm it’s working


        // 8) Finally, subtract from the player’s HP
        int playerHp = engine.getPlayer().getHp();
        engine.getPlayer().setHp(playerHp - (int)Math.round(totalDmg));
    }

}