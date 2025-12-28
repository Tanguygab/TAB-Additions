package io.github.tanguygab.tabadditions.spigot

import io.github.tanguygab.tabadditions.shared.features.chat.Chat
import me.neznamy.tab.api.placeholder.PlayerPlaceholder
import me.neznamy.tab.shared.TAB
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

class SpigotListener : Listener {
    private val tab = TAB.getInstance()

    @EventHandler
    fun onSneak(e: PlayerToggleSneakEvent) {
        (tab.placeholderManager.getPlaceholder("%sneak%") as PlayerPlaceholder).updateValue(
            getPlayer(e),
            e.isSneaking.toString()
        )
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onChat(e: AsyncPlayerChatEvent) {
        if (!tab.featureManager.isFeatureEnabled("Chat")) return
        e.isCancelled = true
        tab.featureManager.getFeature<Chat>("Chat").onChat(getPlayer(e), e.message)
    }

    private fun getPlayer(e: PlayerEvent) = tab.getPlayer(e.player.uniqueId)!!
}
