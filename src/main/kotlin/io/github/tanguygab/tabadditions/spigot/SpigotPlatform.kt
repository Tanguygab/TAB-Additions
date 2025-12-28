package io.github.tanguygab.tabadditions.spigot

import github.scarsz.discordsrv.DiscordSRV
import io.github.tanguygab.tabadditions.shared.Platform
import io.github.tanguygab.tabadditions.shared.features.chat.ChatItem
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.placeholder.PlaceholderManager
import net.essentialsx.api.v2.services.discord.DiscordService
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import java.text.DecimalFormat
import kotlin.math.roundToInt
import kotlin.math.sqrt

class SpigotPlatform(private val plugin: TABAdditionsSpigot) : Platform() {
    private var listener: SpigotListener? = null
    private val kyori = BukkitAudiences.create(plugin)
    private var chatSuggestions = false

    init {
        try {
            Player::class.java.getDeclaredMethod("addCustomChatCompletions", MutableCollection::class.java)
            chatSuggestions = true
        } catch (_: NoSuchMethodException) {}
    }

    override val isProxy = false

    override fun registerPlaceholders(pm: PlaceholderManager) {
        pm.registerRelationalPlaceholder("%rel_distance%", 1000) { viewer: TabPlayer, target: TabPlayer ->
            val viewer0 = viewer.player as Player
            val target0 = target.player as Player
            if (viewer0.world != target0.world) return@registerRelationalPlaceholder "-1"

            val vLoc = viewer0.location
            val tLoc = target0.location
            format.format(sqrt(vLoc.distanceSquared(tLoc)).roundToInt())
        }
    }

    override fun isPluginEnabled(plugin: String) = this.plugin.server.pluginManager.isPluginEnabled(plugin)

    @Suppress("DEPRECATION")
    override fun sendTitle(
        player: TabPlayer,
        title: String,
        subtitle: String,
        fadeIn: Int,
        stay: Int,
        fadeout: Int
    ) {
        try {
            (player.player as Player).sendTitle(title, subtitle, fadeIn, stay, fadeout)
        } catch (_: Exception) {
            (player.player as Player).sendTitle(title, subtitle)
        }
    }

    @Suppress("DEPRECATION")
    override fun sendActionbar(player: TabPlayer, text: String) {
        (player.player as Player).spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(text))
    }

    override fun reload() {
        if (listener != null) HandlerList.unregisterAll(listener!!)
        plugin.server.pluginManager.registerEvents(SpigotListener().also { listener = it }, plugin)
    }

    @Suppress("removal")
    override fun disable() {
        plugin.pluginLoader.disablePlugin(plugin)
    }

    override fun runTask(run: () -> Unit) {
        plugin.server.scheduler.runTask(plugin, run)
    }

    override fun audience(player: TabPlayer?) = if (player == null)
        kyori.console()
    else kyori.player(player.uniqueId)

    override fun sendToDiscord(player: TabPlayer, msg: String, channel: String, plugins: List<String>) {
        val p = player.player as Player
        if ("DiscordSRV" in plugins && isPluginEnabled("DiscordSRV")) sendDiscordSRV(p, msg, channel)
        if ("EssentialsX" in plugins && isPluginEnabled("EssentialsDiscord") && !channel.isEmpty()) sendEssentialsX(p, msg)
    }

    private fun sendDiscordSRV(player: Player, msg: String, channel: String) {
        val discord = DiscordSRV.getPlugin()
        val mainChannel = discord.mainChatChannel
        val optionalChannel = discord.getOptionalChannel(channel)
        discord.processChatMessage(
            player,
            msg,
            if (msg.isEmpty() || optionalChannel == mainChannel) mainChannel else optionalChannel,
            false,
            null
        )
    }

    private fun sendEssentialsX(player: Player, msg: String?) {
        val api = plugin.server.servicesManager.load(DiscordService::class.java)
        api!!.sendChatMessage(player, msg)
    }

    override fun supportsChatSuggestions() = chatSuggestions

    override fun updateChatComplete(player: TabPlayer, emojis: List<String>, add: Boolean) {
        (player.player as Player).apply {
            if (add) addCustomChatCompletions(emojis)
            else removeCustomChatCompletions(emojis)
        }
    }

    @Suppress("DEPRECATION")
    override fun getItem(player: TabPlayer, offhand: Boolean): ChatItem {
        val inv = (player.player as Player).inventory
        val item = try { if (offhand) inv.itemInOffHand else inv.itemInMainHand }
        catch (_: Exception) { inv.itemInHand }
        return ChatItem(
            item.type.key.toString(),
            getItemName(item.itemMeta?.displayName, item.type.toString()),
            item.amount,
            item.itemMeta?.asString
        )
    }

    companion object {
        private val format = DecimalFormat("#.##")
    }
}