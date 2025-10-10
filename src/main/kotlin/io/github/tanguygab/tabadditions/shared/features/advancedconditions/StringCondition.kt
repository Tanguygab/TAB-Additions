package io.github.tanguygab.tabadditions.shared.features.advancedconditions

import me.neznamy.tab.shared.platform.TabPlayer

class StringCondition(arr: List<String>, private val function: (String, String) -> Boolean) : SimpleCondition(arr) {
    override fun isMet(viewer: TabPlayer, target: TabPlayer) = function(parseLeftSide(viewer, target), parseRightSide(viewer, target))
}
