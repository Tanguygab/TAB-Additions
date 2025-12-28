package io.github.tanguygab.tabadditions.shared

import dev.simplix.protocolize.api.Protocolize
import io.github.tanguygab.tabadditions.shared.features.chat.ChatItem
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.placeholder.PlaceholderManager
import net.kyori.adventure.audience.Audience

abstract class Platform {
    abstract val isProxy: Boolean
    abstract fun runTask(run: () -> Unit)
    abstract fun reload()
    abstract fun registerPlaceholders(pm: PlaceholderManager)
    abstract fun isPluginEnabled(plugin: String): Boolean
    abstract fun disable()

    abstract fun sendTitle(player: TabPlayer, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeout: Int)
    abstract fun sendActionbar(player: TabPlayer, text: String)

    abstract fun audience(player: TabPlayer?): Audience
    abstract fun sendToDiscord(player: TabPlayer, msg: String, channel: String, plugins: List<String>)
    abstract fun supportsChatSuggestions(): Boolean
    abstract fun updateChatComplete(player: TabPlayer, emojis: List<String>, add: Boolean)

    open fun getItem(player: TabPlayer, offhand: Boolean): ChatItem {
        val player = Protocolize.playerProvider().player(player.uniqueId)
        val inv = player.proxyInventory()
        val item = inv.item(if (offhand) inv.heldItem().toInt() else 45)
        val displayName = getItemName(item.displayName().asLegacyText(), item.itemType().toString())
        return ChatItem(item.itemType().toString(), displayName, item.amount().toInt(), item.nbtData().toString())
    }

    protected fun getItemName(displayName: String?, type: String): String {
        if (displayName != null) return displayName
        val type = type.replace("_", " ").lowercase()
        val type2 = StringBuilder()
        val typeList = type.split(" ")
        for (str in typeList) {
            type2.append(str.take(1).uppercase()).append(str.substring(1))
            if (typeList.indexOf(str) != typeList.size - 1) type2.append(" ")
        }
        return type2.toString()
    }
}