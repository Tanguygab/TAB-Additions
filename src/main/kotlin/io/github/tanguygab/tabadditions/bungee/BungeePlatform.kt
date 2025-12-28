package io.github.tanguygab.tabadditions.bungee

import com.google.common.io.ByteStreams
import io.github.tanguygab.tabadditions.shared.Platform
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.placeholder.PlaceholderManager
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer

class BungeePlatform(private val plugin: TABAdditionsBungeeCord) : Platform() {
    private var listener: BungeeListener? = null
    private val kyori = BungeeAudiences.create(plugin)

    override val isProxy = true

    override fun registerPlaceholders(pm: PlaceholderManager) {
        for (server in ProxyServer.getInstance().servers.keys) {
            pm.registerServerPlaceholder("%server-status:$server%", 10000) { plugin.getServerStatus(server) }
        }
    }

    override fun isPluginEnabled(plugin: String) = this.plugin.proxy.pluginManager.getPlugin(plugin) != null

    override fun sendTitle(
        player: TabPlayer,
        title: String,
        subtitle: String,
        fadeIn: Int,
        stay: Int,
        fadeout: Int
    ) {
        val t = plugin.proxy.createTitle()
            .title(TextComponent(title))
            .subTitle(TextComponent(subtitle))
            .fadeIn(fadeIn)
            .stay(stay)
            .fadeOut(fadeout)
        (player.player as ProxiedPlayer).sendTitle(t)
    }

    override fun sendActionbar(player: TabPlayer, text: String) {
        (player.player as ProxiedPlayer).sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(text))
    }

    override fun reload() {
        plugin.proxy.pluginManager.apply {
            if (listener != null) unregisterListener(listener)
            registerListener(plugin, BungeeListener().also { listener = it })
        }
    }

    override fun disable() {
        plugin.proxy.pluginManager.apply {
            unregisterListener(listener)
            unregisterCommands(plugin)
        }
    }

    override fun runTask(run: () -> Unit) {
        plugin.proxy.scheduler.runAsync(plugin, run)
    }

    override fun audience(player: TabPlayer?) = if (player == null)
        kyori.console()
    else kyori.player(player.uniqueId)

    override fun sendToDiscord(player: TabPlayer, msg: String, channel: String, plugins: List<String>) {
        val out = ByteStreams.newDataOutput()
        out.writeUTF(plugins.joinToString(","))
        out.writeUTF(msg)
        out.writeUTF(channel)
        (player.player as ProxiedPlayer).sendData("tabadditions:channel", out.toByteArray())
    }

    override fun supportsChatSuggestions() = false //isPluginEnabled("Protocolize");

    override fun updateChatComplete(player: TabPlayer, emojis: List<String>, add: Boolean) {
        //not supported, maybe with Protocolize?
    }
}