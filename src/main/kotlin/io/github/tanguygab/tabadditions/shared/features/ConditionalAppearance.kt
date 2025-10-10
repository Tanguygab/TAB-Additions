package io.github.tanguygab.tabadditions.shared.features

import io.github.tanguygab.tabadditions.shared.features.advancedconditions.AdvancedConditions
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.features.types.JoinListener
import me.neznamy.tab.shared.features.types.QuitListener
import me.neznamy.tab.shared.features.types.RefreshableFeature
import me.neznamy.tab.shared.features.types.UnLoadable
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class ConditionalAppearance(plugin: Any?, private val def: Boolean)
    : RefreshableFeature(), JoinListener, QuitListener, UnLoadable {
    private val tab = TAB.getInstance()
    private val plugin = plugin as Plugin
    private val pwp = tab.featureManager.isFeatureEnabled("PerWorldPlayerList")
    private val properties = mutableMapOf<TabPlayer, Property>()

    init {
        if (pwp) addUsedPlaceholder("%world%")
        tab.onlinePlayers.forEach { onJoin(it) }
    }

    override fun getFeatureName() = "Conditional Appearance"
    override fun getRefreshDisplayName() = "&aConditional Appearance&r"

    override fun onJoin(player: TabPlayer) {
        properties[player] = player.loadPropertyFromConfig(this, "appearance-condition", "")
        refresh(player, true)
    }

    override fun onQuit(player: TabPlayer) {
        properties.remove(player)
    }

    override fun refresh(player: TabPlayer, force: Boolean) {
        tab.onlinePlayers.forEach {
            if (player === it) return
            refresh(player, it)
            refresh(it, player)
        }
    }

    private fun refresh(target: TabPlayer, viewer: TabPlayer) {
        val function = if (getCondition(target, viewer)) ::show else ::hide
        sync { function(p(viewer), p(target)) }
    }

    private fun sync(run: Runnable) {
        plugin.server.scheduler.runTask(plugin, run)
    }

    private fun p(p: TabPlayer) = p.player as Player

    fun getCondition(target: TabPlayer, viewer: TabPlayer): Boolean {
        if (pwp && target.world != viewer.world) return def

        val prop = properties[target] ?: return def
        val cond = prop.currentRawValue
        if (cond.isEmpty()) return def
        return def != AdvancedConditions.getCondition(cond)!!.isMet(viewer, target)
    }

    override fun unload() {
        plugin.server.onlinePlayers.forEach { viewer ->
            plugin.server.onlinePlayers.forEach { target ->
                if (viewer != target) show(viewer, target)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun show(viewer: Player, target: Player) {
        try {
            viewer.showPlayer(plugin, target)
        } catch (_: NoSuchMethodError) {
            viewer.showPlayer(target)
        }
    }

    @Suppress("DEPRECATION")
    private fun hide(viewer: Player, target: Player) {
        try {
            viewer.hidePlayer(plugin, target)
        } catch (_: NoSuchMethodError) {
            viewer.hidePlayer(target)
        }
    }
}
