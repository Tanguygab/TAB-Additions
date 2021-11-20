package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.ActionBar;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;

public class ActionBarCmd {

    public ActionBarCmd(String name, String[] args, String actionbar) {

        TabAPI tab = TabAPI.getInstance();
        TABAdditions instance = TABAdditions.getInstance();
        ActionBar feature = (ActionBar) tab.getFeatureManager().getFeature("ActionBar");


        if (args.length > 2 && args[2].equals("*")) {
            for (TabPlayer p : tab.getOnlinePlayers()) {
                feature.addToNoBar(p);
                feature.sendActionBar(p, actionbar);
            }
            return;
        }

        TabPlayer p = null;
        if (args.length > 2)
            p = instance.getPlayer(args[2]);
        else if (!name.equals("~Console~"))
            p = instance.getPlayer(name);

        if (p == null) {
            instance.sendMessage(name,"&cThis player isn't connected");
            return;
        }
        feature.addToNoBar(p);
        feature.sendActionBar(p,actionbar.replace("_"," "));
    }


}
