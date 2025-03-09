package edu.kit.kastel.monstergame.model.command;

import edu.kit.kastel.monstergame.model.Action;
import edu.kit.kastel.monstergame.model.Monster;

import java.util.List;

/**
 * command interface.
 * @author uuifx
 */
public interface CommandInterface {
    /**
     * Action selection.
     * @param monster monster
     * @param opponents opponent
     * @return selected action
     */
    Action selectAction(Monster monster, List<Monster> opponents);
}