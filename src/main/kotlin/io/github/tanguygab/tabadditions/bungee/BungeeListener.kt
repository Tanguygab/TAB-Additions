package io.github.tanguygab.tabadditions.bungee

import com.google.common.io.ByteStreams
import io.github.tanguygab.tabadditions.shared.features.chat.Chat
import me.neznamy.tab.shared.TAB
import net.md_5.bungee.api.connection.Connection
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class BungeeListener : Listener {
    private val tab = TAB.getInstance()

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChat(e: ChatEvent) {
        if (e.isCancelled) return
        val chat = tab.featureManager.getFeature<Chat>("Chat") ?: return
        val player = getPlayer(e.sender)

        if (e.isCommand && chat.onCommand(player, e.message)) {
            e.isCancelled = true
            return
        }

        if (chat.bukkitBridgeChatEnabled) return
        e.isCancelled = true
        chat.onChat(player, e.message)
    }

    @EventHandler
    fun onMessageReceived(e: PluginMessageEvent) {
        if (!e.tag.equals("tabadditions:channel", ignoreCase = true)) return
        val `in` = ByteStreams.newDataInput(e.data)
        if (!`in`.readUTF().equals("Chat", ignoreCase = true)) return
        val chat = tab.featureManager.getFeature<Chat>("Chat")
        chat?.onChat(getPlayer(e.receiver), `in`.readUTF())
    }

    private fun getPlayer(player: Connection) = tab.getPlayer((player as ProxiedPlayer).uniqueId)!!
}
