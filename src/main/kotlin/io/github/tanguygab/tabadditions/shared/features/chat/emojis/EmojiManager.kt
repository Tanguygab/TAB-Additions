package io.github.tanguygab.tabadditions.shared.features.chat.emojis

import io.github.tanguygab.tabadditions.shared.features.chat.Chat
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager
import io.github.tanguygab.tabadditions.shared.features.chat.ChatUtils.componentToMM
import io.github.tanguygab.tabadditions.shared.features.chat.ChatUtils.countMatches
import io.github.tanguygab.tabadditions.shared.features.chat.ChatUtils.toMMColors
import me.neznamy.tab.shared.config.file.ConfigurationSection
import me.neznamy.tab.shared.platform.TabPlayer

class EmojiManager(
    chat: Chat,
    private val output: String,
    private val untranslate: Boolean,
    autoCompleteEmojis: Boolean,
    emojis: ConfigurationSection,
    emojisCmdEnabled: Boolean,
    toggleCmd: Boolean
) : ChatManager(chat, toggleCmd, "emojis-off", "toggleemojis", "chat-emojis") {
    val autoCompleteEnabled = plugin.platform.supportsChatSuggestions() && autoCompleteEmojis
    private val emojiCategories = mutableMapOf<String, EmojiCategory>()
    private val emojisAutoCompleteList = mutableMapOf<TabPlayer, List<String>>()

    init {
        setToggleCmdMsgs(translation.emojisOn, translation.emojisOff)
        var totalEmojiCount = 0
        for (key in emojis.keys) {
            val category = key.toString()
            val categorySection = emojis.getConfigurationSection(category)
            val emojisMap = categorySection.getMap<String, String>("list", mapOf<String, String>())
            totalEmojiCount += emojisMap.size
            emojiCategories[category] = EmojiCategory(category, emojisMap, componentToMM(categorySection.getConfigurationSection("output")))
        }

        if (emojisCmdEnabled) tab.platform.registerCustomCommand("emojis") { sender, args ->
            if (args.isEmpty()) {
                getEmojisCategories(sender)
                return@registerCustomCommand
            }

            val category = emojiCategories[args.joinToString(" ")]
            if (category == null || !category.canUse(sender)) {
                sender.sendMessage(translation.emojiCategoryNotFound)
                return@registerCustomCommand
            }
            getEmojiCategory(sender, category)
        }
        if (autoCompleteEnabled) tab.onlinePlayers.forEach { if (!hasCmdToggled(it)) loadAutoComplete(it) }

        tab.placeholderManager.registerServerPlaceholder("%chat-emoji-total%", -1) { totalEmojiCount.toString() }
        chat.placeholders.add(
            tab.placeholderManager.registerPlayerPlaceholder("%chat-emoji-owned%", 5000)
            { ownedEmojis(it as TabPlayer).toString() }
        )
    }

    override fun unload() {
        if (autoCompleteEnabled) tab.onlinePlayers.forEach { unloadAutoComplete(it) }
        super.unload()
    }

    fun process(sender: TabPlayer, viewer: TabPlayer?, msg: String): String {
        if (hasCmdToggled(viewer) || hasCmdToggled(sender)) return msg
        var msg = msg

        for (category in emojiCategories.values) {
            val emojis = category.emojis
            if (emojis.isEmpty()) continue

            for ((rawEmoji, emoji) in emojis) {
                val count = countMatches(msg, rawEmoji)
                if (count == 0 || rawEmoji.isEmpty()) continue
                if (!category.canUse(sender, rawEmoji)) {
                    if (untranslate && emoji in msg) msg = msg.replace(emoji, rawEmoji)
                    continue
                }
                val output = plugin.parsePlaceholders(
                    getOutput(category)
                        .replace("%emojiraw%", rawEmoji)
                        .replace("%emoji%", emoji.replace("\"", "''")),
                    sender, viewer
                )
                msg = msg.replace(rawEmoji, output)
            }
        }
        return msg
    }

    private fun getOutput(category: EmojiCategory) = category.output.ifEmpty { output }

    fun ownedEmojis(player: TabPlayer) = emojiCategories.values.sumOf { it.ownedEmojis(player) }

    fun loadAutoComplete(player: TabPlayer) {
        val list = mutableListOf<String>()

        emojiCategories.values.forEach {
            list.addAll(
                if (it.canUse(player)) it.emojis.keys
                else it.emojis.keys.filter { emoji -> it.canUse(player, emoji) }
            )
        }
        emojisAutoCompleteList[player] = list
        plugin.platform.updateChatComplete(player, list, true)
    }

    fun unloadAutoComplete(player: TabPlayer) {
        if (player in emojisAutoCompleteList) plugin.platform.updateChatComplete(player, emojisAutoCompleteList[player]!!, false)
    }

    private fun getEmojisCategories(sender: TabPlayer) {
        val builder = StringBuilder()
        var i = 0
        var emojisOwned = 0
        emojiCategories.forEach { (categoryName: String, category: EmojiCategory) ->
            if (!category.canUse(sender)) return@forEach
            val owned = category.ownedEmojis(sender)
            if (owned == 0) return@forEach
            emojisOwned += owned
            builder
                .append("\n <click:run_command:\"/emojis $categoryName\">")
                .append(translation.getEmojiCategory(sender, category))
                .append("</click>")
            i++
        }

        val output = plugin.parsePlaceholders(
            translation.getEmojiCategoryHeader(i, emojisOwned, emojiCategories.size) + builder,
            sender
        )
        chat.sendMessage(sender, chat.mm.deserialize(toMMColors(output)))
    }

    private fun getEmojiCategory(sender: TabPlayer, category: EmojiCategory) {
        val builder = StringBuilder()
        val emojis = category.emojis
        var i = 0
        emojis.forEach { (emoji: String, output: String) ->
            if (!category.canUse(sender, emoji)) return@forEach
            builder
                .append("\n <click:suggest_command:\"$emoji\"><insert:\"$emoji\">")
                .append(translation.getEmoji(emoji, output))
                .append("</insert></click>")
            i++
        }

        if (i == 0) {
            sender.sendMessage(translation.emojiCategoryNotFound)
            return
        }
        val output = plugin.parsePlaceholders(translation.getEmojiHeader(i, emojis.size) + builder, sender)
        chat.sendMessage(sender, chat.mm.deserialize(toMMColors(output)))
    }

    override fun onCommand(sender: TabPlayer, command: String) {
        if (toggleCmd(sender)) loadAutoComplete(sender)
        else unloadAutoComplete(sender)
    }
}