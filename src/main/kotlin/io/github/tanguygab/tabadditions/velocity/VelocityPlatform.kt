package io.github.tanguygab.tabadditions.velocity

import com.google.common.io.ByteStreams
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import io.github.tanguygab.tabadditions.shared.Platform
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.placeholder.PlaceholderManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import java.time.Duration

class VelocityPlatform(private val plugin: TABAdditionsVelocity) : Platform() {
    private var listener: VelocityListener? = null

    override val isProxy = true

    override fun registerPlaceholders(pm: PlaceholderManager) {
        for (server in plugin.server.allServers) {
            pm.registerServerPlaceholder("%server-status:${server.serverInfo.name}%", 10000) { plugin.getServerStatus(server) }
        }
    }

    override fun isPluginEnabled(plugin: String) = this.plugin.server.pluginManager.getPlugin(plugin).isPresent

    override fun sendTitle(player: TabPlayer, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeout: Int) {
        val t = Title.title(
            Component.text(title),
            Component.text(subtitle),
            Title.Times.times(
                Duration.ofSeconds(fadeIn.toLong()),
                Duration.ofSeconds(stay.toLong()),
                Duration.ofSeconds(fadeout.toLong())
            )
        )
        (player.player as Player).showTitle(t)
    }

    override fun sendActionbar(player: TabPlayer, text: String) {
        (player.player as Player).sendActionBar(Component.text(text))
    }

    override fun reload() {
        if (listener != null) plugin.server.eventManager.unregisterListener(plugin, listener)
        plugin.server.eventManager.register(plugin, VelocityListener().also { listener = it })
    }

    override fun disable() {
        plugin.server.eventManager.unregisterListener(plugin, listener)
    }

    override fun runTask(run: () -> Unit) {
        plugin.server.scheduler.buildTask(plugin, run).schedule()
    }

    override fun audience(player: TabPlayer?) = if (player == null)
        plugin.server.consoleCommandSource!!
    else plugin.server.getPlayer(player.uniqueId).get()

    override fun sendToDiscord(player: TabPlayer, msg: String, channel: String, plugins: List<String>) {
        val out = ByteStreams.newDataOutput()
        out.writeUTF(plugins.joinToString(","))
        out.writeUTF(msg)
        out.writeUTF(channel)
        (player.player as Player).sendPluginMessage(IDENTIFIER, out.toByteArray())
    }

    override fun supportsChatSuggestions() = false //isPluginEnabled("Protocolize");

    override fun updateChatComplete(player: TabPlayer, emojis: List<String>, add: Boolean) {
        //not supported, maybe with Protocolize?
    }

    companion object {
        val IDENTIFIER: MinecraftChannelIdentifier = MinecraftChannelIdentifier.from("tabadditions:channel")
    }
}