package io.github.tanguygab.tabadditions.shared.features.advancedconditions

import io.github.tanguygab.tabadditions.shared.TABAdditions
import me.neznamy.tab.shared.features.PlaceholderManagerImpl
import me.neznamy.tab.shared.platform.TabPlayer

/**
 * An abstract class representing a simple condition
 */
abstract class SimpleCondition(arr: List<String>) {
    /** Text on the left side of condition  */
    protected var leftSide: String = if (arr.isEmpty()) "" else arr[0]

    /** Placeholders used on the left side  */
    private val leftSidePlaceholders = PlaceholderManagerImpl.detectPlaceholders(leftSide)

    /** Text on the right side of condition  */
    protected var rightSide: String = (if (arr.size < 2) "" else arr[1])

    /** Placeholders used on the right side  */
    private val rightSidePlaceholders = PlaceholderManagerImpl.detectPlaceholders(rightSide)

    /**
     * Replaces placeholders on the left side and return result
     *
     * @return  replaced left side
     */
    fun parseLeftSide(viewer: TabPlayer, target: TabPlayer) = parseSide(viewer, target, leftSide, leftSidePlaceholders)

    /**
     * Replaces placeholders on the right side and return result
     * @return   replaced right side
     */
    fun parseRightSide(viewer: TabPlayer, target: TabPlayer) = parseSide(viewer, target, rightSide, rightSidePlaceholders)

    /**
     * Replaces placeholders in provided value
     *
     * @param   value
     * string to replace placeholders in
     * @param   placeholders
     * used placeholders
     * @return  replaced string
     */
    fun parseSide(viewer: TabPlayer, target: TabPlayer, value: String, placeholders: List<String>)
    = TABAdditions.INSTANCE.parsePlaceholders(value, target, viewer, placeholders)

    /**
     * Returns `true` if condition is met for player, `false` if not
     *
     * @return  `true` if met, `false` if not
     */
    abstract fun isMet(viewer: TabPlayer, target: TabPlayer): Boolean
}