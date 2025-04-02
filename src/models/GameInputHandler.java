package models;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import GameEngine.GameEngine;

public class GameInputHandler {
    private GameEngine gameEngine;

    private static final Set<String> VALID_VERBS = new HashSet<>(Arrays.asList(
        "pickup", "drop", "use", "get", "grab", "take", "examine", "go", "move", "walk", "talk", "attack", "swing", "slash", "strike", "hit", "look", "help"
        , "shuttle", "drive", "respond","apply","drink"
    ));

    private static final Set<String> PREPOSITIONS = new HashSet<>(Arrays.asList(
        "on", "with", "at", "in", "to"
    ));
    
    private boolean conversationInitiated = false;
    private String firstOption = "";
    private String secondOption = "";
    private String currentCharName = "";

    public GameInputHandler(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public boolean processInput(String input) {
        String[] parsedInput = parseInput(input);
        String verb = parsedInput[0];
        String noun = parsedInput[1];
        String noun2 = parsedInput[3];

        if (verb == null || verb.isEmpty() || !VALID_VERBS.contains(verb)) {
            gameEngine.appendMessage("\nInvalid command.");
            return false;
        }
        
        gameEngine.appendMessage("\n" + input);

        switch (verb.toLowerCase()) {
            case "take":
            case "get":
            case "grab":
            case "pickup":
                gameEngine.appendMessage(gameEngine.pickupItem(gameEngine.RoomItemNameToID(noun)));
                break;
            case "drop":
                gameEngine.appendMessage(gameEngine.dropItem(gameEngine.CharItemNameToID(noun)));
                break;
            case "attack":
            case "swing":
            case "slash":
            case "hit":
            case "strike":
                gameEngine.appendMessage(gameEngine.playerAttackChar(gameEngine.CharItemNameToID(noun2), gameEngine.CharNameToID(noun)));
                break;
            case "go":
            case "move":
            case "walk":
            	gameEngine.getGo(noun);
                break;
            case "examine":
            case "look":
            	gameEngine.appendMessage(gameEngine.getExamine(noun));
            	break;
            case "help":
            	gameEngine.appendMessage(gameEngine.getHelp());
            	break;
            case "shuttle":
            case "drive":
            	gameEngine.getOnShuttle();
            	break;
            case "apply":
            case "drink":
            	gameEngine.appendMessage(gameEngine.usePotion(gameEngine.CharItemNameToID(noun)));
            	
            	
            	
            break;
            case "talk":
            	this.conversationInitiated = true;
            	this.currentCharName = noun;
            	gameEngine.appendMessage("\n"+gameEngine.talkToNPC(gameEngine.CharNameToID(currentCharName)));
            	String[] stringArr = gameEngine.getResponseOptions(gameEngine.CharNameToID(currentCharName));
            	firstOption = stringArr[1];
            	secondOption = stringArr[0];
            	gameEngine.appendMessage("\nResponse Options:\n" + firstOption + "\n" + secondOption);
            	break;
            case "respond":
            	if (conversationInitiated) {
	            	if (noun.equals("1"))
	            	{
	            		gameEngine.appendMessage("\n" + gameEngine.interactWithNPC(firstOption, gameEngine.CharNameToID(currentCharName)));
	            		
	            		if (!gameEngine.reachedFinalNode()) 
	            		{
	            		String[] stringArrResp = gameEngine.getResponseOptions(gameEngine.CharNameToID(currentCharName));
	                	firstOption = stringArrResp[1];
	                	secondOption = stringArrResp[0];
	                	gameEngine.appendMessage("\nResponse Options:\n" + firstOption + "\n" + secondOption);
	            		}
	            	}
	            	if (noun.equals("2"))
	            	{
	            		gameEngine.appendMessage("\n" + gameEngine.interactWithNPC(secondOption, gameEngine.CharNameToID(currentCharName)));
	            		
	            		if (!gameEngine.reachedFinalNode()) 
	            		{
	            		String[] stringArrResp = gameEngine.getResponseOptions(gameEngine.CharNameToID(currentCharName));
	                	firstOption = stringArrResp[1];
	                	secondOption = stringArrResp[0];
	                	gameEngine.appendMessage("\nResponse Options:\n" + firstOption + "\n" + secondOption);
	            		}
	            		else
	            			conversationInitiated = false;
	            			currentCharName = "";
	            	}
            	}
            	break;
            default:
                gameEngine.appendMessage("\nCommand not implemented.");
        }
        return true;
    }

    public static String[] parseInput(String command) {
        command = command.toLowerCase();
        String[] words = command.split("\\s+");
        if (words.length == 0) return new String[]{null, null, null, null};

        String verb = words[0];
        String noun = null;
        String preposition = null;
        String noun2 = null;
        
        for (int i = 1; i < words.length; i++) {
            if (PREPOSITIONS.contains(words[i])) {
                preposition = words[i];
                noun2 = (i + 1 < words.length) ? String.join(" ", Arrays.copyOfRange(words, i + 1, words.length)) : null;
                break;
            }
            noun = (noun == null) ? words[i] : noun + " " + words[i];
        }

        return new String[]{verb, noun, preposition, noun2};
    }
}
