package io.github.tanguygab.tabadditions.shared.commands

import io.github.tanguygab.tabadditions.shared.TABAdditions
import io.github.tanguygab.tabadditions.shared.features.actionbar.ActionBarManager
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.command.SubCommand
import me.neznamy.tab.shared.platform.TabPlayer

class ActionBarCmd(private val manager: ActionBarManager) : SubCommand("actionbar", null) {
    override fun execute(sender: TabPlayer?, args: Array<String>) {
        val instance = TABAdditions.INSTANCE

        if (args.isEmpty()) {
            sendMessage(sender, "&cYou have to provide a player!")
            return
        }
        if (args.size < 2) {
            sendMessage(sender, "&cYou have to provide an actionbar!")
            return
        }

        val name = args[0]
        val actionbar = args[1]

        if (name == "*") {
            TAB.getInstance().onlinePlayers.forEach {
                manager.announceBar(it, actionbar)
            }
            return
        }

        val self = name.equals("me", ignoreCase = true)
        val player = if (self) sender else instance.getPlayer(name)
        if (player == null) {
            sendMessage(sender, if (self) messages.commandOnlyFromGame else messages.getPlayerNotFound(name))
            return
        }
        manager.announceBar(player, actionbar)
    }

    override fun complete(sender: TabPlayer?, arguments: Array<String>) = if (arguments.size == 1)
        getOnlinePlayers(arguments[0]).apply {
            add("*")
            add("me")
        }
    else getStartingArgument(manager.actionBars.keys, arguments[1])
}
