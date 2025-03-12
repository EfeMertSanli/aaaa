package edu.kit.kastel.monstergame.model.util;

import edu.kit.kastel.monstergame.model.Action;
import edu.kit.kastel.monstergame.model.Monster;
import edu.kit.kastel.monstergame.model.enums.Element;
import edu.kit.kastel.monstergame.model.enums.StatType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for parsing monster data from configuration strings.
 * @author uuifx
 */
public final class MonsterParser {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private MonsterParser() {
    }

    /**
     * Parses a monster from a line in the configuration file.
     * @param line The line containing the monster definition
     * @param actionsMap The map of available actions
     * @return Parsed Monster
     */
    public static Monster parseMonster(String line, Map<String, Action> actionsMap) {
        String[] parts = line.split("\\s+");
        if (parts.length < 7) {
            System.err.println("Invalid monster format: " + line);
            return null;
        }
        String monsterName = parts[1];
        Element element;
        element = Element.valueOf(parts[2]);
        // Parse stats
        Map<StatType, Integer> stats = parseMonsterStats(parts);
        if (stats == null) {
            return null;
        }
        // Parse actions
        List<Action> monsterActions = parseMonsterActions(parts, actionsMap, monsterName);
        return new Monster(monsterName, element, stats, monsterActions);
    }

    /**
     * Parses monster stats from parts of a line.
     * @param parts The parts of the line
     * @return The parsed stats
     */
    public static Map<StatType, Integer> parseMonsterStats(String[] parts) {
        Map<StatType, Integer> stats = new EnumMap<>(StatType.class);
        stats.put(StatType.HP, Integer.parseInt(parts[3]));
        stats.put(StatType.ATK, Integer.parseInt(parts[4]));
        stats.put(StatType.DEF, Integer.parseInt(parts[5]));
        stats.put(StatType.SPD, Integer.parseInt(parts[6]));
        stats.put(StatType.PRC, 1);
        stats.put(StatType.AGL, 1);
        return stats;
    }

    /**
     * Parses the actions of a monster from parts of a line.
     * @param parts Parts of the line
     * @param actionsMap  The map of available actions
     * @param monsterName The name of the monster
     * @return A list of parsed actions
     */
    public static List<Action> parseMonsterActions(String[] parts, Map<String, Action> actionsMap,
                                                   String monsterName) {
        List<Action> monsterActions = new ArrayList<>();
        for (int i = 7; i < parts.length; i++) {
            String actionName = parts[i];
            Action action = actionsMap.get(actionName);
            if (action != null) {
                monsterActions.add(action);
            } else {
                System.err.println("Unknown action for monster " + monsterName + ": " + actionName);
            }
        }
        return monsterActions;
    }

    /**
     * Validates a monster's base stats.
     * @param stats The stats to validate
     * @return True if stats are valid
     */
    public static boolean validateStats(Map<StatType, Integer> stats) {
        if (!stats.containsKey(StatType.HP) || !stats.containsKey(StatType.ATK)
                || !stats.containsKey(StatType.DEF) || !stats.containsKey(StatType.SPD)) {
            return false;
        }

        return stats.get(StatType.HP) > 0 && stats.get(StatType.ATK) > 0
                && stats.get(StatType.DEF) > 0 && stats.get(StatType.SPD) > 0;
    }
}