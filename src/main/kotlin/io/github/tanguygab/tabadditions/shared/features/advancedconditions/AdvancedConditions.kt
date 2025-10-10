package io.github.tanguygab.tabadditions.shared.features.advancedconditions

import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.features.PlaceholderManagerImpl
import me.neznamy.tab.shared.platform.TabPlayer

/**
 * The main condition class. It allows users to configure different
 * condition types that must be met in order to display specified
 * text or make a condition requirement for a visual to be displayed.
 */
class AdvancedConditions(
    /** Name of this condition defined in configuration  */
    val name: String, conditions: Map<String, String>
) {
    /** All defined sub-conditions inside this conditions  */
    private var subConditions = mutableMapOf<((TabPlayer, TabPlayer) -> Boolean)?, String>()

    /**
     * Refresh interval of placeholder created from this condition.
     * It is calculated based on nested placeholders used in sub-conditions.
     */
    var refresh = -1

    /** List of all placeholders used inside this condition  */
    private val placeholdersInConditions = mutableListOf<String>()

    /** Boolean that returns whether the condition has relational placeholders or not  */
    private var hasRelationalPlaceholders: Boolean

    /**
     * Constructs new instance with given parameters and registers
     * this condition to list as well as the placeholder.
     *
     * @param   name
     * name of condition
     * @param   conditions
     * map of condition lines and their output
     */
    init {
        conditions.forEach { (line: String, text: String) ->
            val condition = compile(line)
            if (condition == null && line != "else") return@forEach

            subConditions[condition] = text
            if (!line.startsWith("permission:")) placeholdersInConditions.addAll(
                PlaceholderManagerImpl.detectPlaceholders(line)
            ) else if (refresh > 1000 || refresh == -1) refresh = 1000 //permission refreshing will be done every second
            placeholdersInConditions.addAll(PlaceholderManagerImpl.detectPlaceholders(text))
        }
        hasRelationalPlaceholders = placeholdersInConditions.any { it.startsWith("%rel_") }
        registeredConditions[name] = this

        val pm = TAB.getInstance().placeholderManager
        if (hasRelationalPlaceholders) pm.registerRelationalPlaceholder("%rel_condition+:$name%", refresh)
        { viewer, target -> getText(viewer as TabPlayer, target as TabPlayer) }
        else pm.registerPlayerPlaceholder("%condition+:$name%", refresh)
        { getText(it as TabPlayer, it) }
    }

    /**
     * Configures refresh interval and registers nested placeholders
     */
    fun finishSetup() {
        val pm = TAB.getInstance().placeholderManager
        for (placeholder in placeholdersInConditions) {
            val pl = pm.getPlaceholder(placeholder)
            pl.addParent((if (hasRelationalPlaceholders) "%rel_" else "%") + "condition+:" + name + "%")
            if (pl.refresh < refresh && pl.refresh != -1) refresh = pl.refresh
        }
        pm.addUsedPlaceholders(placeholdersInConditions)
    }

    /**
     * Returns text for player based on if condition is met or not
     *
     * @return  yes or no value depending on if condition passed or not
     */
    fun getText(viewer: TabPlayer, target: TabPlayer) = subConditions[check(viewer, target)]

    /**
     * Returns `true` if condition is met for player, `false` if not
     *
     * @return  `true` if met, `false` if not
     */
    fun isMet(viewer: TabPlayer, target: TabPlayer) = check(viewer, target) != null

    /**
     * Returns `true` if condition is met for player, `false` if not
     *
     * @return  `true` if met, `false` if not
     */
    fun isMet(viewer: TabPlayer) = check(viewer, viewer) != null

    fun check(viewer: TabPlayer, target: TabPlayer) = subConditions.keys.find { it == null || it(viewer, target) }

    companion object {
        /** All conditions defined in configuration including anonymous conditions  */
        private val registeredConditions = mutableMapOf<String, AdvancedConditions>()

        /** All supported sub-condition types  */
        private val conditionTypes = mutableMapOf<String, (String) -> (TabPlayer, TabPlayer) -> Boolean>(
            ">=" to { { viewer: TabPlayer, target: TabPlayer ->
                NumericCondition(it.split(">="))
                { left: Double, right: Double -> left >= right }.isMet(viewer, target)
            } },
            ">" to { { viewer: TabPlayer, target: TabPlayer ->
                NumericCondition(it.split(">"))
                { left: Double, right: Double -> left > right }.isMet(viewer, target)
            } },
            "<=" to { { viewer: TabPlayer, target: TabPlayer ->
                NumericCondition(it.split("<="))
                { left: Double, right: Double -> left <= right }.isMet(viewer, target)
            } },
            "<-" to { { viewer: TabPlayer, target: TabPlayer ->
                StringCondition(it.split("<-"))
                { obj: String, s: String -> s in obj }.isMet(viewer, target)
            } },
            "<" to { { viewer: TabPlayer, target: TabPlayer ->
                NumericCondition(it.split("<"))
                { left: Double, right: Double -> left < right }.isMet(viewer, target)
            } },
            "|-" to { { viewer: TabPlayer, target: TabPlayer ->
                StringCondition(it.split("|-"))
                { obj: String, prefix: String -> obj.startsWith(prefix) }.isMet(viewer, target)
            } },
            "-|" to { { viewer: TabPlayer, target: TabPlayer ->
                StringCondition(it.split("-|"))
                { obj: String, suffix: String -> obj.endsWith(suffix) }.isMet(viewer, target)
            } }
            ,
            "!=" to { { viewer: TabPlayer, target: TabPlayer ->
                StringCondition(it.split("!="))
                { left: String, right: String -> left != right }.isMet(viewer, target)
            } }
            ,
            "=" to { { viewer: TabPlayer, target: TabPlayer ->
                StringCondition(it.split("="))
                { str1: String, str2: String -> str1 == str2 }.isMet(viewer, target)
            } },
            "permission:" to { { viewer: TabPlayer, _: TabPlayer ->
                viewer.hasPermission(it.split(":", limit = 2)[1])
            } }
        )

        /**
         * Returns condition from given string. If the string is name of a condition,
         * that condition is returned. If it's a condition pattern, it is compiled and
         * returned. If the string is null, null is returned.
         *
         * @param   string
         * condition name or pattern
         * @return  condition from string
         */
        fun getCondition(string: String?): AdvancedConditions? {
            if (string.isNullOrEmpty()) return null
            if (string in registeredConditions) return registeredConditions[string]

            val c = AdvancedConditions("AnonymousCondition[$string]", mapOf(string to ""))
            c.finishSetup()
            return c
        }

        /**
         * Clears registered condition map on plugin reload
         */
        fun clearConditions() {
            registeredConditions.clear()
        }

        /**
         * Marks all placeholders used in the condition as used and registers them.
         * Using a separate method to avoid premature registration of nested conditional placeholders
         * before they are registered properly.
         */
        fun finishSetups() {
            registeredConditions.values.forEach { it.finishSetup() }
        }

        /**
         * Compiles condition from condition line. This includes detection
         * what kind of condition it is and creating it.
         *
         * @param   line
         * condition line
         * @return  compiled condition or null if no valid pattern was found
         */
        private fun compile(line: String) = conditionTypes.entries.find { it.key in line }?.value(line)
    }
}