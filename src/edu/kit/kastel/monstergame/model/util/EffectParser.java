package edu.kit.kastel.monstergame.model.util;

import edu.kit.kastel.monstergame.model.effect.ContinueEffect;
import edu.kit.kastel.monstergame.model.effect.Effect;
import edu.kit.kastel.monstergame.model.effect.DamageEffect;
import edu.kit.kastel.monstergame.model.effect.ProtectionEffect;
import edu.kit.kastel.monstergame.model.effect.StatusConditionEffect;
import edu.kit.kastel.monstergame.model.effect.HealingEffect;
import edu.kit.kastel.monstergame.model.effect.StatChangeEffect;
import edu.kit.kastel.monstergame.model.effect.RepeatEffect;
import edu.kit.kastel.monstergame.model.enums.StatType;
import edu.kit.kastel.monstergame.model.enums.StatusCondition;
import edu.kit.kastel.monstergame.model.enums.EffectTarget;
import edu.kit.kastel.monstergame.model.enums.ProtectionTarget;
import edu.kit.kastel.monstergame.model.enums.DamageType;

import java.util.ArrayList;
import java.util.List;


/**
 * Utility class for parsing different effect types from configuration strings.
 * @author uuifx
 */
public final class EffectParser {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private EffectParser() {
    }

    /**
     * Parses an effect from a line in the configuration file.
     * @param line The line containing the effect definition
     * @return The parsed Effect
     */
    public static Effect parseEffect(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length == 0) {
            return null;
        }
        String effectType = parts[0];
        switch (effectType) {
            case "damage":
                return parseDamageEffect(parts);
            case "inflictStatusCondition":
                return parseStatusConditionEffect(parts);
            case "inflictStatChange":
                return parseStatChangeEffect(parts);
            case "protectStat":
                return parseProtectionEffect(parts);
            case "heal":
                return parseHealingEffect(parts);
            case "continue":
                return parseContinueEffect(parts);
            default:
                System.err.println("Unknown effect type: " + effectType);
                return null;
        }
    }
    private static ContinueEffect parseContinueEffect(String[] parts) {
        if (parts.length < 2) {
            System.err.println("Invalid continue effect format: expected 'continue HIT_RATE'");
            return null;
        }

        try {
            double hitRate = Double.parseDouble(parts[1]) / 100.0;
            return new ContinueEffect(hitRate);
        } catch (NumberFormatException e) {
            System.err.println("Invalid hit rate for continue effect: " + parts[1]);
            return null;
        }
    }

    /**
     * Parses RepeatEffects.
     * @param parts parts
     * @param fullLine line
     * @return
     */
    private static Effect parseRepeatEffect(String[] parts, String fullLine) {
        if (parts.length < 2) {
            System.err.println("Invalid repeat effect format");
            return null;
        }

        int count = 0;
        int minCount = 0;
        int maxCount = 0;
        boolean randomCount = false;
        List<Effect> nestedEffects = new ArrayList<>();

        // Check if this is a random count
        if (parts[1].equalsIgnoreCase("random") && parts.length >= 4) {
            randomCount = true;
            minCount = Integer.parseInt(parts[2]);
            maxCount = Integer.parseInt(parts[3]);
        } else {
            count = Integer.parseInt(parts[1]);
        }

        // placeholder that will be populated later
        if (randomCount) {
            return new RepeatEffect(minCount, maxCount, nestedEffects);
        } else {
            return new RepeatEffect(count, nestedEffects);
        }
    }

    /**
     * Parses a damage effect from parts of a line.
     * @param parts The parts of the line
     * @return The parsed DamageEffect, or null if parsing failed
     */
    private static DamageEffect parseDamageEffect(String[] parts) {
        if (parts.length < 5) {
            System.err.println("Invalid damage effect format");
            return null;
        }

        EffectTarget damageTarget = getEffectTarget(parts[1]);
        if (damageTarget == null) {
            return null;
        }
        DamageType damageType = getDamageType(parts[2]);
        if (damageType == null) {
            return null;
        }
        int power;
        double hitRate;
        power = Integer.parseInt(parts[3]);
        hitRate = Double.parseDouble(parts[4]) / 100.0; // Convert percentage to decimal

        return new DamageEffect(damageTarget, damageType, power, hitRate);
    }

    /**
     * Gets a DamageType from a string.
     * @param typeStr The type string
     * @return DamageType.
     */
    public static DamageType getDamageType(String typeStr) {
        if (typeStr.equalsIgnoreCase("base")) {
            return DamageType.BASE;
        } else if (typeStr.equalsIgnoreCase("rel")) {
            return DamageType.RELATIVE;
        } else if (typeStr.equalsIgnoreCase("abs")) {
            return DamageType.ABSOLUTE;
        } else {
            System.err.println("Unknown damage type: " + typeStr);
            return null;
        }
    }

    /**
     * Parses a status condition effect from parts of a line.
     * @param parts The parts of the line
     * @return The parsed StatusConditionEffect.
     */
    private static StatusConditionEffect parseStatusConditionEffect(String[] parts) {
        if (parts.length < 4) {
            System.err.println("Invalid status condition effect format");
            return null;
        }

        EffectTarget statusTarget = getEffectTarget(parts[1]);
        if (statusTarget == null) {
            return null;
        }
        StatusCondition condition;
        double statusHitRate;
        condition = StatusCondition.valueOf(parts[2]);
        statusHitRate = Double.parseDouble(parts[3]) / 100.0;

        return new StatusConditionEffect(statusTarget, condition, statusHitRate);
    }

    /**
     * Parses a stat change effect from parts of a line.
     *
     * @param parts The parts of the line
     * @return The parsed StatChangeEffect
     */
    private static Effect parseStatChangeEffect(String[] parts) {
        if (parts.length < 5) {
            System.err.println("Invalid stat change effect format");
            return null;
        }

        EffectTarget statTarget = getEffectTarget(parts[1]);
        if (statTarget == null) {
            return null;
        }

        StatType stat;
        int stages;
        double statHitRate;
        stat = StatType.valueOf(parts[2]);
        stages = Integer.parseInt(parts[3]);
        statHitRate = Double.parseDouble(parts[4]) / 100.0;
        return new StatChangeEffect(statTarget, stat, stages, statHitRate);
    }

    /**
     * Parses a protection effect from parts of a line.
     * @param parts The parts of the line
     * @return The parsed ProtectionEffect
     */
    private static ProtectionEffect parseProtectionEffect(String[] parts) {
        if (parts.length < 4) {
            System.err.println("Invalid protection effect format");
            return null;
        }
        ProtectionTarget protectTarget = getProtectionTarget(parts[1]);
        if (protectTarget == null) {
            return null;
        }
        if (parts[2].equalsIgnoreCase("random") && parts.length >= 6) {
            int minRounds = Integer.parseInt(parts[3]);
            int maxRounds = Integer.parseInt(parts[4]);
            double protectHitRate = Double.parseDouble(parts[5]) / 100.0;
            return new ProtectionEffect(protectTarget, minRounds, maxRounds, protectHitRate);
        } else {
            int rounds = Integer.parseInt(parts[2]);
            double protectHitRate = Double.parseDouble(parts[3]) / 100.0;
            return new ProtectionEffect(protectTarget, rounds, protectHitRate);
        }

    }

    /**
     * Gets a ProtectionTarget from a string.
     * @param targetStr The target string
     * @return ProtectionTarget
     */
    public static ProtectionTarget getProtectionTarget(String targetStr) {
        if (targetStr.equalsIgnoreCase("health")) {
            return ProtectionTarget.HEALTH;
        } else if (targetStr.equalsIgnoreCase("stats")) {
            return ProtectionTarget.STATS;
        } else {
            System.err.println("Unknown protection target: " + targetStr);
            return null;
        }
    }

    /**
     * Parses a healing effect from parts of a line.
     * @param parts The parts of the line
     * @return The parsed HealingEffect
     */
    private static HealingEffect parseHealingEffect(String[] parts) {
        if (parts.length < 5) {
            System.err.println("Invalid healing effect format");
            return null;
        }

        EffectTarget healTarget = getEffectTarget(parts[1]);
        if (healTarget == null) {
            return null;
        }

        DamageType healType = getDamageType(parts[2]);
        if (healType == null) {
            return null;
        }
        int healPower;
        double healHitRate;
        healPower = Integer.parseInt(parts[3]);
        healHitRate = Double.parseDouble(parts[4]) / 100.0;
        return new HealingEffect(healTarget, healType, healPower, healHitRate);
    }

    /**
     * Determines the effect target from a string.
     *
     * @param targetStr The target string
     * @return EffectTarget
     */
    public static EffectTarget getEffectTarget(String targetStr) {
        if (targetStr.equalsIgnoreCase("target")) {
            return EffectTarget.TARGET;
        } else if (targetStr.equalsIgnoreCase("user") || targetStr.equalsIgnoreCase("self")) {
            return EffectTarget.SELF;
        } else {
            System.err.println("Unknown target: " + targetStr);
            return null;
        }
    }
}