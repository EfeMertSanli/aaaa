package edu.kit.kastel.monstergame.model.combat;

import edu.kit.kastel.monstergame.model.Action;
import edu.kit.kastel.monstergame.model.Monster;
import edu.kit.kastel.monstergame.model.command.CommandInterface;
import edu.kit.kastel.monstergame.model.effect.Effect;
import edu.kit.kastel.monstergame.model.effect.RepeatEffect;
import edu.kit.kastel.monstergame.model.enums.EffectType;
import edu.kit.kastel.monstergame.model.enums.ProtectionTarget;
import edu.kit.kastel.monstergame.model.enums.StatusCondition;
import edu.kit.kastel.monstergame.model.util.RandomUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Main class responsible for the monster combat system.
 * @author uuifx
 */
public class CombatSystem {
    private List<Monster> monsters;
    private CommandInterface commandInterface;
    private boolean inDebugMode;

    private ActionExecutor actionExecutor;
    private StatusConditionHandler statusHandler;

    private int currentMonsterIndex;
    private boolean allActionsSelected;

    /**
     * Creates a new combat system with the given monsters and command interface.
     * @param monsters List of monsters that will participate in combat
     * @param commandInterface Interface for handling commands
     * @param debugMode Whether to run in debug mode with extra output
     */
    public CombatSystem(List<Monster> monsters, CommandInterface commandInterface, boolean debugMode) {
        this.monsters = new ArrayList<>(monsters);
        this.commandInterface = commandInterface;
        this.inDebugMode = debugMode;

        // Initialize helper classes using the singleton RandomUtil
        this.statusHandler = new StatusConditionHandler(debugMode);
        this.actionExecutor = new ActionExecutor(debugMode, statusHandler);

        // Assign contestant numbers
        for (int i = 0; i < monsters.size(); i++) {
            monsters.get(i).setContestantNumber(i + 1);
        }

        // Initialize phase tracking
        currentMonsterIndex = 0;
        allActionsSelected = false;
    }

    /**
     * Gets a copy of the monster list.
     *
     * @return A new list containing all monsters
     */
    public List<Monster> getMonsters() {
        return new ArrayList<>(monsters);
    }

    /**
     * Gets a list of monsters that are still active in combat.
     *
     * @return List of non-defeated monsters
     */
    public List<Monster> getActiveFighters() {
        List<Monster> active = new ArrayList<>();
        for (Monster monster : monsters) {
            if (!monster.isDefeated()) {
                active.add(monster);
            }
        }
        return active;
    }

    /**
     * Gets the next monster that needs to select an action.
     *
     * @return The next monster or null if all have selected actions
     */
    public Monster getNextMonsterForActionSelection() {
        List<Monster> sortedMonsters = new ArrayList<>(monsters);
        Collections.sort(sortedMonsters, new Comparator<Monster>() {
            @Override
            public int compare(Monster m1, Monster m2) {
                return Integer.compare(m1.getContestantNumber(), m2.getContestantNumber());
            }
        });
        for (Monster monster : sortedMonsters) {
            // Check if the monster hasn't been defeated, hasn't selected an action,
            // and hasn't passed its turn
            if (!monster.isDefeated() && monster.getSelectedAction() == null && !monster.hasPassed()) {
                return monster;
            }
        }
        return null;
    }

    /**
     * Checks if there is a winner in the combat.
     *
     * @return The winning monster, or null if there is no winner yet or it's a draw
     */
    public Monster checkForWinner() {
        List<Monster> activeFighters = getActiveFighters();

        if (activeFighters.size() < 2) {
            if (activeFighters.size() == 1) {
                return activeFighters.get(0);
            } else {
                return null;
            }
        }

        return null;
    }

    /**
     * Execute the action execution phase.
     */
    public void executeActionsPhase() {
        // Sort monsters by their effective speed
        List<Monster> sortedBySpeed = new ArrayList<>(getActiveFighters());
        Collections.sort(sortedBySpeed, (m1, m2) -> Integer.compare(m2.getEffectiveSpeed(), m1.getEffectiveSpeed()));

        for (Monster attacker : sortedBySpeed) {
            // Check if the monster is still alive before executing its action
            if (!attacker.isDefeated()) {
                Action action = attacker.getSelectedAction();
                System.out.println("\n" + "It's " + attacker.getName() + "'s turn.");

                // Process status conditions before action
                boolean skipAction = processStatusConditions(attacker);
                if (skipAction) {
                    // Apply burn damage even if action is skipped
                    if (attacker.getStatusCondition() == StatusCondition.BURN) {
                        statusHandler.applyBurnDamage(attacker);
                    }
                    continue;
                }

                // Handle passing
                if (action == null) {
                    handlePassingTurn(attacker);
                    continue;
                }

                // Execute the action
                executeMonsterAction(attacker, action);
            }
        }
    }

    /**
     * Process a monsters status conditions at the start of its turn.
     *
     * @param monster The monster to process status conditions for
     * @return true if the monster should skip its action, false otherwise
     */
    private boolean processStatusConditions(Monster monster) {
        StatusCondition currentCondition = monster.getStatusCondition();
        if (currentCondition == null) {
            return false;
        }

        // Check if condition ends
        boolean conditionEnds = RandomUtil.getInstance().rollChance(33.33, "status condition end for " + monster.getName());
        if (conditionEnds) {
            handleStatusConditionEnding(monster, currentCondition);
            return false;
        }

        displayStatusCondition(monster, currentCondition);

        // Skip action if still sleeping
        return currentCondition == StatusCondition.SLEEP;
    }

    /**
     * Display a monster's current status condition.
     */
    private void displayStatusCondition(Monster monster, StatusCondition condition) {
        if (condition == StatusCondition.BURN) {
            System.out.println(monster.getName() + " is burning!");
        } else if (condition == StatusCondition.WET) {
            System.out.println(monster.getName() + " is soaked!");
        } else if (condition == StatusCondition.QUICKSAND) {
            System.out.println(monster.getName() + " is stuck in quicksand!");
        } else if (condition == StatusCondition.SLEEP) {
            System.out.println(monster.getName() + " is sleeping and cannot move!");
        }
    }

    /**
     * Handle a status condition ending.
     */
    private void handleStatusConditionEnding(Monster monster, StatusCondition condition) {
        String conditionName = "";
        switch (condition) {
            case BURN:
                conditionName = "burning";
                break;
            case WET:
                conditionName = "soaked";
                break;
            case QUICKSAND:
                conditionName = "quicksand";
                break;
            case SLEEP:
                conditionName = "sleeping";
                break;
            default: break;
        }
        System.out.println(monster.getName() + "'s " + conditionName + " has faded!");
        monster.setStatusCondition(null);
    }

    /**
     * Handle a monster passing its turn.
     */
    private void handlePassingTurn(Monster monster) {
        System.out.println(monster.getName() + " passes!");
        if (monster.getStatusCondition() == StatusCondition.BURN) {
            statusHandler.applyBurnDamage(monster);
        }
    }

    /**
     * Execute a monsters action.
     */
    private void executeMonsterAction(Monster attacker, Action action) {
        System.out.println(attacker.getName() + " uses " + action.getName() + "!");

        // Execute the action
        boolean actionFailed = !actionExecutor.executeAction(attacker, action, monsters);

        // Report action failure
        if (actionFailed) {
            System.out.println("The action failed...");
        }

        // Apply burn damage after action if monster is burning
        if (attacker.getStatusCondition() == StatusCondition.BURN) {
            statusHandler.applyBurnDamage(attacker);
        }

        // Reset selected action
        attacker.setSelectedAction(null);
    }
    /**
     * Helper method to check if action has damage effects
     *
     * @param action selected action
     * @return true if the selected action does damage, false otherwise
     */
    private boolean actionHasDamage(Action action) {
        for (Effect effect : action.getEffects()) {
            if (effect.getEffectType() == EffectType.DAMAGE) {
                return true;
            } else if (effect.getEffectType() == EffectType.REPEAT) {
                RepeatEffect repeatEffect = (RepeatEffect) effect;
                for (Effect repeatedEffect : repeatEffect.getEffects()) {
                    if (repeatedEffect.getEffectType() == EffectType.DAMAGE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Process end of round effects.
     */
    public void endOfRoundPhase() {
        List<Monster> sortedMonsters = new ArrayList<>(monsters);

        Collections.sort(sortedMonsters, new Comparator<Monster>() {
            @Override
            public int compare(Monster m1, Monster m2) {
                return Integer.compare(m1.getContestantNumber(), m2.getContestantNumber());
            }
        });

        for (Monster monster : sortedMonsters) {
            if (!monster.isDefeated()) {
                Map<ProtectionTarget, Integer> protection = monster.getProtection();

                for (ProtectionTarget target : protection.keySet()) {
                    int rounds = protection.get(target);
                    if (rounds > 0) {
                        protection.put(target, rounds - 1);
                        if (protection.get(target) == 0) {
                            System.out.println(monster.getName() + "'s "
                                    + (target == ProtectionTarget.HEALTH ? "damage" : "stat reduction")
                                    + " protection has ended.");
                        }
                    }
                }

                statusHandler.evaluateStatusCondition(monster);
            }
        }
        for (Monster monster : monsters) {
            monster.setSelectedAction(null);
            monster.setHasPassed(false); // Reset the pass status
        }
    }

    /**
     * Start a competition with the selected monsters.
     *
     * @return The winning monster or null if it's a draw
     */
    public Monster startCombat() {
        int roundCount = 1;

        while (true) {
            System.out.println("\n=== Round " + roundCount + " ===");

            // Phase 0
            List<Monster> activeFighters = getActiveFighters();
            if (activeFighters.size() < 2) {
                if (activeFighters.size() == 1) {
                    Monster winner = activeFighters.get(0);
                    System.out.println(winner.getName() + " has no opponents left and wins the competition!");
                    return winner;
                } else {
                    System.out.println("No monsters left. It's a draw!");
                    return null;
                }
            }

            // Note to self, rest of the phasing is now in commandhandler

            roundCount++;
        }
    }
}