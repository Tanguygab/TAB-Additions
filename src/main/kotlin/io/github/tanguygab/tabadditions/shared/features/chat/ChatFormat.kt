package io.github.tanguygab.tabadditions.shared.features.chat

import io.github.tanguygab.tabadditions.shared.features.advancedconditions.AdvancedConditions
import me.neznamy.tab.shared.platform.TabPlayer

open class ChatFormat(
    val name: String,
    val displayName: String,
    private val condition: AdvancedConditions?,
    val viewCondition: AdvancedConditions?,
    val channel: String,
    val text: String
) {
    fun isConditionMet(p: TabPlayer) = condition?.isMet(p) ?: true
    fun hasNoViewCondition() = viewCondition == null
    fun isViewConditionMet(sender: TabPlayer, viewer: TabPlayer) = hasNoViewCondition() || viewCondition!!.isMet(viewer, sender)
}
