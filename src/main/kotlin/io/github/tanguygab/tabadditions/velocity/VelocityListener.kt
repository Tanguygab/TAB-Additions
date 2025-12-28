package io.github.tanguygab.tabadditions.velocity

import com.google.common.io.ByteStreams
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.proxy.Player
import io.github.tanguygab.tabadditions.shared.features.chat.Chat
import me.neznamy.tab.shared.TAB

class VelocityListener {
    private val tab = TAB.getInstance()

    @Subscribe
    fun onChat(e: PlayerChatEvent) {
        if (!e.result.isAllowed) return
        val chat = tab.featureManager.getFeature<Chat>("Chat")
        if (chat == null || chat.bukkitBridgeChatEnabled) return

        e.result = PlayerChatEvent.ChatResult.denied()
        chat.onChat(getPlayer(e.player), e.message)
    }

    @Subscribe
    fun onMessageReceived(e: PluginMessageEvent) {
        val receiver = e.source
        if (e.identifier !== VelocityPlatform.IDENTIFIER || receiver !is Player) return

        val input = ByteStreams.newDataInput(e.data)
        if (!input.readUTF().equals("Chat", ignoreCase = true)) return

        val chat = tab.featureManager.getFeature<Chat>("Chat")
        chat?.onChat(getPlayer(receiver), input.readUTF())
    }

    private fun getPlayer(player: Player) = tab.getPlayer(player.uniqueId)!!
}
