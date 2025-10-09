package io.github.tanguygab.tabadditions.shared.features.chat;

import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.util.function.TriFunction;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.intellij.lang.annotations.Subst;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ChatUtils {

    protected static final Pattern tabRGBPattern = Pattern.compile("([^:</]|^)(?<rgb>#[0-9a-fA-F]{6})");

    /** Formatter to use Kyori's &lt;gradient:#RRGGBB:#RRGGBB>Text&lt;/gradient> */
    private static final TriFunction<TabTextColor, String, TabTextColor, String> kyoriGradientFormatter =
            (start, text, end) -> String.format("<gradient:#%s:#%s>%s</gradient>", start.getHexCode(), end.getHexCode(), text);

    /** Formatter to convert RGB code to use Kyori's &lt;color:#RRGGBB>*/
    private static final Function<TabTextColor, String> kyoriRGBFormatter = color -> String.format("<color:#%s>", color.getHexCode());

    public static int countMatches(String str, String sub) {
        if (str == null || str.isEmpty() || sub == null || sub.isEmpty()) return 0;
        int count = 0;
        for (int idx = 0; (idx = str.indexOf(sub,idx)) != -1; idx += sub.length()) ++count;
        return count;
    }

    public static String componentsToMM(ConfigurationSection config) {
        StringBuilder output = new StringBuilder();
        config.getKeys().forEach(component->output.append(componentToMM(config.getConfigurationSection(component.toString()))));
        return output.toString();
    }

    public static String componentToMM(ConfigurationSection component) {
        StringBuilder output = new StringBuilder();
        String text = toMMColors(component.getString("text"));
        String hover = toMMColors(component.getObject("hover") instanceof List ? String.join("\n", component.getStringList("hover", List.of())) : component.getString("hover"));
        String click = toMMColors(component.getString("click"));
        String clickType = click.contains(":") ? click.substring(0,click.indexOf(":")) : "";
        click = click.contains(":") ? click.substring(click.indexOf(":")+1) : "";
        clickType = clickType.contains("_") ? clickType : clickType.replace("command","run_command")
                .replace("suggest","suggest_command")
                .replace("url","open_url")
                .replace("copy","copy_to_clipboard")
                .replace("file","open_file");
        if (!hover.isEmpty()) output.append("<hover:show_text:\"").append(hover).append("\">");
        if (!click.isEmpty()) output.append("<click:").append(clickType).append(":\"").append(click).append("\">");
        output.append(text);
        if (!click.isEmpty()) output.append("</click>");
        if (!hover.isEmpty()) output.append("</hover>");
        return output.toString();
    }

    public static String applyFormats(String text) {
        return RGBUtils.getInstance().applyFormats(text, kyoriGradientFormatter, kyoriRGBFormatter);
    }

    public static String toMMColors(String text) {
        if (text == null || text.isEmpty()) return "";
        text = applyFormats(text);

        text = text.replace("§","&");
        for (EnumChatFormat c : EnumChatFormat.values()) {
            String string = c.name().toLowerCase();
            if (string.equals("underline")) string+="d";
            text = text.replace("&" + c.getCharacter(), "<" + string + ">");
        }
        text = text.replace("&u","<rainbow>");
        text = text.replace("<reset>","<bold:false><italic:false><underlined:false><strikethrough:false><obfuscated:false><white>");

        text = tabRGBPattern.matcher(text).replaceAll(result -> {
            String rgb = result.group();
            return rgb.substring(0, rgb.length()-7) + "<color:" + rgb.substring(rgb.length()-7) + ">";
        });
        return text;
    }

    public static Sound getSound(@Subst("block.note_block.pling") String sound) {
        Key key;
        try {key = Key.key(sound);}
        catch (Exception e) {key = Key.key("block.note_block.pling");}
        return Sound.sound(key,Sound.Source.MASTER,1,1);
    }

    public static double getDouble(Object object) {
        return object instanceof Double d ? d : 0;
    }
}
