package io.github.tanguygab.tabadditions.shared.features.chat.mentions

import me.neznamy.tab.shared.placeholders.conditions.Condition
import me.neznamy.tab.shared.platform.TabPlayer
import net.kyori.adventure.sound.Sound

data class CustomMention(
    private val input: String?,
    private val output: String?,
    private val condition: Condition?,
    val sound: Sound
) {
    fun matches(msg: String) = msg.matches(input!!.toRegex())
    fun replace(msg: String) = msg.replace(input!!.toRegex(), output!!)
    fun isConditionMet(player: TabPlayer) = condition?.isMet(player) ?: true
}
