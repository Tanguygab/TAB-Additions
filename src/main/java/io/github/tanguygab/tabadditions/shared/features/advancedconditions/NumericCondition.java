package io.github.tanguygab.tabadditions.shared.features.advancedconditions;

import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * A class handling numeric conditions to avoid
 * repeated number parsing for static numbers and therefore
 * reduce memory allocations and improve performance.
 */
public class NumericCondition extends SimpleCondition {

    /** {@code true} if left side is a static number, {@code false} if it has placeholders */
    private boolean leftSideStatic;

    /** If left side is static, value is stored here */
    private float leftSideValue;

    /** {@code true} if right side is a static number, {@code false} if it has placeholders */
    private boolean rightSideStatic;

    /** If right side is static, value is stored here */
    private float rightSideValue;

    /** Function that determines whether condition is met or not */
    private final BiFunction<Double, Double, Boolean> function;

    public NumericCondition(String[] arr, BiFunction<Double, Double, Boolean> function) {
        super(arr);
        this.function = function;
        try {
            leftSideValue = Float.parseFloat(leftSide);
            leftSideStatic = true;
        } catch (NumberFormatException e) {
            //not a valid number
        }
        try {
            rightSideValue = Float.parseFloat(rightSide);
            rightSideStatic = true;
        } catch (NumberFormatException e) {
            //not a valid number
        }
    }

    /**
     * Returns left side of this condition. If it's a static number it is
     * returned, if not, placeholders are replaced and then text parsed and returned.
     *
     * @return  parsed left side
     */
    public double getLeftSide(TabPlayer viewer, TabPlayer target) {
        if (leftSideStatic) return leftSideValue;
        String value = parseLeftSide(viewer,target);
        if (value.contains(",")) value = value.replace(",", "");
        return parseDouble(value);
    }

    /**
     * Returns right side of this condition. If it's a static number it is
     * returned, if not, placeholders are replaced and then text parsed and returned.
     *
     * @return  parsed right side
     */
    public double getRightSide(TabPlayer viewer, TabPlayer target) {
        if (rightSideStatic) return rightSideValue;
        String value = parseRightSide(viewer,target);
        if (value.contains(",")) value = value.replace(",", "");
        return parseDouble(value);
    }

    @Override
    public boolean isMet(TabPlayer viewer, TabPlayer target) {
        return function.apply(getLeftSide(viewer,target), getRightSide(viewer,target));
    }

    /**
     * Parses double in given string and returns it.
     * Returns second argument if string is not valid and prints a console warn.
     *
     * @param   output
     *          string to parse
     * @return  parsed double or {@code defaultValue} if input is invalid
     */
    public double parseDouble(@NotNull String output) {
        try {return Double.parseDouble(output);}
        catch (NumberFormatException e) {return 0;}
    }
}