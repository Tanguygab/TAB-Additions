package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.intellij.lang.annotations.Subst;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils {

    private static final Pattern tabRGBPattern = Pattern.compile("#[0-9a-fA-F]{6}");

    public static int countMatches(String str, String sub) {
        if (str == null || str.length() == 0 || sub == null || sub.length() == 0) return 0;
        int count = 0;
        for (int idx = 0; (idx = str.indexOf(sub,idx)) != -1; idx += sub.length()) ++count;
        return count;
    }

    public static String componentsToMM(Map<String,Map<String,Object>> config) {
        StringBuilder output = new StringBuilder();
        config.values().forEach(component->output.append(componentToMM(component)));
        return output.toString();
    }

    public static String componentToMM(Map<String,Object> component) {
        if (component == null) return "";
        StringBuilder output = new StringBuilder();
        String text = toMMColors(component.get("text"));
        @SuppressWarnings("unchecked")
        String hover = toMMColors(component.get("hover") instanceof List ? String.join("\n",(List<String>)component.get("hover")) : component.get("hover"));
        String click = toMMColors(component.get("click"));
        String clickType = click.contains(":") ? click.substring(0,click.indexOf(":")) : "";
        click = click.contains(":") ? click.substring(click.indexOf(":")+1) : "";
        clickType = clickType.contains("_") ? clickType : clickType.replace("command","run_command")
                .replace("suggest","suggest_command")
                .replace("url","open_url")
                .replace("copy","copy_to_clipboard")
                .replace("file","open_file");
        if (!hover.equals("")) output.append("<hover:show_text:\"").append(hover).append("\">");
        if (!click.equals("")) output.append("<click:").append(clickType).append(":\"").append(click).append("\">");
        output.append(text);
        if (!click.equals("")) output.append("</click>");
        if (!hover.equals("")) output.append("</hover>");
        return output.toString();
    }

    public static String toMMColors(Object str) {
        if (str == null || str.equals("")) return "";
        String text = str.toString();
        text = RGBUtils.getInstance().applyFormats(text);

        text = text.replace("§","&");
        for (EnumChatFormat c : EnumChatFormat.values())
            text = text.replace("&"+c.getCharacter(),"<"+c.toString().toLowerCase()+">");
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

    public static List<UUID> registerToggleCmd(boolean toggleCmd, String data, String cmd, String placeholder, Function<me.neznamy.tab.api.TabPlayer,Object> fun) {
        if (!toggleCmd) return null;
        TABAdditions.getInstance().getPlatform().registerCommand(cmd);
        TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%"+placeholder+"%",1000,fun);
        return TABAdditions.getInstance().loadData(data,true);
    }

    public static Sound getSound(@Subst("block.note_block.pling") String sound) {
        Key key;
        try {key = Key.key(sound);}
        catch (Exception e) {key = Key.key("block.note_block.pling");}
        return Sound.sound(key,Sound.Source.MASTER,1,1);
    }
}
