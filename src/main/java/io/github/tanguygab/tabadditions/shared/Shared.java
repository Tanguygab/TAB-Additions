package io.github.tanguygab.tabadditions.shared;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Animation;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.placeholders.Placeholders;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Shared {

    public String parsePlaceholders(TabPlayer p, String msg) {
        msg = me.neznamy.tab.shared.Shared.platform.replaceAllPlaceholders(msg,p);
        Pattern pattern = Pattern.compile("%animation:[a-zA-Z]+%");
        Matcher placeholder = pattern.matcher(msg);
        boolean find = placeholder.find();
        if (find)
            for (Animation anim : Configs.animations)
                if (anim.getName().equals(placeholder.group())){
                    Placeholders.getPlaceholder("%animation:"+anim.getName()+"%");
                    msg = msg.replace(pattern.pattern(),anim.getMessage());}
        return msg;
    }
}
