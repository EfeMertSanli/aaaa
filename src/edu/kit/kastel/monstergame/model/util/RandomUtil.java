package edu.kit.kastel.monstergame.model.util;

import java.util.Random;
import java.util.Scanner;

/**
 * Utility class for generating random numbers.
 * Implemented as a singleton to ensure only one instance is used throughout the game.
 * @author uuifx
 */
public final class RandomUtil {
    // Singleton instance
    private static RandomUtil instance;

    private final Random attackRandom; // For attacks, damage, crits
    private final Random statusRandom; // For status condition checks only
    private final boolean inDebugMode;
    private final Scanner scanner;

    /**
     * Private constructor to prevent direct instantiation.
     * Use getInstance() instead.
     *
     * @param seed The seed for the random number generator
     * @param debugMode Whether to run in debug mode with interactive prompts
     */
    private RandomUtil(long seed, boolean debugMode) {
        this.attackRandom = new Random(seed);
        this.statusRandom = new Random(seed); // Use a different seed for status checks
        this.inDebugMode = debugMode;
        this.scanner = debugMode ? new Scanner(System.in) : null;
    }

    /**
     * Initialize the singleton instance.
     * This should be called exactly once at the start of the program.
     *
     * @param seed The seed for the random number generator
     * @param debugMode Whether to run in debug mode with interactive prompts
     */
    public static void initialize(long seed, boolean debugMode) {
        if (instance != null) {
            System.out.println("Warning: RandomUtil is being reinitialized!");
        }
        instance = new RandomUtil(seed, debugMode);
    }

    /**
     * Get the singleton instance.
     * RandomUtil must be initialized with initialize() before calling this.
     *
     * @return The RandomUtil instance
     * @throws IllegalStateException If getInstance is called before initialization
     */
    public static RandomUtil getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RandomUtil has not been initialized! Call initialize() first.");
        }
        return instance;
    }

    /**
     * Generates a boolean value with the given probability for attack-related rolls.
     *
     * @param probability The probability of returning true (0-100)
     * @param decisionDescription Description for debug mode
     * @return true with the given probability
     */
    public boolean rollChance(double probability, String decisionDescription) {
        if (inDebugMode) {
            System.out.printf("Decide %s: yes or no? (y/n)%n", decisionDescription);
            String input = scanner.nextLine().trim().toLowerCase();
            while (!input.equals("y") && !input.equals("n")
                    && !input.equals("yes") && !input.equals("no")) {
                System.out.println("Error, enter y or n.");
                System.out.printf("Decide %s: yes or no? (y/n)%n", decisionDescription);
                input = scanner.nextLine().trim().toLowerCase();
            }
            return input.equals("y") || input.equals("yes");
        } else {
            double value = attackRandom.nextDouble() * 100;
            boolean result = value <= probability;
            return result;
        }
    }

    /**
     * Generates a boolean value with the given probability for status condition checks.
     *
     * @param probability The probability of returning true (0-100)
     * @param decisionDescription Description for debug mode
     * @return true with the given probability
     */
    public boolean rollStatusChance(double probability, String decisionDescription) {
        if (inDebugMode) {
            System.out.printf("Decide %s: yes or no? (y/n)%n", decisionDescription);
            String input = scanner.nextLine().trim().toLowerCase();
            while (!input.equals("y") && !input.equals("n")
                    && !input.equals("yes") && !input.equals("no")) {
                System.out.println("Error, enter y or n.");
                System.out.printf("Decide %s: yes or no? (y/n)%n", decisionDescription);
                input = scanner.nextLine().trim().toLowerCase();
            }
            return input.equals("y") || input.equals("yes");
        } else {
            double value = statusRandom.nextDouble() * 100;
            boolean result = value <= probability;
            return result;
        }
    }

    /**
     * Generates a random double in the range for damage calculations.
     * @param min The minimum value
     * @param max The maximum value
     * @param decisionDescription Description for debug mode
     * @return A random double in the range
     */
    public double getRandomDouble(double min, double max, String decisionDescription) {
        if (inDebugMode) {
            System.out.printf("Decide %s: a number between %.2f and %.2f?%n",
                    decisionDescription, min, max);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value < min || value > max) {
                    System.out.println("Error, out of range.");
                    return getRandomDouble(min, max, decisionDescription);
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Error, invalid number format.");
                return getRandomDouble(min, max, decisionDescription);
            }
        } else {
            return min + (attackRandom.nextDouble() * (max - min));
        }
    }

    /**
     * Generates a random integer in the range for repetition counts and protection durations.
     * @param min The minimum value
     * @param max The maximum value
     * @param decisionDescription Description for debug mode
     * @return A random integer in the range [min, max]
     */
    public int getRandomInt(int min, int max, String decisionDescription) {
        if (inDebugMode) {
            System.out.printf("Decide %s: an integer between %d and %d?%n",
                    decisionDescription, min, max);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value < min || value > max) {
                    System.out.println("Error, out of range.");
                    return getRandomInt(min, max, decisionDescription);
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Error, invalid number format.");
                return getRandomInt(min, max, decisionDescription);
            }
        } else {
            return min + attackRandom.nextInt(max - min + 1);
        }
    }
}