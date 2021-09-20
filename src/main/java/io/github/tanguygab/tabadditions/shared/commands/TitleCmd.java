package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabAPI;

import java.util.List;

public class TitleCmd {


    public TitleCmd(String name, String[] args, List<Object> properties) {

        TABAdditions instance = TABAdditions.getInstance();

        String title;
        String subtitle;
        int fadeIn;
        int stay;
        int fadeOut;
        if (!args[1].startsWith("custom:")) {
            title = properties.get(1)+"";
            subtitle = properties.get(0)+"";
            fadeIn = (int) properties.get(2);
            stay = (int) properties.get(3);
            fadeOut = (int) properties.get(4);
        } else {
            String[] t = args[1].replace("custom:","").split("\\|\\|");
            title = t[0];
            subtitle = t.length > 1 ? t[1] : "";
            fadeIn = t.length > 2 ? parseInt(t[2]) : 5;
            stay = t.length > 3 ? parseInt(t[3]) : 5;
            fadeOut = t.length > 4 ? parseInt(t[4]) : 5;
        }

        if (args.length > 2 && args[2].equals("*")) {
            for (TabPlayer p : TabAPI.getInstance().getOnlinePlayers())
                instance.getPlatform().sendTitle(p, parseText(title, p), parseText(subtitle, p), fadeIn, stay, fadeOut);
            return;
        }

        TabPlayer p = null;
        if (args.length > 2)
            p = instance.getPlayer(args[2]);
        else if (!name.equals("~Console~"))
            p = instance.getPlayer(name);

        if (p == null) {
            instance.sendMessage(name, "&cThis player isn't connected!");
            return;
        }
        instance.getPlatform().sendTitle(p, parseText(title, p), parseText(subtitle, p), fadeIn, stay, fadeOut);
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 5;
        }
    }

    private String parseText(String str, TabPlayer p) {
        return TABAdditions.getInstance().parsePlaceholders(str,p,TabAPI.getInstance().getFeatureManager().getFeature("&aTitle&r")).replace("_"," ");
    }

}
