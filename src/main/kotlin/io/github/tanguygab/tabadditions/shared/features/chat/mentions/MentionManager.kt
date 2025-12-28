package io.github.tanguygab.tabadditions.shared.features.chat.mentions

import io.github.tanguygab.tabadditions.shared.features.chat.Chat
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager
import io.github.tanguygab.tabadditions.shared.features.chat.ChatUtils
import me.neznamy.tab.shared.config.file.ConfigurationSection
import me.neznamy.tab.shared.platform.TabPlayer
import net.kyori.adventure.sound.Sound

class MentionManager(
    chat: Chat,
    private val input: String?,
    private val output: String,
    sound: String,
    toggleCmd: Boolean,
    private val outputForEveryone: Boolean,
    customMentions: ConfigurationSection
) : ChatManager(chat, toggleCmd, "mentions-off", "togglementions", "chat-mentions") {
    private val sound = ChatUtils.getSound(sound)
    private val mentions = mutableMapOf<String, CustomMention>()

    init {
        setToggleCmdMsgs(translation.mentionOn, translation.mentionOff)
        customMentions.keys.forEach {
            val mention = it.toString()
            val mentionSection = customMentions.getConfigurationSection(mention)

            mentions[mention] = CustomMention(
                mentionSection.getString("input"),
                mentionSection.getString("output"),
                tab.placeholderManager.conditionManager.getByNameOrExpression(mentionSection.getString("condition")),
                ChatUtils.getSound(mentionSection.getString("sound"))
            )
        }
    }

    fun isMentioned(msg: String, sender: TabPlayer, viewer: TabPlayer): Boolean {
        val input = plugin.parsePlaceholders(input, viewer)
        if (input.isEmpty()) return false
        if (!sender.hasPermission("tabadditions.chat.bypass.togglemention") && hasCmdToggled(viewer)) return false
        if (chat.isIgnored(sender, viewer)) return false
        return msg.lowercase().contains(input.lowercase())
    }

    //is there a better way to do this? I have no fucking clue as to what I've just done
    // Don't look at me, I'm just kotlinifying everything, I can't even remember how long it's been since I first wrote this lol
    fun process(sender: TabPlayer, viewer: TabPlayer, msg: String): String {
        //checking custom mentions
        val mentions = mentions.values.filter { it.matches(msg) }
        for (p in tab.onlinePlayers) {
            val mention = mentions.find { it.isConditionMet(p) }
            if (mention != null) playSound(p, mention.sound)
        }
        var msg = msg
        mentions.forEach { msg = it.replace(msg) }

        //checking player mentions
        var check = false
        if (outputForEveryone) for (player in tab.onlinePlayers) {
            if (isMentioned(msg, sender, player)) {
                check = true
                break
            }
        }
        else if (isMentioned(msg, sender, viewer)) {
            check = true
            playSound(viewer, sound)
        }
        return if (!check) msg
        else msg.replace(
            plugin.parsePlaceholders(input, viewer),
            plugin.parsePlaceholders(output, sender, viewer),
            true
        )
    }

    private fun playSound(player: TabPlayer, sound: Sound) {
        plugin.platform.audience(player).playSound(sound)
    }

    override fun onCommand(sender: TabPlayer, command: String) { toggleCmd(sender) }
}
