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
            gameEngine.appendMessage("\n<b>Invalid command.</b>");
            return false;
        }
        
        gameEngine.appendMessage("\n" + input);

        switch (verb.toLowerCase()) {
            case "take":
            case "get":
            case "grab":
            case "pickup":
            	if (noun == null || noun.trim().isEmpty()) {
                    gameEngine.appendMessage("<b>\nYou must specify an item to grab. Try: grab [item name]</b>");
                } else {
                    gameEngine.appendMessage(gameEngine.pickupItem(gameEngine.RoomItemNameToID(noun)));
                }
                break;
            case "drop":
                gameEngine.appendMessage(gameEngine.dropItem(gameEngine.CharItemNameToID(noun)));
                break;
            case "attack":
            case "swing":
            case "slash":
            case "hit":
            case "strike":
            	if (noun2 == null || noun2.trim().isEmpty()) {
                    gameEngine.appendMessage("\n<b>Invalid attack. You must specify a weapon. Try: hit [enemy] with [weapon]</b>");
                } else {
                    gameEngine.appendMessage(gameEngine.playerAttackChar(gameEngine.CharItemNameToID(noun2), gameEngine.CharNameToID(noun)));
                }
                break;
            case "go":
            case "move":
            case "walk":
            	if (noun == null || noun.trim().isEmpty()) {
            	    gameEngine.appendMessage("\n<b>Invalid movement. You must specify a direction. Try: go [north]</b>");
            	} else {
            	gameEngine.getGo(noun);
            	}
                break;
            case "examine":
            case "look":
            	if (noun == null || noun.trim().isEmpty()) {
            	    gameEngine.appendMessage("\n<b>Invalid look. You must specify something to look at. Try: look [item] or [room] or [NPC]</b>");
            	} else {
            	gameEngine.appendMessage(gameEngine.getExamine(noun));
            	}
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
            	if (noun == null || noun.trim().isEmpty()) {
            	    gameEngine.appendMessage("\n<b>Invalid apply. You must specify something apply or drink. Try: apply [potion]</b>");
            	} else {
            	gameEngine.appendMessage(gameEngine.usePotion(gameEngine.CharItemNameToID(noun)));
            	}
            break;
            case "talk":
            	this.conversationInitiated = true;
                this.currentCharName = noun;

                int charID = gameEngine.CharNameToID(currentCharName);
                gameEngine.appendMessage("\n" + gameEngine.talkToNPC(charID));

                String[] stringArr = gameEngine.getResponseOptions(charID);

                if (stringArr == null || stringArr.length < 2) {
                    firstOption = "";
                    secondOption = "";
                    conversationInitiated = false;
                    gameEngine.appendMessage("\n<b>(No more responses available.)</b>");
                } else {
                    firstOption = stringArr[1];
                    secondOption = stringArr[0];
                    gameEngine.appendMessage("\n<b>Response Options:\n" + firstOption + "\n" + secondOption + "</b>");
                }
            	break;
            case "respond":
            	if (conversationInitiated) {
                    int charID1 = gameEngine.CharNameToID(currentCharName);

                    if (noun.equals("1") && firstOption != null && !firstOption.isEmpty()) {
                        gameEngine.appendMessage("\n" + gameEngine.interactWithNPC(firstOption, charID1));

                        if (!gameEngine.reachedFinalNode()) {
                            String[] stringArrResp = gameEngine.getResponseOptions(charID1);
                            if (stringArrResp == null || stringArrResp.length < 2) {
                                conversationInitiated = false;
                                currentCharName = "";
                                gameEngine.appendMessage("\n<b>(No further responses. Conversation ended.)</b>");
                            } else {
                                firstOption = stringArrResp[1];
                                secondOption = stringArrResp[0];
                                gameEngine.appendMessage("<b>\nResponse Options:\n" + firstOption + "\n" + secondOption + "</b>");
                            }
                        } else {
                            conversationInitiated = false;
                            currentCharName = "";
                            gameEngine.appendMessage("<b>\n(Conversation complete.)</b>");
                        }

                    } else if (noun.equals("2") && secondOption != null && !secondOption.isEmpty()) {
                        gameEngine.appendMessage("\n" + gameEngine.interactWithNPC(secondOption, charID1));

                        if (!gameEngine.reachedFinalNode()) {
                            String[] stringArrResp = gameEngine.getResponseOptions(charID1);
                            if (stringArrResp == null || stringArrResp.length < 2) {
                                conversationInitiated = false;
                                currentCharName = "";
                                gameEngine.appendMessage("<b>\n(No further responses. Conversation ended.)</b>");
                            } else {
                                firstOption = stringArrResp[1];
                                secondOption = stringArrResp[0];
                                gameEngine.appendMessage("<b>\nResponse Options:\n" + firstOption + "\n" + secondOption + "</b>");
                            }
                        } else {
                            conversationInitiated = false;
                            currentCharName = "";
                            gameEngine.appendMessage("<b>\n(Conversation complete.)</b>");
                        }

                    } else {
                        gameEngine.appendMessage("<b>\nInvalid response option.</b>");
                    }
                } else {
                    gameEngine.appendMessage("<b>\nNo active conversation.</b>");
                }
            	break;
            default:
                gameEngine.appendMessage("<b>\nCommand not implemented.</b>");
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
