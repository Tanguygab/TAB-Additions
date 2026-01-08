package io.github.tanguygab.tabadditions.shared.features.chat.commands

import io.github.tanguygab.tabadditions.shared.features.chat.Chat
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager
import io.github.tanguygab.tabadditions.shared.features.chat.ChatUtils
import me.neznamy.tab.shared.config.file.ConfigurationSection
import me.neznamy.tab.shared.platform.TabPlayer
import java.util.UUID

class CommandManager(chat: Chat, commands: ConfigurationSection) : ChatManager(chat) {
    private val commands = mutableMapOf<String, FormatCommand>()
    private val players = mutableMapOf<UUID, FormatCommand>()

    init {
        commands.keys.forEach {
            val name = it.toString()
            val command = commands.getConfigurationSection(name)

            val displayName = command.getString("name") ?: name
            val condition = command.getString("condition")
            val viewCondition = command.getString("view-condition")
            val channel = command.getString("channel")
            val save = command.getBoolean("keep-on-reload") ?: true
            val prefix = command.getString("prefix")
            val display = command.getConfigurationSection("display")
            val cmd = FormatCommand(
                name,
                displayName,
                tab.placeholderManager.conditionManager.getByNameOrExpression(condition),
                tab.placeholderManager.conditionManager.getByNameOrExpression(viewCondition),
                channel ?: "",
                ChatUtils.componentsToMM(display),
                save,
                prefix!!
            )
            this.commands[name] = cmd

            tab.platform.registerCustomCommand(name) { sender, _ ->
                if (!cmd.isConditionMet(sender)) {
                    sender.sendMessage(tab.configuration.messages.noPermission)
                    return@registerCustomCommand
                }
                val name = cmd.displayName
                if (players.containsKey(sender.uniqueId)) {
                    players.remove(sender.uniqueId)
                    sender.sendMessage(translation.getChatCmdLeave(name))
                    return@registerCustomCommand
                }
                players[sender.uniqueId] = cmd
                sender.sendMessage(translation.getChatCmdJoin(name))
            }
        }
        val data = plugin.playerData.getMap<String, String>("chat-commands-formats")
        data.forEach { (uuid: String, cmd: String) ->
            if (cmd in this.commands) players[UUID.fromString(uuid)] = this.commands[cmd]!!
        }
    }

    override fun unload() {
        val data = players
            .filter { it.value.saveOnReload }
            .entries
            .associate { it.key.toString() to it.value.name }
            .ifEmpty { null }
        plugin.playerData.set("chat-commands-formats", data)
    }

    fun contains(player: TabPlayer) = player.uniqueId in players

    fun getFormat(player: TabPlayer) = players[player.uniqueId]

    fun getFromPrefix(sender: TabPlayer, message: String) = commands.values.find { message.startsWith(it.prefix) && it.isConditionMet(sender) }

}