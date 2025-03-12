package edu.kit.kastel.monstergame.model.effect;

import edu.kit.kastel.monstergame.model.enums.EffectTarget;
import edu.kit.kastel.monstergame.model.enums.EffectType;

/**
 * Represents a continue effect.
 * If this effect hits, nothing happens and execution continues with the next effect.
 * This allows controlling the hit rate of an action independently from the hit rate
 * of the first "real" effect.
 * @author uuifx
 */
public class ContinueEffect extends Effect {

    /**
     * Creates a new continue effect with the specified hit rate.
     * @param hitRate The probability of the effect hitting (0.0-1.0)
     */
    public ContinueEffect(double hitRate) {
        super(EffectType.CONTINUE, EffectTarget.SELF, hitRate);
    }

    @Override
    public String toString() {
        return String.format("%s(hit_rate=%.2f)", effectType.getValue(), hitRate);
    }
}