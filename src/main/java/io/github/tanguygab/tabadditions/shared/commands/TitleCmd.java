package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabAPI;

import java.util.List;

public class TitleCmd {
    public TitleCmd(String name, String[] args, List<Object> properties) {

        TABAdditions instance = TABAdditions.getInstance();
        TabFeature feature = TabAPI.getInstance().getFeatureManager().getFeature("&aTitle&r");

        String title = properties.get(0)+"";
        String subtitle = properties.get(1)+"";
        int fadeIn = (int) properties.get(2);
        int stay = (int) properties.get(3);
        int fadeOut = (int) properties.get(4);

        if (args.length > 2 && args[2].equals("*")) {
            for (TabPlayer p : TabAPI.getInstance().getOnlinePlayers())
                instance.getPlatform().sendTitle(p, instance.parsePlaceholders(title, p,feature), instance.parsePlaceholders(subtitle, p,feature), fadeIn, stay, fadeOut);
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
        instance.getPlatform().sendTitle(p, instance.parsePlaceholders(title, p,feature), instance.parsePlaceholders(subtitle, p,feature), fadeIn, stay, fadeOut);
    }


}
