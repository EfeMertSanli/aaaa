package edu.kit.kastel.monstergame.model.command.handlers;
import edu.kit.kastel.monstergame.model.command.CommandHandler;
import edu.kit.kastel.monstergame.model.util.FileParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Handles configuration commands.
 * @author uuifx
 */
public class ConfigurationHandler {
    private final CommandHandler commandHandler;

    /**
     * Creates a new ConfigurationHandler.
     *
     * @param commandHandler The main command handler
     */
    public ConfigurationHandler(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    /**
     * Handles the quit command.
     */
    public void quit() {
        commandHandler.setRunning(false);
        System.out.println("Exiting game...");
    }

    /**
     * Handles the load command.
     * @param args arguments that are passed after load command
     **/
    public void load(String[] args) {
        // Check that there is exactly one argument
        if (args.length != 1) {
            System.out.println("Error, load command requires exactly one file path argument");
            return;
        }

        String filePath = args[0];
        try {
            java.nio.file.Path path = Paths.get(filePath);
            java.util.List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                System.out.println(line);
            }
            FileParser.GameData newGameData = FileParser.parseFile(filePath);

            if (newGameData.getMonsters().isEmpty() && newGameData.getActions().isEmpty()) {
                System.out.println("Error, loading configuration: Invalid format or empty file");
                return;
            }

            commandHandler.setInCompetition(false);
            commandHandler.setCurrentMonster(null);
            commandHandler.setGameData(newGameData);

            System.out.println("\nLoaded " + newGameData.getActions().size() + " actions, "
                    + newGameData.getMonsters().size() + " monsters.");

        } catch (IOException e) {
            System.out.println("Error, loading configuration: " + e.getMessage());
        }
    }
}
