package models;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import GameEngine.GameEngine;

public class GameInputHandler {
    private GameEngine gameEngine;

    private static final Set<String> VALID_VERBS = new HashSet<>(Arrays.asList(
        "pickup", "drop", "use", "get", "grab", "take", "examine", "go", "talk", "attack", "swing", "slash", "strike", "hit"
    ));

    private static final Set<String> PREPOSITIONS = new HashSet<>(Arrays.asList(
        "on", "with", "at", "in", "to"
    ));

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
                // Just call getGo; it already appends to runningMessage.
                gameEngine.getGo(noun);
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
