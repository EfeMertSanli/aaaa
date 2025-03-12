package edu.kit.kastel.monstergame.model.util;

import edu.kit.kastel.monstergame.model.Action;
import edu.kit.kastel.monstergame.model.effect.Effect;
import edu.kit.kastel.monstergame.model.effect.RepeatEffect;
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
        // For handling repeat blocks
        List<String> currentRepeatLines = null;
        int repeatLevel = 0;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.equals("end action")) {
                break;
            }
            // Handle repeat blocks
            if (line.startsWith("repeat ")) {
                if (repeatLevel == 0) {
                    // Start a new repeat block
                    currentRepeatLines = new ArrayList<>();
                    currentRepeatLines.add(line); // Store the repeat line itself
                    repeatLevel = 1;
                } else {
                    // Nested repeats - just add to current block for error handling later
                    currentRepeatLines.add(line);
                    repeatLevel++;
                }
                continue;
            }
            if (line.equals("end repeat")) {
                if (repeatLevel == 1) {
                    // Process the completed repeat block
                    Effect repeatEffect = processRepeatBlock(currentRepeatLines);
                    if (repeatEffect != null) {
                        effects.add(repeatEffect);
                    }
                    currentRepeatLines = null;
                    repeatLevel = 0;
                } else if (repeatLevel > 1) {
                    // Nested end repeat - just add to current block
                    currentRepeatLines.add(line);
                    repeatLevel--;
                } else {
                    System.err.println("Error: 'end repeat' without matching 'repeat'");
                }
                continue;
            }
            if (repeatLevel > 0) {
                currentRepeatLines.add(line);
            } else {
                Effect effect = EffectParser.parseEffect(line);
                if (effect != null) {
                    effects.add(effect);
                }
            }
        }
        return effects;
    }
    private static Effect processRepeatBlock(List<String> repeatLines) {
        if (repeatLines == null || repeatLines.isEmpty()) {
            return null;
        }
        // Parse the repeat header
        String repeatHeader = repeatLines.get(0);
        String[] parts = repeatHeader.split("\\s+");
        int count = 0;
        int minCount = 0;
        int maxCount = 0;
        boolean randomCount = false;

        if (parts.length >= 3 && parts[1].equalsIgnoreCase("random")) {
            randomCount = true;
            minCount = Integer.parseInt(parts[2]);
            maxCount = Integer.parseInt(parts[3]);
        } else {

            count = Integer.parseInt(parts[1]);
        }
        // Parse the nested effects
        List<Effect> nestedEffects = new ArrayList<>();
        for (int i = 1; i < repeatLines.size(); i++) {
            String effectLine = repeatLines.get(i);
            // Skip nested repeats
            if (effectLine.startsWith("repeat ") || effectLine.equals("end repeat")) {
                System.err.println("Warning: Nested repeats are not supported and will be ignored");
                continue;
            }

            Effect effect = EffectParser.parseEffect(effectLine);
            if (effect != null) {
                nestedEffects.add(effect);
            }
        }

        if (nestedEffects.isEmpty()) {
            System.err.println("Warning: Repeat block contains no valid effects");
            return null;
        }

        // Create the repeat effect
        if (randomCount) {
            return new RepeatEffect(minCount, maxCount, nestedEffects);
        } else {
            return new RepeatEffect(count, nestedEffects);
        }
    }
}