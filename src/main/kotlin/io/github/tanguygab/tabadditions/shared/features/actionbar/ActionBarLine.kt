package io.github.tanguygab.tabadditions.shared.features.actionbar

import me.neznamy.tab.shared.placeholders.conditions.Condition
import me.neznamy.tab.shared.platform.TabPlayer

data class ActionBarLine(val text: String, private val condition: Condition? = null) {
    fun isConditionMet(player: TabPlayer) = condition?.isMet(player) ?: true
}
