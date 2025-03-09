package edu.kit.kastel.monstergame.model.command;

import edu.kit.kastel.monstergame.model.Monster;
import edu.kit.kastel.monstergame.model.combat.CombatSystem;
import edu.kit.kastel.monstergame.model.command.handlers.ActionHandler;
import edu.kit.kastel.monstergame.model.command.handlers.CompetitionHandler;
import edu.kit.kastel.monstergame.model.command.handlers.ConfigurationHandler;
import edu.kit.kastel.monstergame.model.command.handlers.MonsterDisplayHandler;
import edu.kit.kastel.monstergame.model.util.FileParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Main command handler for the monster game.
 * Manages the command loops.
 * @author uuifx
 */
public class CommandHandler {
    private final BufferedReader reader;
    private FileParser.GameData gameData;
    private CombatSystem combatSystem;
    private boolean isRunning;
    private boolean inDebugMode;
    private boolean inCompetition;
    private Monster currentMonster;

    private CompetitionHandler competitionHandler;
    private MonsterDisplayHandler displayHandler;
    private ConfigurationHandler configHandler;
    private ActionHandler actionHandler;

    /**
     * Creates a new CommandHandler.
     * @param initialGameData The initial game data
     * @param debugMode Whether to run in debug mode
     */
    public CommandHandler(FileParser.GameData initialGameData, boolean debugMode) {
        this.gameData = initialGameData;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.isRunning = true;
        this.inCompetition = false;
        this.inDebugMode = debugMode;

        this.competitionHandler = new CompetitionHandler(this);
        this.displayHandler = new MonsterDisplayHandler(this);
        this.configHandler = new ConfigurationHandler(this);
        this.actionHandler = new ActionHandler(this);
    }

    /**
     * Starts the main command loop.
     */
    public void start() {
        while (isRunning) {
            try {
                if (inCompetition && currentMonster != null) {
                    System.out.println("\n" + "What should " + currentMonster.getName() + " do?");
                }

                String input = reader.readLine().trim();
                processCommand(input);

            } catch (IOException e) {
                System.out.println("Error reading input: " + e.getMessage());
            }
        }
    }

    /**
     * Process commands from the user.
     * @param input The command input string
     */
    private void processCommand(String input) {
        if (input.isEmpty()) {
            return;
        }
        String[] parts = input.split("\\s+");
        String command = parts[0].toLowerCase();
        // Check if in debug mode special input handling
        if (inDebugMode && inCompetition && actionHandler.isWaitingForDebugInput()) {
            actionHandler.handleDebugInput(input);
            return;
        }
        switch (command) {
            case "quit":
                configHandler.quit();
                break;
            case "load":
                if (parts.length < 2) {
                    System.out.println("Error: load command requires a file path");
                } else {
                    configHandler.load(parts[1]);
                }
                break;
            case "competition":
                if (parts.length < 3) {
                    System.out.println("Error: competition command requires at least two monster names");
                } else {
                    String[] monsterNames = new String[parts.length - 1];
                    System.arraycopy(parts, 1, monsterNames, 0, parts.length - 1);
                    competitionHandler.handleCompetition(monsterNames);
                }
                break;
            case "show":
                handleShowCommand(parts);
                break;
            case "action":
                if (!inCompetition || currentMonster == null) {
                    System.out.println("Error: action command only available during competition in Phase I");
                    return;
                }

                if (parts.length < 2) {
                    System.out.println("Error: action command requires an action name");
                } else {
                    String actionName = parts[1];
                    String targetName = parts.length > 2 ? parts[2] : null;
                    actionHandler.selectAction(actionName, targetName);
                }
                break;
            case "pass":
                if (!inCompetition || currentMonster == null) {
                    System.out.println("Error: pass command only available during competition in Phase I");
                } else {
                    actionHandler.pass();
                }
                break;
            default:
                System.out.println("Error: unknown command: " + command);
                break;
        }
    }

    /**
     * Handles the show command.
     * @param parts The parts of the command
     */
    private void handleShowCommand(String[] parts) {
        if (parts.length == 1) {
            // Just "show" in competition mode shows monster status
            if (inCompetition) {
                displayHandler.showCompetitionMonsters();
            } else {
                System.out.println("Error: show command requires additional parameters"
                        + " (monsters, actions, stats)");
            }
        } else if (parts[1].equalsIgnoreCase("monsters")) {
            displayHandler.showAllMonsters();
        } else if (parts[1].equalsIgnoreCase("actions")) {
            if (inCompetition && currentMonster != null) {
                displayHandler.showActions();
            } else {
                System.out.println("Error: can only show actions during competition in Phase I");
            }
        } else if (parts[1].equalsIgnoreCase("stats")) {
            if (inCompetition && currentMonster != null) {
                displayHandler.showStats();
            } else {
                System.out.println("Error: can only show stats during competition in Phase I");
            }
        } else {
            System.out.println("Error: unknown show command: " + parts[1]);
        }
    }

    // Getters and setters for handlers to access shared state

    /**
     * Gets the game data.
     * @return The current game data
     */
    public FileParser.GameData getGameData() {
        return gameData;
    }

    /**
     * Sets the game data.
     * @param gameData The new game data
     */
    public void setGameData(FileParser.GameData gameData) {
        this.gameData = gameData;
    }

    /**
     * Checks if a competition is running.
     * @return True if in a competition, false otherwise
     */
    public boolean isInCompetition() {
        return inCompetition;
    }

    /**
     * Sets whether game in a competition.
     * @param inCompetition True to set in competition, false otherwise
     */
    public void setInCompetition(boolean inCompetition) {
        this.inCompetition = inCompetition;
    }

    /**
     * Gets the current monster.
     * @return The current monster
     */
    public Monster getCurrentMonster() {
        return currentMonster;
    }

    /**
     * Sets the current monster.
     * @param monster The new current monster
     */
    public void setCurrentMonster(Monster monster) {
        this.currentMonster = monster;
    }

    /**
     * Checks if the game is running.
     * @return True if the game is running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Sets whether the game is running.
     * @param running True to set running, false to stop
     */
    public void setRunning(boolean running) {
        isRunning = running;
    }

    /**
     * Checks if the game is in debug mode.
     * @return True if in debug mode, false otherwise
     */
    public boolean isInDebugMode() {
        return inDebugMode;
    }

    /**
     * Gets the combat system.
     * @return The current combat system
     */
    public CombatSystem getCombatSystem() {
        return combatSystem;
    }

    /**
     * Sets the combat system.
     * @param combatSystem The new combat system
     */
    public void setCombatSystem(CombatSystem combatSystem) {
        this.combatSystem = combatSystem;
    }

    /**
     * Gets the competition handler.
     * @return The competition handler
     */
    public CompetitionHandler getCompetitionHandler() {
        return competitionHandler;
    }
}