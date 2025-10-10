package io.github.tanguygab.tabadditions.shared.features.chat

import io.github.tanguygab.tabadditions.shared.TABAdditions
import io.github.tanguygab.tabadditions.shared.features.chat.ChatUtils.applyFormats
import io.github.tanguygab.tabadditions.shared.features.chat.ChatUtils.componentToMM
import me.neznamy.tab.shared.chat.EnumChatFormat
import me.neznamy.tab.shared.config.file.ConfigurationFile
import me.neznamy.tab.shared.platform.TabPlayer
import java.util.regex.Pattern

class ChatFormatter(config: ConfigurationFile) {
    private val filterEnabled = config.getBoolean("char-filter.enabled", true)
    private val filterCancelMessage = config.getBoolean("char-filter.cancel-message", false)
    private val filterChar = config.getString("char-filter.char-replacement", "*")
    private val filterFakeLength = config.getInt("char-filter.fake-length", 0)
    private val filterOutput = componentToMM(config.getConfigurationSection("char-filter.output"))
    private val filterPatterns = config.getStringList("char-filter.filter", listOf()).map { Pattern.compile(it) }
    private val filterExempt = config.getStringList("char-filter.exempt", listOf()).map { Pattern.compile(it) }

    private val itemEnabled = config.getBoolean("item.enabled", true)
    private val itemMainHand = config.getString("item.mainhand", "[item]")
    private val itemOffHand = config.getString("item.offhand", "[offhand]")
    private val itemOutput = config.getString("item.output", "%name% x%amount%")
    private val itemOutputSingle = config.getString("item.output-single", "%name%")
    private val itemOutputAir = config.getString("item.output-air", "No Item")
    private val itemPermission = config.getBoolean("item.permission", false)

    private val customInteractions = mutableMapOf<String, Interaction>()

    private val embedURLs = config.getBoolean("embed-urls.enabled", true)
    private val urlsOutput = componentToMM(config.getConfigurationSection("embed-urls.output"))
    private val urlPattern = Pattern.compile("([&§][a-fA-Fk-oK-OrR0-9])?(?<url>(http(s)?:/.)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*))")
    private val ipv4Pattern = Pattern.compile("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}")

    init {
        val customInteractionsSection = config.getConfigurationSection("custom-interactions")
        customInteractionsSection.keys.forEach {
            val key = it.toString()
            val section = customInteractionsSection.getConfigurationSection(key)

            customInteractions[key] = Interaction(
                section.getString("input", ""),
                section.getBoolean("permission", false),
                componentToMM(section.getConfigurationSection("output"))
            )
        }
    }

    fun process(message: String, sender: TabPlayer): String {
        var message = message
        EnumChatFormat.entries
            .filterNot { sender.hasPermission("tabadditions.chat.color.&${it.character}") }
            .forEach { message = message.replace("&${it.character}", "") }

        message = applyFormats(message)

        if (!sender.hasPermission("tabadditions.chat.color.rgb"))
            message = ChatUtils.tabRGBPattern.matcher(message).replaceAll("")

        if (filterEnabled && !sender.hasPermission("tabadditions.chat.bypass.filter")) message = filter(message)
        if (embedURLs) message = embedURLs(message)
        if (itemEnabled && (!itemPermission || sender.hasPermission("tabadditions.chat.item"))) {
            message = formatItems(sender, message, false)
            message = formatItems(sender, message, true)
        }
        if (!customInteractions.isEmpty()) message = formatInteractions(sender, message)

        return message
    }

    fun shouldBlock(msg: String, sender: TabPlayer): Boolean {
        if (!filterEnabled || !filterCancelMessage || sender.hasPermission("tabadditions.chat.bypass.filter")) return false

        val map = mutableMapOf<Int, String>()
        filterExempt.forEach {
            if (it.pattern() !in msg) return@forEach
            val m = it.matcher(msg)
            while (m.find()) map[m.start()] = it.pattern()
        }

        filterPatterns.forEach {
            val matcher = it.matcher(msg)
            if (matcher.find()) {
                if (map.isEmpty() || map.any { (pos, str) -> str.length <= matcher.group().length || pos > matcher.start() || pos + str.length <= matcher.start() })
                    return true
            }
        }
        return false
    }

    private fun filter(msg: String): String {
        var msg = msg
        for (pattern in filterPatterns) {
            val matcher = pattern.matcher(msg)
            val map = mutableMapOf<Int, String>()
            for (bypass in filterExempt) {
                if (bypass.pattern() !in msg) continue
                val m = bypass.matcher(msg)
                while (m.find()) map[m.start()] = bypass.pattern()
            }
            val posJumps = mutableMapOf<String, Int>()
            while (matcher.find()) {
                val word = matcher.group()
                val wordReplaced = StringBuilder()
                val i = if (filterFakeLength < 1) word.length else filterFakeLength
                wordReplaced.append(filterChar.repeat(i))
                val posJump = posJumps.getOrDefault(word, 0)
                val output = filterOutput.replace("%word%", word).replace("%replacement%", wordReplaced.toString())

                if (map.isEmpty()) {
                    msg = msg.replace(word, output)
                } else {
                    for ((pos, str) in map) {
                        if (str.length > word.length && pos <= matcher.start() && pos + str.length > matcher.start()) continue
                        val sb = StringBuilder(msg)
                        sb.replace(matcher.start() + posJump, matcher.end() + posJump, output)
                        msg = sb.toString()

                        posJumps[word] = posJump + output.length - word.length
                    }
                }
            }
        }

        return msg
    }

    private fun formatItems(sender: TabPlayer, message: String, offhand: Boolean): String {
        val input = if (offhand) itemOffHand else itemMainHand
        if (input.isEmpty() || !message.contains(input)) return message

        val item: ChatItem = TABAdditions.INSTANCE.platform.getItem(sender, offhand)
        if (item.type == "minecraft:air") return message.replace(input, itemOutputAir)

        var text = "<hover:show_item:'${item.type}':${item.amount}"
        if (item.nbt != null) text += ":'${item.nbt.replace("'", "\\'")}'"
        text += ">${if (item.amount == 1) itemOutputSingle else itemOutput}</hover>"
            .replace("%name%", item.name)
            .replace("%amount%", item.amount.toString())
        return message.replace(input, text)
    }

    private fun formatInteractions(sender: TabPlayer, message: String): String {
        var message = message
        for ((key, interaction) in customInteractions) {
            if (interaction.permission && !sender.hasPermission("tabadditions.chat.interaction.$key")) continue
            if (interaction.input.isEmpty()) continue

            message = message.replace(
                interaction.input,
                TABAdditions.INSTANCE.parsePlaceholders(interaction.output, sender)
            )
        }
        return message
    }

    private fun embedURLs(message: String): String {
        var message = message
        val msg2 = message.replace(
            "#[A-Fa-f0-9]{6}".toRegex(),
            " "
        ) // removing RGB colors to avoid IPV4 check from breaking them
        val urlMatcher = urlPattern.matcher(msg2)
        val ipv4Matcher = ipv4Pattern.matcher(msg2)

        while (urlMatcher.find()) {
            val url = urlMatcher.group("url")
            message = message
                .replace(url, urlsOutput
                    .replace("%url%", url)
                    .replace("%fullurl%", "https://$url")
                )
        }
        while (ipv4Matcher.find()) {
            val ipv4 = ipv4Matcher.group()
            message = message.replace(ipv4, urlsOutput.replace("%url%", ipv4))
        }
        return message
    }

    private data class Interaction(
        val input: String,
        val permission: Boolean,
        val output: String
    )
}
