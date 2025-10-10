package io.github.tanguygab.tabadditions.shared.features.chat.emojis

import me.neznamy.tab.shared.platform.TabPlayer

data class EmojiCategory(val name: String, val emojis: MutableMap<String, String>, val output: String) {
    fun canUse(player: TabPlayer) = player.hasPermission("tabadditions.chat.emoji.category.$name")
    fun canUse(player: TabPlayer, emoji: String) = canUse(player) || player.hasPermission("tabadditions.chat.emoji.$emoji")

    fun ownedEmojis(p: TabPlayer) = if (canUse(p)) emojis.size
    else emojis.keys.count { canUse(p, it) }
}