package io.github.tanguygab.tabadditions.shared.features.chat

import me.neznamy.tab.shared.chat.EnumChatFormat
import me.neznamy.tab.shared.chat.TabTextColor
import me.neznamy.tab.shared.chat.rgb.RGBUtils
import me.neznamy.tab.shared.config.file.ConfigurationSection
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.intellij.lang.annotations.Subst
import java.util.regex.Pattern

object ChatUtils {
    val tabRGBPattern: Pattern = Pattern.compile("([^:</]|^)(?<rgb>#[0-9a-fA-F]{6})")

    /** Formatter to use Kyori's &lt;gradient:#RRGGBB:#RRGGBB>Text&lt;/gradient>  */
    private val kyoriGradientFormatter = { start: TabTextColor, text: String, end: TabTextColor ->
        "<gradient:#${start.hexCode}:#${end.hexCode}>$text</gradient>"
    }

    /** Formatter to convert RGB code to use Kyori's &lt;color:#RRGGBB> */
    private val kyoriRGBFormatter = { color: TabTextColor -> "<color:#${color.hexCode}>" }

    fun countMatches(str: String?, sub: String?): Int {
        if (str.isNullOrEmpty() || sub.isNullOrEmpty()) return 0
        var count = 0
        var idx = 0
        while (str.indexOf(sub, idx).also { idx = it } != -1) {
            ++count
            idx += sub.length
        }
        return count
    }

    fun componentsToMM(config: ConfigurationSection): String {
        val output = StringBuilder()
        config.keys.forEach { output.append(componentToMM(config.getConfigurationSection(it.toString()))) }
        return output.toString()
    }

    fun componentToMM(component: ConfigurationSection): String {
        val output = StringBuilder()
        val text = toMMColors(component.getString("text"))
        val hover = toMMColors(
            if (component.getObject("hover") is MutableList<*>)
                component.getStringList("hover", listOf()).joinToString("\n")
            else component.getString("hover")
        )
        var click = toMMColors(component.getString("click"))
        var clickType = click.substringBefore(":", "")
        click = if (":" in click) click.substring(click.indexOf(":") + 1) else ""
        clickType = if ("_" in clickType) clickType else clickType
            .replace("command", "run_command")
            .replace("suggest", "suggest_command")
            .replace("url", "open_url")
            .replace("copy", "copy_to_clipboard")
            .replace("file", "open_file")
        if (!hover.isEmpty()) output.append("<hover:show_text:\"$hover\">")
        if (!click.isEmpty()) output.append("<click:$clickType:\"$click\">")
        output.append(text)
        if (!click.isEmpty()) output.append("</click>")
        if (!hover.isEmpty()) output.append("</hover>")
        return output.toString()
    }

    fun applyFormats(text: String) = RGBUtils.getInstance().applyFormats(text, kyoriGradientFormatter, kyoriRGBFormatter)

    fun toMMColors(text: String?): String {
        var text = text
        if (text.isNullOrEmpty()) return ""
        text = applyFormats(text)

        text = text.replace("§", "&")
        for (c in EnumChatFormat.entries) {
            var string = c.name.lowercase()
            if (string == "underline") string += "d"
            text = text!!.replace("&" + c.character, "<$string>")
        }
        text = text.replace("&u", "<rainbow>")
        text = text.replace("<reset>", "<bold:false><italic:false><underlined:false><strikethrough:false><obfuscated:false><white>")

        text = tabRGBPattern.matcher(text).replaceAll {
            val rgb = it.group()
            rgb.dropLast(7) + "<color:" + rgb.substring(rgb.length - 7) + ">"
        }
        return text
    }

    fun getSound(@Subst("block.note_block.pling") sound: String?): Sound {
        val key = try {
            Key.key(sound ?: "block.note_block.pling")
        } catch (_: Exception) {
            Key.key("block.note_block.pling")
        }
        return Sound.sound(key, Sound.Source.MASTER, 1f, 1f)
    }
}
