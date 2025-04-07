package models;

import java.util.*;

import GameEngine.GameEngine;


public class PlayerManager {
    private GameEngine engine;
    
    public PlayerManager(GameEngine engine) {
        this.engine = engine;
    }
    
    // Loading of player data
    public void loadPlayer() {
        String[] components = {};
        Weapon weaponPlayer = new Weapon(20, 30, "Dagger", components, 40, "<b>A trusty dagger that fits in your back pocket.</b>", "<b>A dagger.</b>");
        ArrayList<Item> itemContainer = new ArrayList<>();
        itemContainer.add(weaponPlayer);
        String playerName = "Cooper";
        Inventory inventory = new Inventory(itemContainer, 30);
        Player newPlayer = new Player(playerName, 200, 0, inventory, "<b>It's you! You know, you!</b>", "<b>You.</b>",1);
        engine.setPlayer(newPlayer);
    }
}