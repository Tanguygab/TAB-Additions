package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.Title;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;

import java.util.List;

public class TitleCmd {


    public TitleCmd(String name, String[] args, List<Object> properties) {

        TABAdditions instance = TABAdditions.getInstance();
        Title feature = (Title) TAB.getInstance().getFeatureManager().getFeature("Title");


        if (args.length > 2 && args[2].equals("*")) {
            for (TabPlayer p : TAB.getInstance().getOnlinePlayers())
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
