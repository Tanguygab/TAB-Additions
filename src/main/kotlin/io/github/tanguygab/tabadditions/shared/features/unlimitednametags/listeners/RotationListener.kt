package io.github.tanguygab.tabadditions.shared.features.unlimitednametags.listeners

import io.github.tanguygab.tabadditions.shared.features.unlimitednametags.UnlimitedNametags
import me.neznamy.tab.shared.TAB
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class RotationListener(val feature: UnlimitedNametags) : Listener {

    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        if (!e.hasChangedOrientation()) return
        val player = TAB.getInstance().getPlayer(e.player.uniqueId) ?: return
        feature.lines.values.forEach { it.rotate(player) }
    }
}