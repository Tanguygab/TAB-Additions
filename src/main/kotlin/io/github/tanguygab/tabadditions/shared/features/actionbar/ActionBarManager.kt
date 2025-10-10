package io.github.tanguygab.tabadditions.shared.features.actionbar

import io.github.tanguygab.tabadditions.shared.TABAdditions
import io.github.tanguygab.tabadditions.shared.commands.ActionBarCmd
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.cpu.TimedCaughtTask
import me.neznamy.tab.shared.features.PlaceholderManagerImpl
import me.neznamy.tab.shared.features.types.CommandListener
import me.neznamy.tab.shared.features.types.JoinListener
import me.neznamy.tab.shared.features.types.RefreshableFeature
import me.neznamy.tab.shared.features.types.UnLoadable
import me.neznamy.tab.shared.platform.TabPlayer
import me.neznamy.tab.shared.util.cache.StringToComponentCache
import java.util.UUID

class ActionBarManager : RefreshableFeature(), UnLoadable, CommandListener, JoinListener {
    private val plugin: TABAdditions = TABAdditions.INSTANCE

    override fun getFeatureName() = "ActionBar"
    override fun getRefreshDisplayName() = "&aActionBar&r"
    override fun getCommand() = "/toggleactionbar"

    val actionBars = mutableMapOf<String, ActionBarLine>()
    private val announcedBars = mutableMapOf<TabPlayer, String>()
    private val toggled: MutableList<UUID>
    private val toggleCmd: Boolean

    init {
        val tab = TAB.getInstance()
        tab.command.registerSubCommand(ActionBarCmd(this))

        toggleCmd = plugin.config.getBoolean("actionbars./toggleactionbar", true)
        if (toggleCmd) plugin.platform.registerCommand("toggleactionbar")
        toggled = plugin.loadData("actionbar-off", toggleCmd)

        val barsConfig = plugin.config.getConfigurationSection("actionbars.bars")
        barsConfig.keys.forEach {
            val bar = it.toString()
            val section = barsConfig.getConfigurationSection(bar)

            val text = section.getString("text") ?: ""
            val condition = section.getString("condition")

            if (text.isNotEmpty()) addUsedPlaceholders(PlaceholderManagerImpl.detectPlaceholders(text))
            actionBars[bar] = ActionBarLine(
                text,
                if (condition == null) null
                else tab.placeholderManager.conditionManager.getByNameOrExpression(condition)
            )
        }

        tab.cpuManager.processingThread.repeatTask(TimedCaughtTask(tab.cpuManager, {
            tab.onlinePlayers.forEach { refresh(it, false) }
        }, featureName, "handling ActionBar"), 2000)
    }

    override fun unload() {
        plugin.unloadData("actionbar-off", toggled, toggleCmd)
    }

    override fun refresh(player: TabPlayer, force: Boolean) {
        var text = if (player in announcedBars) {
            val bar = announcedBars[player]!!
            actionBars[bar]?.text ?: bar
        } else getActionBar(player)?.text ?: return

        if (player.uniqueId in toggled) return

        text = plugin.parsePlaceholders(text, player)
        plugin.platform.sendActionbar(player, plugin.toFlatText(cache[text]))
    }

    fun getActionBar(player: TabPlayer) = actionBars.values.find { it.isConditionMet(player) }

    fun announceBar(player: TabPlayer, actionbar: String) {
        addUsedPlaceholders(PlaceholderManagerImpl.detectPlaceholders(actionbar))
        announcedBars[player] = actionbar
        refresh(player, true)

        val cpu = TAB.getInstance().cpuManager
        cpu.processingThread.executeLater(TimedCaughtTask(cpu, {
            if (actionbar == announcedBars[player]) announcedBars.remove(player)
        }, featureName, "handling ActionBar on join for " + player.name), 2000)
    }

    override fun onJoin(player: TabPlayer) {
        val property = player.loadPropertyFromConfig(this, "join-actionbar", "").currentRawValue
        if (property.isEmpty()) return
        announceBar(player, property)
    }

    override fun onCommand(player: TabPlayer, msg: String): Boolean {
        if (msg == command && plugin.toggleCmd(
                toggleCmd,
                player,
                toggled,
                plugin.translation.actionBarOn,
                plugin.translation.actionBarOff
            )
        ) {
            refresh(player, true)
            return true
        }
        return false
    }

    companion object {
        private val cache = StringToComponentCache("ActionBar", 100)
    }
}
