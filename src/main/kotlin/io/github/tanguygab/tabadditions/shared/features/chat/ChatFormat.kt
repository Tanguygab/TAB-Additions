package io.github.tanguygab.tabadditions.shared.features.chat

import me.neznamy.tab.shared.placeholders.conditions.Condition
import me.neznamy.tab.shared.platform.TabPlayer

open class ChatFormat(
    val name: String,
    val displayName: String,
    private val condition: Condition?,
    val viewCondition: Condition?,
    val channel: String,
    val text: String
) {
    fun isConditionMet(p: TabPlayer) = condition?.isMet(p) ?: true
    fun hasNoViewCondition() = viewCondition == null
    fun isViewConditionMet(sender: TabPlayer, viewer: TabPlayer) = hasNoViewCondition() || viewCondition!!.isMet(viewer, sender)
}
