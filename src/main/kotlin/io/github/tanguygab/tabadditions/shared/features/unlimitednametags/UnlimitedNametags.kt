package io.github.tanguygab.tabadditions.shared.features.unlimitednametags

import io.github.tanguygab.tabadditions.shared.TABAdditions
import io.github.tanguygab.tabadditions.shared.features.unlimitednametags.lines.NametagLine
import io.github.tanguygab.tabadditions.shared.features.unlimitednametags.listeners.ChunkListener
import io.github.tanguygab.tabadditions.shared.features.unlimitednametags.listeners.RotationListener
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.features.types.JoinListener
import me.neznamy.tab.shared.features.types.QuitListener
import me.neznamy.tab.shared.features.types.TabFeature
import me.neznamy.tab.shared.features.types.UnLoadable
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class UnlimitedNametags(
    val plugin: TABAdditions,
    config: UnlimitedNametagsConfig
) : TabFeature(), UnLoadable, JoinListener, QuitListener {

    private val tab = TAB.getInstance()
    internal val bukkit get() = plugin.plugin as JavaPlugin
    val entityKey = NamespacedKey(bukkit, "unlimited-nametag")
    val lines = config.lines.mapValues { (name, line) ->
        NametagLine(this, line).also { plugin.registerFeature(it, "unlimited-nametags-line-$name") }
    }

    //private val toggled: MutableList<UUID> = plugin.loadData("nametags-off", toggleCommand.isNotEmpty())

    override fun getFeatureName() = "Unlimited Nametags"

    init {
//        if (config.toggleCommand.isNotEmpty()) {
//            tab.platform.registerCustomCommand(toggleCommand) { sender, _ ->
//                if (plugin.toggleCmd(sender, toggled, plugin.translation.nametagsOn, plugin.translation.nametagsOff)) {
//
//                }
//            }
//        }

        bukkit.server.pluginManager.apply {
            registerEvents(ChunkListener(this@UnlimitedNametags), bukkit)
            if (config.rotation) registerEvents(RotationListener(this@UnlimitedNametags), bukkit)
        }

        tab.onlinePlayers.forEach { onJoin(it) }
    }

    override fun unload() {
        tab.onlinePlayers.forEach { onQuit(it) }
        //plugin.unloadData("nametags-off", toggled, toggleCommand.isNotEmpty())
    }

    override fun onJoin(player: TabPlayer) {
        player.loadPropertyFromConfig(null, "customtagname", player.nickname)
        tab.nameTagManager?.hideNameTag(player)
        lines.values.forEach { it.refresh(player, true) }
    }

    override fun onQuit(player: TabPlayer) {
        tab.nameTagManager?.showNameTag(player)
    }
}

