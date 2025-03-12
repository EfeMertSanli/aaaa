package edu.kit.kastel.monstergame.model.combat;

import edu.kit.kastel.monstergame.model.Action;
import edu.kit.kastel.monstergame.model.Monster;
import edu.kit.kastel.monstergame.model.effect.DamageEffect;
import edu.kit.kastel.monstergame.model.enums.Element;
import edu.kit.kastel.monstergame.model.enums.StatType;
import edu.kit.kastel.monstergame.model.util.ElementEffectiveness;
import edu.kit.kastel.monstergame.model.util.RandomUtil;

/**
 * Calculates damage based on different factors during combat.
 *
 * @author uuifx
 */
public class DamageCalculator {
    private boolean inDebugMode;

    /**
     * Creates a new damage calculator.
     *
     * @param debugMode Whether to run in debug mode
     */
    public DamageCalculator(boolean debugMode) {
        this.inDebugMode = debugMode;
    }

    /**
     * Calculate base damage for an attack.
     *
     * @param attacker The monster performing the attack
     * @param target   The target monster
     * @param effect   The damage effect to calculate
     * @return The calculated damage amount
     */
    public int calculateBaseDamage(Monster attacker, Monster target, DamageEffect effect) {
        Action action = attacker.getSelectedAction();
        int baseValue = effect.getPower();
        double totalDamage = baseValue;
        RandomUtil randomUtil = RandomUtil.getInstance();
        Element actionElement = action.getElement();
        Element attackerElement = attacker.getElement();
        Element targetElement = target.getElement();
        double elementFactor = ElementEffectiveness.getElementFactor(actionElement, targetElement);
        totalDamage *= elementFactor;
        if (elementFactor > 1.0) {
            System.out.println("It is very effective!");
        } else if (elementFactor < 1.0) {
            System.out.println("It is not very effective...");
        }
        double attackerAtk = attacker.getEffectiveStat(StatType.ATK);
        double targetDef = target.getEffectiveStat(StatType.DEF);
        double statusFactor = attackerAtk / targetDef;
        totalDamage *= statusFactor;
        double attackerSpd = attacker.getEffectiveStat(StatType.SPD);
        double targetSpd = target.getEffectiveStat(StatType.SPD);
        double criticalChance = Math.pow(10, -targetSpd / attackerSpd) * 100;
        boolean isCriticalHit = randomUtil.rollChance(criticalChance, "critical hit");
        double criticalFactor = isCriticalHit ? 2.0 : 1.0;
        totalDamage *= criticalFactor;
        if (isCriticalHit) {
            System.out.println("Critical hit!");
        }
        double sameElementFactor = (actionElement == attackerElement) ? 1.5 : 1.0;
        totalDamage *= sameElementFactor;
        double randomFactor = randomUtil.getRandomDouble(0.85, 1.0, "damage random factor");
        totalDamage *= randomFactor;
        double normalizationFactor = 1.0 / 3.0;
        totalDamage *= normalizationFactor;
        int finalDamage = (int) Math.ceil(totalDamage);
        return finalDamage;
    }
}