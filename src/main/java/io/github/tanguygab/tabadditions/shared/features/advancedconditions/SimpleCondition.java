package io.github.tanguygab.tabadditions.shared.features.advancedconditions;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;

import java.util.List;

/**
 * An abstract class representing a simple condition
 */
public abstract class SimpleCondition {

    /** Text on the left side of condition */
    protected String leftSide;

    /** Placeholders used on the left side */
    private final String[] leftSidePlaceholders;

    /** Text on the right side of condition */
    protected String rightSide;

    /** Placeholders used on the right side */
    private final String[] rightSidePlaceholders;

    public SimpleCondition(String[] arr) {
        leftSide = arr.length < 1 ? "" : arr[0];
        leftSidePlaceholders = TAB.getInstance().getPlaceholderManager().detectPlaceholders(leftSide).toArray(new String[0]);
        rightSide = arr.length < 2 ? "" : arr[1];
        rightSidePlaceholders = TAB.getInstance().getPlaceholderManager().detectPlaceholders(rightSide).toArray(new String[0]);
    }

    /**
     * Replaces placeholders on the left side and return result
     *
     * @return  replaced left side
     */
    public String parseLeftSide(TabPlayer viewer, TabPlayer target) {
        return parseSide(viewer, target, leftSide, leftSidePlaceholders);
    }

    /**
     * Replaces placeholders on the right side and return result
     * @return   replaced right side
     */
    public String parseRightSide(TabPlayer viewer, TabPlayer target) {
        return parseSide(viewer, target, rightSide, rightSidePlaceholders);
    }

    /**
     * Replaces placeholders in provided value
     *
     * @param   value
     *          string to replace placeholders in
     * @param   placeholders
     *          used placeholders
     * @return  replaced string
     */
    public String parseSide(TabPlayer viewer, TabPlayer target, String value, String[] placeholders) {
        return TABAdditions.getInstance().parsePlaceholders(value,viewer,target, List.of(placeholders));
    }

    /**
     * Returns {@code true} if condition is met for player, {@code false} if not
     *
     * @return  {@code true} if met, {@code false} if not
     */
    public abstract boolean isMet(TabPlayer viewer, TabPlayer target);
}