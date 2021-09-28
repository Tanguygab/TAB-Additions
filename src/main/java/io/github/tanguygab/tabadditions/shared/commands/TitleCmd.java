package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.Title;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabAPI;

import java.util.List;

public class TitleCmd {


    public TitleCmd(String name, String[] args, List<Object> properties) {

        TABAdditions instance = TABAdditions.getInstance();
        Title feature = (Title) TabAPI.getInstance().getFeatureManager().getFeature("&aTitle&r");


        if (args.length > 2 && args[2].equals("*")) {
            for (TabPlayer p : TabAPI.getInstance().getOnlinePlayers())
                feature.sendTitle(properties, args,p);
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
        feature.sendTitle(properties, args,p);
    }



}
