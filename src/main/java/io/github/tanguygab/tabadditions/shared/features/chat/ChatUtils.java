package io.github.tanguygab.tabadditions.shared.features.chat;

import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.intellij.lang.annotations.Subst;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils {

    protected static final Pattern tabRGBPattern = Pattern.compile("#[0-9a-fA-F]{6}");

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

    public static String toMMColors(String text) {
        if (text == null || text.isEmpty()) return "";
        text = "§r"+text; // This prevents TAB from applying the MiniMessage format which yeeted everything MM related aside from colors
        text = RGBUtils.getInstance().applyFormats(text);
        text = text.substring(2);

        text = text.replace("§","&");
        for (EnumChatFormat c : EnumChatFormat.values()) {
            String string = c.name().toLowerCase();
            if (string.equals("underline")) string+="d";
            text = text.replace("&" + c.getCharacter(), "<" + string + ">");
        }
        text = text.replace("&u","<rainbow>");
        text = text.replace("<reset>","<bold:false><italic:false><underlined:false><strikethrough:false><obfuscated:false><white>");

        Matcher m = tabRGBPattern.matcher(text);
        List<String> rgbs = new ArrayList<>();
        while (m.find()) {
            String rgb = m.group();
            if (rgbs.contains(rgb)) continue;
            rgbs.add(rgb);
            text = text.replace(rgb,"<"+rgb+">");
        }
        return text;
    }

    public static Sound getSound(@Subst("block.note_block.pling") String sound) {
        Key key;
        try {key = Key.key(sound);}
        catch (Exception e) {key = Key.key("block.note_block.pling");}
        return Sound.sound(key,Sound.Source.MASTER,1,1);
    }
}
