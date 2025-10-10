package io.github.tanguygab.tabadditions.shared.features.chat

import io.github.tanguygab.tabadditions.shared.TABAdditions
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl
import me.neznamy.tab.shared.platform.TabPlayer
import java.util.UUID

abstract class ChatManager @JvmOverloads constructor(
    protected val chat: Chat,
    protected val toggleCmd: Boolean = false,
    private val data: String? = null,
    cmd: String? = null,
    placeholder: String? = null
) {
    protected val plugin = TABAdditions.INSTANCE
    protected val tab = TAB.getInstance()!!
    protected val translation = plugin.translation

    protected lateinit var toggled: MutableList<UUID>
    private lateinit var placeholder: PlayerPlaceholderImpl
    private var invertPlaceholder = false
    private var toggledOn: String? = null
    private var toggledOff: String? = null

    init {
        if (toggleCmd) {
            plugin.platform.registerCommand(cmd!!)
            this.placeholder = tab.placeholderManager.registerPlayerPlaceholder("%$placeholder%", -1)
            { if (hasCmdToggled(it as TabPlayer)) "Off" else "On" }
            chat.placeholders.add(this.placeholder)
            toggled = plugin.loadData(data!!, true)
        }
    }

    open fun unload() {
        plugin.unloadData(data!!, toggled, toggleCmd)
    }

    protected fun setToggleCmdMsgs(toggledOn: String, toggledOff: String) {
        this.toggledOn = toggledOn
        this.toggledOff = toggledOff
    }

    protected fun invertToggleCmdPlaceholder() {
        invertPlaceholder = true
    }

    fun hasCmdToggled(p: TabPlayer?) = p?.uniqueId in toggled

    fun toggleCmd(player: TabPlayer): Boolean {
        return plugin.toggleCmd(toggleCmd, player, toggled, placeholder, toggledOn!!, toggledOff!!, invertPlaceholder)
    }

    abstract fun onCommand(sender: TabPlayer, command: String): Boolean
}
