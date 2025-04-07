package models;

<<<<<<< Updated upstream


import GameEngine.GameEngine;

=======
import java.util.*;

import GameEngine.GameEngine;
import models.*;
>>>>>>> Stashed changes

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
        
        Room currentRoom = engine.getRooms().get(engine.getCurrentRoomNum());
        double damageMulti = engine.getPlayer().getdamageMulti();
        double attackDmg = engine.getPlayer().getAttackDmg(itemNum) * damageMulti;
        int charHealth = currentRoom.getCharacterHealth(characterNum);
        double newHealth = charHealth - attackDmg;
        
        boolean aggressive = currentRoom.isCharAgressive(characterNum);
        
        if(newHealth <= 0) {
            String temp = currentRoom.getCharacterName(characterNum);
            currentRoom.handleCharacterDeath(characterNum);
            return "\n<b>" + temp + " has been slain and dropped its inventory!</b>";
        }
        else {
            currentRoom.setCharacterHealth(characterNum, newHealth);
            charAttackPlayer(0, characterNum, aggressive);

            // Format attack damage string
            String dmgString = (attackDmg % 1 == 0) ? String.format("%.0f", attackDmg) : String.format("%.1f", attackDmg);

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

        // Don't attack if not aggressive
        if (!aggressive) return;

        // If character has no item at index, skip attack
        if (character.getInventorySize() <= itemNum) {
            engine.appendMessage("\n" + character.getName() + " has no weapon to attack with.");
            return;
        }

        int attackDmg = currentRoom.getCharacterAttackDmg(characterNum, itemNum);
        int playerHealth = engine.getPlayer().getHp();

        engine.getPlayer().setHp(playerHealth - attackDmg);
    }
}