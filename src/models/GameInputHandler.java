package models;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import GameEngine.GameEngine;
import models.GameStateManager;    // ← NEW: your state manager

public class GameInputHandler {
    private GameEngine gameEngine;

    private static final Set<String> VALID_VERBS = new HashSet<>(Arrays.asList(
        "pickup", "drop", "use", "get", "grab", "take",
        "examine", "go", "move", "walk",
        "talk", "attack", "swing", "slash", "strike", "hit",
        "look", "help", "shuttle", "drive", "respond",
        "apply", "drink", "reset", "initialize", "forward", "backward", "left", "right",
        "North", "N", "north", "n", "South", "south", "S", "s", "East", "east", "E", "e",
        "West", "W", "west", "w", "equip","unequip","choose", "shoo", "disassemble",
        "combine", "give", "takec", "increase", "funky", "daddy"
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
        return processInput(input, 0); // Default to first player
    }

    public boolean processInput(String input, int player_id) {
        String[] parsedInput = parseInput(input);
        String verb  = parsedInput[0];
        String noun  = parsedInput[1];
        String noun2 = parsedInput[3];

        Player playerCurrentUse = gameEngine.getPlayerById(player_id);
        
        if (playerCurrentUse == null) {
            gameEngine.appendMessage("\n<b>Invalid player ID.</b>");
            return false;
        }

        if (verb == null || verb.isEmpty() || !VALID_VERBS.contains(verb)) {
            gameEngine.appendMessage("\n<b>Invalid command.</b>");
            return false;
        }

        gameEngine.appendMessage("\n" + input);

        boolean commandExecuted = true;
        
        switch (verb.toLowerCase()) {
            case "take":
            case "get":
            case "grab":
            case "pickup":
                if (noun == null || noun.trim().isEmpty()) {
                    gameEngine.appendMessage(
                        "<b>\nYou must specify an item to grab. Try: grab [item name]</b>"
                    );
                } else {
                    gameEngine.appendMessage(
                        gameEngine.pickupItem(gameEngine.RoomItemNameToID(noun))
                    );
                }
                break;
                
            case "takec":
            	if (noun == null || noun.trim().isEmpty()) {
                    gameEngine.appendMessage(
                        "<b>\nYou must specify an item to grab. Try: grab [item name]</b>"
                    );
                } else {
                    gameEngine.appendMessage(
                        gameEngine.pickupItemFromCompanion(gameEngine.CompanionItemNameToID(noun))
                    );
                }
            	break;
            	
            case "drop":
                gameEngine.appendMessage(
                    gameEngine.dropItem(gameEngine.CharItemNameToID(noun))
                );
                break;
                
            case "give":
                gameEngine.appendMessage(
                    gameEngine.giveItemToCompanion(gameEngine.CharItemNameToID(noun))
                );
                break;

            case "attack":
            case "swing":
            case "slash":
            case "hit":
            case "strike":
                if (noun2 == null || noun2.trim().isEmpty()) {
                    gameEngine.appendMessage(
                        "\n<b>Invalid attack. You must specify a weapon. "
                      + "Try: hit [enemy] with [weapon]</b>"
                    );
                } else {
                    gameEngine.appendMessage(
                        gameEngine.playerAttackChar(
                            gameEngine.CharItemNameToID(noun2),
                            gameEngine.CharNameToID(noun)
                        )
                    );
                }
                break;

            case "go":
            case "move":
            case "walk":
                if (noun == null || noun.trim().isEmpty()) {
                    gameEngine.appendMessage(
                        "\n<b>Invalid movement. You must specify a direction. "
                      + "Try: go [north]</b>"
                    );
                } else {
                    gameEngine.appendMessage(gameEngine.getGo(noun));
                }
                break;
            case "North":
            case "north":
            case "N":
            case "n":
            	gameEngine.appendMessage(gameEngine.getGo("North"));
            	break;
            	
            case "South":
            case "south":
            case "S":
            case "s":
            	gameEngine.appendMessage(gameEngine.getGo("South"));
            	break;
            	
            case "East":
            case "east":
            case "E":
            case "e":
            	gameEngine.appendMessage(gameEngine.getGo("East"));
            	break;
            	
            case "West":
            case "west":
            case "W":
            case "w":
            	gameEngine.appendMessage(gameEngine.getGo("West"));
            	break;
             
            case "forward":
                    gameEngine.appendMessage(gameEngine.getGo("North"));
                break;
                
            case "backward":
                    gameEngine.appendMessage(gameEngine.getGo("South"));
                break;
                
            case "left":
                    gameEngine.appendMessage(gameEngine.getGo("West"));
                break;
                
            case "right":
                    gameEngine.appendMessage(gameEngine.getGo("East"));
                break;

            case "examine":
            case "look":
                if (noun == null || noun.trim().isEmpty()) {
                    gameEngine.appendMessage(
                        "\n<b>Invalid look. You must specify something to look at. "
                      + "Try: look [item] or [room] or [NPC]</b>"
                    );
                } else {
                    gameEngine.appendMessage(gameEngine.getExamine(noun));
                }
                break;

            case "help":
                gameEngine.appendMessage(gameEngine.getHelp());
                break;
            case "choose":
            	if (noun == null || noun.trim().isEmpty()) {
                    gameEngine.appendMessage(
                        "<b>\nYou must specify a Companion to choose. Try: choose [Companion name]</b>"
                    );
                } else {
                    gameEngine.appendMessage(
                        gameEngine.chooseCompanion(gameEngine.RoomCompanionNameToID(noun))
                    );
                }
                break;
            case "shoo":
            	gameEngine.appendMessage(
            			gameEngine.shooCompanion(gameEngine.playerCompanionNameToID(noun))
            			);
            	break;
                

            case "shuttle":
            case "drive":
                gameEngine.appendMessage(gameEngine.getOnShuttle());
                break;

            case "apply":
            case "drink":
                if (noun == null || noun.trim().isEmpty()) {
                    gameEngine.appendMessage(
                        "\n<b>Invalid apply. You must specify something to apply or drink. "
                      + "Try: apply [potion]</b>"
                    );
                } else {
                    gameEngine.appendMessage(
                        gameEngine.usePotion(gameEngine.CharItemNameToID(noun))
                    );
                }
                break;

            case "talk":
                this.conversationInitiated = true;
                this.currentCharName = noun;
                int charID = gameEngine.CharNameToID(currentCharName);
                gameEngine.appendMessage("\n" + gameEngine.talkToNPC(charID));

                String[] opts = gameEngine.getResponseOptions(charID);
                if (opts == null || opts.length < 2) {
                    conversationInitiated = false;
                    gameEngine.appendMessage("\n<b>(No more responses available.)</b>");
                } else {
                    firstOption  = opts[1];
                    secondOption = opts[0];
                    gameEngine.appendMessage(
                        "\n<b>Response Options:\n" + firstOption + "\n" + secondOption + "</b>"
                    );
                }
                break;

            case "respond":
                if (conversationInitiated) {
                    handleResponse(noun);
                } else {
                    gameEngine.appendMessage("<b>\nNo active conversation.</b>");
                }
                break;
                
            case "reset":
               		gameEngine.appendMessage(gameEngine.reset());
               	break;
            case "initialize":
               	gameEngine.initialize();
               	break;
            case "equip":
            	if (noun == null || noun.trim().isEmpty()) {
                    gameEngine.appendMessage(
                        "\n<b>Invalid equip. You must specify something to equip. "
                      + "Try: apply [armor]</b>"
                    );
                } else {
                    gameEngine.appendMessage(
                        gameEngine.equipArmor(gameEngine.CharItemNameToID(noun))
                    );
                }
                break;
            case "unequip":
            	if (noun == null || noun.trim().isEmpty()) {
                    gameEngine.appendMessage(
                        "\n<b>Invalid equip. You must specify something to remove. "
                      + "Try: apply [armor]</b>"
                    );
                } else {
                    gameEngine.appendMessage(
                        gameEngine.unequipArmor(noun)
                    );
                }
                break;
            case "disassemble":
                if (noun == null || noun.trim().isEmpty()) {
                    gameEngine.appendMessage(
                        "\n<b>You must specify an item to disassemble. Try: disassemble [item]</b>"
                    );
                } else {
                    gameEngine.appendMessage(
                        gameEngine.disassembleItem(noun)
                    );
                }
                break;
            case "combine":
                if (noun == null || noun2 == null || noun.trim().isEmpty() || noun2.trim().isEmpty()) {
                    gameEngine.appendMessage(
                        "\n<b>You must specify two components to combine. "
                      + "Try: combine [componentA] with [componentB]</b>"
                    );
                } else {
                    gameEngine.appendMessage(
                        gameEngine.combineItems(noun, noun2)
                    );
                }
                break;

            case "funky":
                if (noun == null || noun.trim().isEmpty()) {
                    gameEngine.appendMessage(
                        "\n<b>Invalid funky command. You must specify a room ID. "
                      + "Try: funky [roomID]</b>"
                    );
                } else {
                    try {
                        int targetId = Integer.parseInt(noun);
                        Integer idx = gameEngine.getRoomIndex(targetId);
                        if (idx != null) {
                            gameEngine.setCurrentRoomNum(idx);
                            gameEngine.appendMessage(
                                "\n<b>You have been teleported to room " + targetId + "!</b>"
                            );
                        } else {
                            gameEngine.appendMessage(
                                "\n<b>Invalid room ID. No such room: " + targetId + ".</b>"
                            );
                        }
                    } catch (NumberFormatException e) {
                        gameEngine.appendMessage(
                            "\n<b>Invalid room ID. Please enter a valid number.</b>"
                        );
                    }
                }
                break;

            case "increase":
                if (noun == null || noun.trim().isEmpty()) {
                    gameEngine.appendMessage(
                        "\n<b>Invalid increase. You must specify what to increase. "
                      + "Try: increase [attack|defense]</b>"
                    );
                } else if (noun.toLowerCase().equals("attack")) {
                    gameEngine.appendMessage(
                        gameEngine.increaseAttack()
                    );
                } else if (noun.toLowerCase().equals("defense")) {
                    gameEngine.appendMessage(
                        gameEngine.increaseDefense()
                    );
                } else {
                    gameEngine.appendMessage(
                        "\n<b>Invalid increase. You can only increase attack or defense.</b>"
                    );
                }
                break;

            case "daddy": // Hidden command for giving skill points
                try {
                    int amount = Integer.parseInt(noun);
                    Player player = gameEngine.getPlayer();
                    if (player != null) {
                        player.setSkillPoints(player.getSkillPoints() + amount);
                        gameEngine.appendMessage("\n<b>Added " + amount + " skill points.</b>");
                    }
                } catch (NumberFormatException e) {
                    // Silently fail for invalid numbers
                }
                break;

            default:
                gameEngine.appendMessage("\n<b>Command not implemented.</b>");
                commandExecuted = false;
        }
        
        // Save player state after every command
        if (commandExecuted) {
            // Update player's room number to current room before saving
            playerCurrentUse.setCurrentRoomNum(gameEngine.getCurrentRoomNum());
            
            // Update player's running message to current running message
            playerCurrentUse.setRunningMessage(gameEngine.getRunningMessage());
            
            // Save state to database
            gameEngine.saveAllPlayersState();
        }
        
        return true;
    }

    private void handleResponse(String noun) {
        int charID = gameEngine.CharNameToID(currentCharName);
        String chosen = noun.equals("1") ? firstOption : noun.equals("2") ? secondOption : null;

        if (chosen == null || chosen.isEmpty()) {
            gameEngine.appendMessage("<b>\nInvalid response option.</b>");
            return;
        }

        gameEngine.appendMessage("\n" + gameEngine.interactWithNPC(chosen, charID));

        if (!gameEngine.reachedFinalNode()) {
            String[] nextOpts = gameEngine.getResponseOptions(charID);
            if (nextOpts == null || nextOpts.length < 2) {
                conversationInitiated = false;
                gameEngine.appendMessage("\n<b>(Conversation ended.)</b>");
            } else {
                firstOption  = nextOpts[1];
                secondOption = nextOpts[0];
                gameEngine.appendMessage(
                    "<b>\nResponse Options:\n" + firstOption + "\n" + secondOption + "</b>"
                );
            }
        } else {
            conversationInitiated = false;
            gameEngine.appendMessage("<b>\n(Conversation complete.)</b>");
        }
    }

    public static String[] parseInput(String command) {
        command = command.toLowerCase();
        String[] words = command.split("\\s+");
        if (words.length == 0) return new String[]{null, null, null, null};

        String verb        = words[0];
        String noun        = null;
        String noun2       = null;

        for (int i = 1; i < words.length; i++) {
            if (PREPOSITIONS.contains(words[i])) {
                noun2 = (i + 1 < words.length)
                      ? String.join(" ", Arrays.copyOfRange(words, i + 1, words.length))
                      : null;
                break;
            }
            noun = (noun == null) ? words[i] : noun + " " + words[i];
        }

        return new String[]{verb, noun, null, noun2};
    }
}
