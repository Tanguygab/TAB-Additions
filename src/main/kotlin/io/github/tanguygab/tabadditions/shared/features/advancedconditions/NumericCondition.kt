package io.github.tanguygab.tabadditions.shared.features.advancedconditions

import me.neznamy.tab.shared.platform.TabPlayer

/**
 * A class handling numeric conditions to avoid
 * repeated number parsing for static numbers and therefore
 * reduce memory allocations and improve performance.
 */
class NumericCondition(
    arr: List<String>,
    /** Function that determines whether condition is met or not  */
    private val function: (Double, Double) -> Boolean
) : SimpleCondition(arr) {
    /** If left side is static, value is stored here  */
    private val leftSideValue = leftSide.toFloatOrNull()

    /** If right side is static, value is stored here  */
    private val rightSideValue = rightSide.toFloatOrNull()

    /**
     * Returns left side of this condition. If it's a static number it is
     * returned, if not, placeholders are replaced and then text parsed and returned.
     *
     * @return  parsed left side
     */
    fun getLeftSide(viewer: TabPlayer, target: TabPlayer): Double {
        if (leftSideValue != null) return leftSideValue.toDouble()
        var value = parseLeftSide(viewer, target)
        if ("," in value) value = value.replace(",", "")
        return value.toDoubleOrNull() ?: .0
    }

    /**
     * Returns right side of this condition. If it's a static number it is
     * returned, if not, placeholders are replaced and then text parsed and returned.
     *
     * @return  parsed right side
     */
    fun getRightSide(viewer: TabPlayer, target: TabPlayer): Double {
        if (rightSideValue != null) return rightSideValue.toDouble()
        var value = parseRightSide(viewer, target)
        if ("," in value) value = value.replace(",", "")
        return value.toDoubleOrNull() ?: .0
    }

    override fun isMet(viewer: TabPlayer, target: TabPlayer) = function(getLeftSide(viewer, target), getRightSide(viewer, target))
}