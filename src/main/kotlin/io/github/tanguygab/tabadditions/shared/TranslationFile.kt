package io.github.tanguygab.tabadditions.shared

import io.github.tanguygab.tabadditions.shared.features.chat.emojis.EmojiCategory
import me.neznamy.tab.shared.config.file.YamlConfigurationFile
import me.neznamy.tab.shared.platform.TabPlayer
import java.io.File
import java.io.InputStream

class TranslationFile(source: InputStream?, destination: File) : YamlConfigurationFile(source, destination) {
    val providePlayer = get("player.provide", "&cYou have to provide a player name!")
    val playerNotFound = get("player.offline", "&cNo online player found with the name \"%player%\"")

    val actionBarOn = get("actionbars.on", "&aYou will now receive new actionbars!")
    val actionBarOff = get("actionbars.off", "&cYou won't receive any new actionbar!")
    val titleOn = get("titles.on", "&aYou will now receive new titles!")
    val titleOff = get("titles.off", "&cYou won't receive any new title!")

    val emojisOn = get("emojis.on", "&aYou will now see emojis in chat!")
    val emojisOff = get("emojis.off", "&cYou won't see any new emoji in chat!")
    private val emojiCategoryHeader = get(
        "emojis.categories.header",
        "&7All categories of emojis you have access to &8(%amount%, Emojis: %owned%/%max%)&7:"
    )
    private val emojiCategory = get("emojis.categories.category", "&7 - &8%category% &8(%owned%/%max%)")
    val emojiCategoryNotFound = get("emojis.category.not_found", "&7This category doesn't exist.")
    private val emojiHeader = get("emojis.category.header", "&7All emojis in this category &8(%owned%/%max%)&7:")
    private val emoji = get("emojis.category.emoji", "&7 - %emojiraw%&8: &r%emoji%")

    val chatOn = get("chat.on", "&aYou will now receive new chat messages!")
    val chatOff = get("chat.off", "&cYou won't receive any new chat message!")
    private val chatCooldown = get("chat.cooldown", "&cYou have to wait %seconds% more seconds!")
    private val chatCleared = get("chat.cleared", "&aChat cleared by %name%!")
    private val chatCmdJoin = get("chat.commands-formats.join", "&7You joined %name%!")
    private val chatCmdLeave = get("chat.commands-formats.leave", "&7You left %name%!")

    private val ignoreOn = get("ignore.on", "&cYou won't receive any new message from %name%!")
    private val ignoreOff = get("ignore.off", "&aYou will now receive new messages from %name%!")
    val cantIgnoreSelf = get("ignore.self", "&cYou can't ignore yourself!")
    val isIgnored = get("ignored.is", "&cThis player ignores you")

    val mentionOn = get("mentions.on", "&aMentions enabled.")
    val mentionOff = get("mentions.off", "&cMentions disabled.")

    val pmOn = get("msg.on", "&cYou will now receive new private messages!")
    val pmOff = get("msg.off", "&aYou won't receive any new private message!")
    private val pmCooldown = get("msg.cooldown", "&cYou have to wait %seconds% more seconds!")
    val cantPmSelf = get("msg.self", "&cYou can't message yourself!")
    val pmEmpty = get("msg.empty", "&7You have to provide a message!")
    val noPlayerToReplyTo = get("msg.no_reply", "&cYou don't have any player to reply to!")
    val hasPmOff = get("msg.has_off", "&cThis player doesn't accept private messages")


    val socialSpyOn = get("socialspy.on", "&aSocialSpy enabled.")
    val socialSpyOff = get("socialspy.off", "&cSocialSpy disabled.")

    val cantSwear = get("msg.cant-swear", "&cYou are not allowed to swear on this server!")

    fun get(path: String, defaultValue: String) = super.getString(path, defaultValue) ?: defaultValue

    fun getChatCleared(p: TabPlayer) = chatCleared.replace("%name%", p.name)

    fun getCooldown(time: Double) = chatCooldown.replace("%seconds%", time.toString())

    fun getIgnore(ignored: String, on: Boolean) = (if (on) ignoreOn else ignoreOff).replace("%name%", ignored)

    fun getPmCooldown(time: Double) = pmCooldown.replace("%seconds%", time.toString())

    fun getPlayerNotFound(player: String) = playerNotFound.replace("%player%", player)

    fun getEmojiCategory(p: TabPlayer, category: EmojiCategory) = emojiCategory
        .replace("%category%", category.name)
        .replace("%owned%", category.ownedEmojis(p).toString())
        .replace("%max%", category.emojis.size.toString())

    fun getEmojiCategoryHeader(amount: Int, owned: Int, total: Int) = emojiCategoryHeader
        .replace("%amount%", amount.toString())
        .replace("%owned%", owned.toString())
        .replace("%max%", total.toString())

    fun getEmoji(emojiraw: String, emoji: String) = this.emoji
        .replace("%emojiraw%", emojiraw)
        .replace("%emoji%", emoji)

    fun getEmojiHeader(owned: Int, max: Int) = emojiHeader
        .replace("%owned%", owned.toString())
        .replace("%max%", max.toString())

    fun getChatCmdJoin(name: String) = chatCmdJoin.replace("%name%", name)
    fun getChatCmdLeave(name: String) = chatCmdLeave.replace("%name%", name)
}
