package io.github.tanguygab.tabadditions.shared.features.titles

import io.github.tanguygab.tabadditions.shared.TABAdditions
import io.github.tanguygab.tabadditions.shared.commands.TitleCmd
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.cpu.TimedCaughtTask
import me.neznamy.tab.shared.features.PlaceholderManagerImpl
import me.neznamy.tab.shared.features.types.JoinListener
import me.neznamy.tab.shared.features.types.RefreshableFeature
import me.neznamy.tab.shared.features.types.UnLoadable
import me.neznamy.tab.shared.platform.TabPlayer
import me.neznamy.tab.shared.util.cache.StringToComponentCache
import java.util.UUID

class TitleManager(private val plugin: TABAdditions) : RefreshableFeature(), UnLoadable, JoinListener {
    override fun getFeatureName() = "Title"
    override fun getRefreshDisplayName() = "&aTitle&r"

    val titles = mutableMapOf<String, Title>()
    private val announcedTitles = mutableMapOf<TabPlayer, Title>()
    private val toggled: MutableList<UUID>
    private val toggleCmd: Boolean

    init {
        val tab = TAB.getInstance()
        tab.command.registerSubCommand(TitleCmd(this))

        val config = plugin.config
        toggleCmd = config.getBoolean("titles./toggletitle", true)
        if (toggleCmd) {
            tab.platform.registerCustomCommand("toggletitle") { sender, _ ->
                if (plugin.toggleCmd(sender, toggled, plugin.translation.titleOn, plugin.translation.titleOff)) {
                    refresh(sender, true)
                }
            }
        }
        toggled = plugin.loadData("title-off", toggleCmd)

        val titlesConfig = config.getConfigurationSection("titles.titles")
        titlesConfig.keys.forEach {
            val name = it.toString()
            val section = titlesConfig.getConfigurationSection(name)

            val title = section.getString("title") ?: ""
            val subtitle = section.getString("subtitle") ?: ""
            addUsedPlaceholders(PlaceholderManagerImpl.detectPlaceholders(title))
            addUsedPlaceholders(PlaceholderManagerImpl.detectPlaceholders(subtitle))
            titles[name] = Title(title, subtitle)
        }

        tab.cpuManager.processingThread.repeatTask(TimedCaughtTask(tab.cpuManager, {
            for (p in tab.onlinePlayers) refresh(p, false)
        }, featureName, "handling Title"), 2000)
    }

    override fun unload() {
        plugin.unloadData("title-off", toggled, toggleCmd)
    }

    override fun refresh(player: TabPlayer, force: Boolean) {
        if (player !in announcedTitles || player.uniqueId in toggled) return
        sendTitle(player, true)
    }

    private fun sendTitle(player: TabPlayer, refresh: Boolean) {
        val announced = announcedTitles[player]!!
        val title = titles[announced.title] ?: announced

        plugin.platform.sendTitle(
            player,
            parse(player, title.title),
            parse(player, title.subtitle),
            if (refresh) 0 else 20,
            60,
            20
        )
    }

    private fun parse(player: TabPlayer, text: String) = plugin.toFlatText(cache[plugin.parsePlaceholders(text, player)])

    fun announceTitle(player: TabPlayer, titleStr: String) {
        if (player.uniqueId in toggled) return
        addUsedPlaceholders(PlaceholderManagerImpl.detectPlaceholders(titleStr))
        val titleSplit = titleStr.split("\n")
        val title = Title(titleSplit[0], if (titleSplit.size > 1) titleSplit[1] else "")
        announcedTitles[player] = title
        sendTitle(player, false)

        val cpu = TAB.getInstance().cpuManager
        cpu.processingThread.executeLater(TimedCaughtTask(cpu, {
            if (title == announcedTitles[player]) announcedTitles.remove(player)
        }, featureName, "handling Title on join for " + player.name), 2000)
    }

    override fun onJoin(player: TabPlayer) {
        val property = player.loadPropertyFromConfig(this, "join-title", "").currentRawValue
        if (property.isEmpty()) return
        announceTitle(player, property)
    }

    companion object {
        private val cache = StringToComponentCache("Title", 100)
    }
}
