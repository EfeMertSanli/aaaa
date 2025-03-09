package edu.kit.kastel.monstergame.model.util;

import edu.kit.kastel.monstergame.model.Action;
import edu.kit.kastel.monstergame.model.effect.Effect;
import edu.kit.kastel.monstergame.model.enums.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing actio data from config strings.
 * @author uuifx
 */
public final class ActionParser {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ActionParser() {
    }

    /**
     * Parses an action definition from the first line and reader.
     * @param firstLine The first line of the action definition
     * @param reader The reader to read subsequent lines
     * @return The parsed Action, or null if parsing failed
     * @throws IOException If an IO error occurs
     */
    public static Action parseAction(String firstLine, BufferedReader reader) throws IOException {
        String[] parts = firstLine.split("\\s+", 3);
        if (parts.length < 3) {
            System.err.println("Invalid action format: " + firstLine);
            return null;
        }

        String actionName = parts[1];
        Element element;
        element = Element.valueOf(parts[2]);
        List<Effect> effects = parseActionEffects(reader);
        if (effects.isEmpty()) {
            System.err.println("No valid effects found for action: " + actionName);
            return null;
        }
        return new Action(actionName, element, effects);
    }

    /**
     * Parses the effects of an action from the configuration file.
     * @param reader The reader to read effect lines
     * @return A list of parsed effects
     * @throws IOException If an IO error occurs
     */
    public static List<Effect> parseActionEffects(BufferedReader reader) throws IOException {
        List<Effect> effects = new ArrayList<>();
        String line;

        // Read effects until "end action"
        while ((line = reader.readLine()) != null) {
            line = line.trim();

            if (line.equals("end action")) {
                break;
            }
            Effect effect = EffectParser.parseEffect(line);
            if (effect != null) {
                effects.add(effect);
            }
        }
        return effects;
    }

    /**
     * Validates an actions properties.
     * @param action The action to validate
     * @return True if the action is valid, false otherwise
     */
    public static boolean validateAction(Action action) {
        // Check for required properties
        if (action.getName() == null || action.getName().isEmpty()) {
            return false;
        }

        if (action.getElement() == null) {
            return false;
        }

        // Check for effects
        return !action.getEffects().isEmpty();
    }
}